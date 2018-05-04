package org.testcontainers.containers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.testcontainers.DockerClientFactory;

import static org.rnorth.visibleassertions.VisibleAssertions.*;
import static org.testcontainers.containers.Network.newNetwork;

@RunWith(Enclosed.class)
public class NetworkTest {

    public static class WithRules {

        @Rule
        public Network network = newNetwork();

        @Rule
        public GenericContainer foo = new GenericContainer()
                .withNetwork(network)
                .withNetworkAliases("foo")
                .withCommand("/bin/sh", "-c", "while true ; do printf 'HTTP/1.1 200 OK\\n\\nyay' | nc -l -p 8080; done");

        @Rule
        public GenericContainer bar = new GenericContainer()
                .withNetwork(network)
                .withCommand("top");

        @Test
        public void testNetworkSupport() throws Exception {
            String response = bar.execInContainer("wget", "-O", "-", "http://foo:8080").getStdout();
            assertEquals("received response", "yay", response);
        }
    }

    public static class WithoutRules {

        @Test
        public void testNetworkSupport() throws Exception {
            try (
                    Network network = newNetwork();

                    GenericContainer foo = new GenericContainer()
                            .withNetwork(network)
                            .withNetworkAliases("foo")
                            .withCommand("/bin/sh", "-c", "while true ; do printf 'HTTP/1.1 200 OK\\n\\nyay' | nc -l -p 8080; done");

                    GenericContainer bar = new GenericContainer()
                            .withNetwork(network)
                            .withCommand("top")
            ) {
                foo.start();
                bar.start();

                String response = bar.execInContainer("wget", "-O", "-", "http://foo:8080").getStdout();
                assertEquals("received response", "yay", response);
            }
        }

        @Test
        public void testBuilder() throws Exception {
            try (
                    Network network = Network.builder()
                            .driver("macvlan")
                            .build();
            ) {
                String id = network.getId();
                assertEquals(
                        "Flag is set",
                        "macvlan",
                        DockerClientFactory.instance().client().inspectNetworkCmd().withNetworkId(id).exec().getDriver()
                );
            }
        }

        @Test
        public void testModifiers() throws Exception {
            try (
                    Network network = Network.builder()
                            .createNetworkCmdModifier(cmd -> cmd.withDriver("macvlan"))
                            .build();
            ) {
                String id = network.getId();
                assertEquals(
                        "Flag is set",
                        "macvlan",
                        DockerClientFactory.instance().client().inspectNetworkCmd().withNetworkId(id).exec().getDriver()
                );
            }
        }

        @Test
        public void testReusability() throws Exception {
            try (Network network = Network.newNetwork()) {
                String firstId = network.getId();
                assertNotNull(
                        "Network exists",
                        DockerClientFactory.instance().client().inspectNetworkCmd().withNetworkId(firstId).exec()
                );

                network.close();

                assertNotEquals(
                        "New network created",
                        firstId,
                        DockerClientFactory.instance().client().inspectNetworkCmd().withNetworkId(network.getId()).exec().getId()
                );
            }
        }
    }
}
