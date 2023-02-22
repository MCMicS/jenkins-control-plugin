/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.security;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.net.IdeHttpClientHelpers;
import com.intellij.util.net.ssl.CertificateManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codinjutsu.tools.jenkins.exception.AuthenticationException;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.codinjutsu.tools.jenkins.logic.JenkinsNotifier;
import org.codinjutsu.tools.jenkins.model.RequestData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

class DefaultSecurityClient implements SecurityClient {

    static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final Logger LOG = Logger.getInstance(DefaultSecurityClient.class);
    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";
    private final CloseableHttpClient httpClient;
    private final CredentialsProvider credentialsProvider;
    private final AuthCache authCache;
    private final HttpClientContext httpClientContext;
    private final Function<String, RequestConfig> configCreator;
    protected @Deprecated String crumbData;
    protected JenkinsVersion jenkinsVersion = JenkinsVersion.VERSION_1;

    DefaultSecurityClient(String crumbData, int connectionTimout) {
        this(crumbData, connectionTimout, CertificateManager.getInstance().getSslContext(), true);
    }

    @SuppressWarnings("java:S2095")
    DefaultSecurityClient(String crumbData, int connectionTimout, SSLContext sslContext, boolean useProxySettings) {
        final var socketConfig = SocketConfig.custom()
                .setSoTimeout(connectionTimout).build();
        final var requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimout)
                .setConnectionRequestTimeout(connectionTimout)
                .setSocketTimeout(connectionTimout);
        this.credentialsProvider = new BasicCredentialsProvider();

        final RequestConfig defaultRequestConfig = requestConfig
                .setMaxRedirects(10)
                .build();
        final var httpClientBuilder = HttpClients.custom()
                .setSSLContext(sslContext)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setDefaultCredentialsProvider(credentialsProvider)
                .setRedirectStrategy(new JenkinsRedirectStrategy());
        this.httpClient = httpClientBuilder.build();
        if (useProxySettings) {
            this.configCreator = url -> {
                final var configForUrl = RequestConfig.copy(defaultRequestConfig);
                IdeHttpClientHelpers.ApacheHttpClient4.setProxyForUrlIfEnabled(configForUrl, url);
                IdeHttpClientHelpers.ApacheHttpClient4.setProxyCredentialsForUrlIfEnabled(credentialsProvider, url);
                return configForUrl.build();
            };
        } else {
            this.configCreator = url -> defaultRequestConfig;
        }
        this.crumbData = crumbData;

        this.authCache = new BasicAuthCache();
        this.httpClientContext = HttpClientContext.create();
        this.httpClientContext.setCredentialsProvider(this.credentialsProvider);
        this.httpClientContext.setAuthCache(authCache);
    }

    @Nullable
    @Override
    public String connect(URL jenkinsUrl) {
        return execute(jenkinsUrl);
    }

    public @NotNull Response execute(URL url, @NotNull Collection<RequestData> data) {
        String urlStr = url.toString();

        final var responseCollector = new ResponseCollector();
        runMethod(urlStr, data, responseCollector);

        if (isRedirection(responseCollector.statusCode)) {
            runMethod(responseCollector.data, data, responseCollector);
        }

        return new Response(responseCollector.statusCode, responseCollector.data, responseCollector.error);
    }

    @Override
    public @NotNull HttpClientContext getHttpClientContext() {
        return this.httpClientContext;
    }

    @Override
    public @NotNull CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * send parameter via Json:<br>
     * <pre>
     * {
     *   "parameter": [
     *     {
     *       "name": "Parameter-Name",
     *       "value": "Parameter-Value"
     *     },
     *     {
     *       "name": "File-Parameter",
     *       "file": "fileName-same-as-in-parts"
     *     }
     *   ]
     * }
     * </pre>
     */
    @NotNull
    JenkinsPost createPost(String url, @NotNull Collection<RequestData> data) {
        final var post = new JenkinsPost(url);
        post.setData(data);
        if (isCrumbDataSet()) {
            post.addHeader(jenkinsVersion.getCrumbName(), crumbData);
        }
        return post;
    }

    protected final void addAuthenticationPreemptive(HttpHost host, UsernamePasswordCredentials credentials) {
        final var authScope = new AuthScope(host.getHostName(), host.getPort());
        authCache.put(host, new BasicScheme());
        credentialsProvider.setCredentials(authScope, credentials);
    }

    protected final HttpHost getLastRedirectionHost(HttpHost host) {
        final var httpHead = new HttpHead(host.toURI());
        try {
            final var context = getHttpClientContext();
            final var response = executeHttp(httpHead);
            final var statusCode = response.getStatusLine().getStatusCode();

            var targetHost = host;
            if (HttpURLConnection.HTTP_OK == statusCode) {
                final var locations = context.getRedirectLocations();
                if (locations != null) {
                    final var lastHost = locations.get(locations.size() - 1).toURL();
                    targetHost = new HttpHost(lastHost.getHost(), lastHost.getPort(), lastHost.getProtocol());
                }
            }
            return targetHost;
        } catch (IOException cause) {
            final var errorMessage = Optional.ofNullable(cause.getMessage())
                    .orElseGet(() -> Optional.ofNullable(cause.getCause())
                            .map(Throwable::getMessage)
                            .orElse(String.valueOf(cause.getClass())));
            final String message = String.format("IO Error during retrieve last Host for Redirection for URL '%s': %s",
                    httpHead.getURI(), errorMessage);
            LOG.info(message, cause);
            JenkinsNotifier.notifyForCurrentContext(message, NotificationType.WARNING);
            return host;
        } finally {
            httpHead.releaseConnection();
        }
    }

    private void runMethod(String url, @NotNull Collection<RequestData> data, ResponseCollector responseCollector) {
        final var post = createPost(url, data);

        try {
            final var response = executeHttp(post);
            final var statusCode = response.getStatusLine().getStatusCode();
            final var responseBody = EntityUtils.toString(response.getEntity());
            final var errorHeader = response.getFirstHeader("X-Error");
            checkResponse(statusCode, responseBody);

            if (HttpURLConnection.HTTP_OK == statusCode) {
                responseCollector.collect(statusCode, responseBody);
            } else {
                responseCollector.collect(statusCode, response.getStatusLine().getReasonPhrase());
            }
            if (errorHeader != null) {
                responseCollector.error(errorHeader.getValue());
            }
            // if redirect handling not works or we still get a redirect header we handle it manually
            if (isRedirection(statusCode)) {
                responseCollector.collect(statusCode, response.getLastHeader("Location").getValue());
            }
        } catch (UnknownHostException uhEx) {
            throw new ConfigurationException(String.format("Unknown server: %s", uhEx.getMessage()), uhEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("IO Error during method execution [%s] for URL '%s'",
                    ioEx.getMessage(), createUrlForNotification(post)), ioEx);
        } finally {
            post.releaseConnection();
        }
    }

    protected final HttpResponse executeHttp(HttpPost post) throws IOException {
        final var postConfig = post.getConfig();
        if (postConfig == null) {
            post.setConfig(configCreator.apply(post.getURI().toASCIIString()));
        }
        return executeHttp((HttpRequestBase) post);
    }

    protected final HttpResponse executeHttp(HttpRequestBase httpRequest) throws IOException {
        return getHttpClient().execute(httpRequest, this.httpClientContext);
    }

    protected void checkResponse(int statusCode, String responseBody) throws AuthenticationException {
        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new AuthenticationException("Not found", responseBody);
        }

        if (HttpURLConnection.HTTP_BAD_REQUEST == statusCode) {
            throw new AuthenticationException("Invalid Request", responseBody);
        }

        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (StringUtils.containsIgnoreCase(responseBody, BAD_CRUMB_DATA)) {
                throw new AuthenticationException("CSRF enabled -> Missing or bad crumb data", responseBody);
            }

            throw new AuthenticationException("Unauthorized -> Missing or bad credentials", responseBody);
        }

        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error: Server unavailable", responseBody);
        }
    }

    private boolean isRedirection(int statusCode) {
        return statusCode / 100 == 3;
    }


    protected boolean isCrumbDataSet() {
        return StringUtils.isNotBlank(crumbData);
    }

    public void setJenkinsVersion(JenkinsVersion jenkinsVersion) {
        this.jenkinsVersion = jenkinsVersion;
    }

    protected final String createUrlForNotification(@NotNull String url) {
        int startQueryParamtersIndex = url.indexOf('?');
        if (startQueryParamtersIndex != -1) {
            return url.substring(0, startQueryParamtersIndex);
        }
        return url;
    }

    protected final String createUrlForNotification(HttpUriRequest httpMethod) {
        return createUrlForNotification(httpMethod.getURI().toString());
    }

    private static class ResponseCollector {

        private int statusCode;
        private String data;
        private String error;

        @NotNull ResponseCollector collect(int statusCode, String body) {
            this.data = body;
            return statusCode(statusCode);
        }

        @NotNull ResponseCollector statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        @NotNull ResponseCollector error(String error) {
            this.error = error;
            return this;
        }

    }

}
