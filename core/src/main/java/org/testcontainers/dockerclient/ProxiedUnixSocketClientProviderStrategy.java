package org.testcontainers.dockerclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.rnorth.tcpunixsocketproxy.TcpToUnixSocketProxy;
import org.testcontainers.utility.ComparableVersion;

import java.io.File;

@Slf4j
public class ProxiedUnixSocketClientProviderStrategy extends UnixSocketClientProviderStrategy {

    public static final int PRIORITY = EnvironmentAndSystemPropertyClientProviderStrategy.PRIORITY - 10;

    private final File socketFile = new File(DOCKER_SOCK_PATH);

    @Override
    protected boolean isApplicable() {
        final boolean nettyDoesNotSupportMacUnixSockets = SystemUtils.IS_OS_MAC_OSX &&
                ComparableVersion.OS_VERSION.isLessThan("10.12");

        return nettyDoesNotSupportMacUnixSockets && socketFile.exists();
    }

    @Override
    protected int getPriority() {
        return PRIORITY;
    }

    @Override
    public void test() throws InvalidConfigurationException {
        TcpToUnixSocketProxy proxy = new TcpToUnixSocketProxy(socketFile);

        try {
            int proxyPort = proxy.start().getPort();

            config = tryConfiguration("tcp://localhost:" + proxyPort);

            log.debug("Accessing unix domain socket via TCP proxy (" + DOCKER_SOCK_PATH + " via localhost:" + proxyPort + ")");
        } catch (Exception e) {

            proxy.stop();

            throw new InvalidConfigurationException("ping failed", e);
        }

    }

    @Override
    public String getDescription() {
        return "local Unix socket (via TCP proxy)";
    }

}
