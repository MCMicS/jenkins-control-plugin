package org.codinjutsu.tools.jenkins.security;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideProxySelector;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.protocol.HttpContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
public final class JenkinsConnectionSocketFactory {

    public @NotNull Registry<ConnectionSocketFactory> getRegistry(@NotNull String url, @NotNull SSLContext sslContext) {
        final Proxy proxy = getProxy(url);
        if(proxy.type() == Proxy.Type.SOCKS) {
            return createSocksRegistry(proxy, sslContext);
        }
        return createDefaultRegistry(sslContext);
    }

    @NotNull
    private static Registry<ConnectionSocketFactory> createDefaultRegistry(@NotNull SSLContext sslContext) {
        String[] supportedProtocols = null;
        String[] supportedCipherSuites = null;
        HostnameVerifier proxyAuthStrategyCopy = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        final SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
                supportedProtocols, supportedCipherSuites, proxyAuthStrategyCopy);

        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
    }

    public boolean prepareContext(@NotNull String url,
                               @NotNull SSLContext sslContext,
                               @NotNull HttpClientContext httpClientContext,
                               @NotNull JenkinsDnsResolver dnsResolver) {
        httpClientContext.removeAttribute("http.socket-factory-registry");
        dnsResolver.clearFakes();
        final URI uri = toUri(url);
        final Proxy proxy = getProxy(uri);
        if(proxy.type() == Proxy.Type.SOCKS) {
            // see org.apache.http.impl.conn.DefaultHttpClientConnectionOperator.SOCKET_FACTORY_REGISTRY
            httpClientContext.setAttribute("http.socket-factory-registry", getRegistry(url, sslContext));
            if (uri != null) {
                dnsResolver.addFakeHost(uri.getHost());
            }
            return true;
        }
        return false;
    }

    private Registry<ConnectionSocketFactory> createSocksRegistry(@NotNull Proxy proxy,
                                                                  @NotNull SSLContext sslContext) {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new SocksPlainConnectionSocketFactory(proxy))
                .register("https", new SocksSSLConnectionSocketFactory(sslContext, proxy))
                .build();
    }

    @NotNull
    private static HttpConfigurable getHttpConfigurable() {
        return HttpConfigurable.getInstance();
    }

    private static @NotNull Proxy getProxy(String url) {
        return getProxy(toUri(url));
    }

    private static @NotNull Proxy getProxy(@Nullable URI uri) {
        final List<Proxy> proxies = uri == null ? Collections.emptyList() :
                new IdeaWideProxySelector(getHttpConfigurable()).select(uri);
        return proxies.isEmpty() ? Proxy.NO_PROXY : proxies.get(0);
    }

    @Nullable
    private static URI toUri(String url) {
        return url != null ? VfsUtil.toUri(url) : null;
    }


    private static final class SocksPlainConnectionSocketFactory extends PlainConnectionSocketFactory {
        @NotNull
        private final Proxy proxy;

        public SocksPlainConnectionSocketFactory(@NotNull Proxy proxy) {
            super();
            this.proxy = proxy;
        }

        @Override
        @NotNull
        public Socket createSocket(@Nullable HttpContext context) {
            return new Socket(this.proxy);
        }

        @Override
        @NotNull
        public Socket connectSocket(int connectTimeout,
                                    @Nullable Socket socket,
                                    @NotNull HttpHost host,
                                    @NotNull InetSocketAddress remoteAddress,
                                    @Nullable InetSocketAddress localAddress,
                                    @Nullable HttpContext context) throws IOException {
            InetSocketAddress unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }

    private static final class SocksSSLConnectionSocketFactory extends SSLConnectionSocketFactory {
        @NotNull
        private final Proxy proxy;

        public SocksSSLConnectionSocketFactory(@NotNull SSLContext sslContext, @NotNull Proxy proxy) {
            super(sslContext);
            this.proxy = proxy;
        }

        @Override
        @NotNull
        public Socket createSocket(@NotNull HttpContext context) {
            return new Socket(this.proxy);
        }

        @Override
        @NotNull
        public Socket connectSocket(int connectTimeout,
                                    @Nullable Socket socket,
                                    @NotNull HttpHost host,
                                    @NotNull InetSocketAddress remoteAddress,
                                    @Nullable InetSocketAddress localAddress,
                                    @Nullable HttpContext context) throws IOException {
            final var unresolvedRemote = InetSocketAddress.createUnresolved(host.getHostName(), remoteAddress.getPort());
            return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
        }
    }
}
