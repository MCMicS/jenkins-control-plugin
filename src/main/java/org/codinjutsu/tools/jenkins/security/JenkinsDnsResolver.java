package org.codinjutsu.tools.jenkins.security;

import org.apache.http.conn.DnsResolver;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

public class JenkinsDnsResolver implements DnsResolver {
    private static final DnsResolver DNS_RESOLVER = SystemDefaultDnsResolver.INSTANCE;
    private final Set<String> hostWithFakeDns = new HashSet<>();

    public void clearFakes() {
        hostWithFakeDns.clear();
    }

    public void addFakeHost(String host) {
        hostWithFakeDns.add(host);
    }

    @Override
    public InetAddress[] resolve(@NotNull String host) throws UnknownHostException {
        return hostWithFakeDns.contains(host) ? returnFakeDns(): DNS_RESOLVER.resolve(host);
    }

    private InetAddress[] returnFakeDns() throws UnknownHostException {
        final var fakeIp = new byte[]{1, 1, 1, 1};
        return new InetAddress[]{InetAddress.getByAddress(fakeIp)};
    }
}
