package org.codinjutsu.tools.jenkins.security;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Http4DefaultSecurityClient implements SecurityClient {

    private static final String BAD_CRUMB_DATA = "No valid crumb was included in the request";

    private final CloseableHttpClient httpClient;

    public Http4DefaultSecurityClient() {
        httpClient = HttpClients.createDefault();
    }

    @Override
    public void connect(URL url) {
        execute(url);
    }

    @Override
    public String execute(URL url) {
        String urlStr = url.toString();
        HttpGet httpGet = new HttpGet(urlStr);
        try {
            CloseableHttpResponse response = httpClient.execute(httpGet);

            try {
                int statusCode = response.getStatusLine().getStatusCode();
                HttpEntity entity = response.getEntity();
                InputStream contentStream = entity.getContent();
                String content = IOUtils.toString(contentStream);

                checkResponse(statusCode, content);

                IOUtils.closeQuietly(contentStream);
                EntityUtils.consume(entity);

                return content;
            } finally {
                response.close();
            }

        } catch (ClientProtocolException protocolEx) {
            throw new ConfigurationException(String.format("HTTP Error during method execution '%s': %s", urlStr, protocolEx.getMessage()), protocolEx);
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("IO Error during method execution '%s': %s", urlStr, ioEx.getMessage()), ioEx);
        }
    }

    public void close() {
        try {
            httpClient.close();
        } catch (IOException ioEx) {
            throw new ConfigurationException(String.format("Unable de close HTTP client: %s", ioEx.getMessage()), ioEx);
        }
    }

    @Override
    public void setFiles(Map<String, VirtualFile> files) {

    }

    private void checkResponse(int statusCode, String responseBody) throws AuthenticationException {
        if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new AuthenticationException("Not found");
        }

        if (statusCode == HttpURLConnection.HTTP_FORBIDDEN || statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (StringUtils.containsIgnoreCase(responseBody, BAD_CRUMB_DATA)) {
                throw new AuthenticationException("CSRF enabled -> Missing or bad crumb data");
            }

            throw new AuthenticationException("Unauthorized -> Missing or bad credentials", responseBody);
        }

        if (HttpURLConnection.HTTP_INTERNAL_ERROR == statusCode) {
            throw new AuthenticationException("Server Internal Error: Server unavailable");
        }
    }
}
