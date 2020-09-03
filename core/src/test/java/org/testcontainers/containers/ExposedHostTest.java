package org.testcontainers.containers;

import com.google.common.collect.ImmutableMap;
import com.sun.net.httpserver.HttpServer;
import lombok.SneakyThrows;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.Testcontainers;

import java.io.OutputStream;
import java.net.InetSocketAddress;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.testcontainers.TestImages.TINY_IMAGE;

public class ExposedHostTest {

    private static HttpServer server;

    @BeforeClass
    public static void setUpClass() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            byte[] content = "Hello World!".getBytes();
            exchange.sendResponseHeaders(200, content.length);
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(content);
                responseBody.flush();
            }
        });

        server.start();
        Testcontainers.exposeHostPorts(server.getAddress().getPort());

        Testcontainers.exposeHostPorts(ImmutableMap.of(server.getAddress().getPort(), 80));
        Testcontainers.exposeHostPorts(ImmutableMap.of(server.getAddress().getPort(), 81));
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        server.stop(0);
    }

    @Test
    public void testExposedHost() throws Exception {
        assertResponse(new GenericContainer<>(TINY_IMAGE)
            .withCommand("top"),
            server.getAddress().getPort());
    }

    @Test
    public void testExposedHostWithNetwork() throws Exception {
        try (Network network = Network.newNetwork()) {
            assertResponse(new GenericContainer<>(TINY_IMAGE)
                .withNetwork(network)
                .withCommand("top"),
                server.getAddress().getPort());
        }
    }

    @Test
    public void testExposedHostPortOnFixedInternalPorts() throws Exception {
        assertResponse(new GenericContainer<>(TINY_IMAGE)
            .withCommand("top"),
            80);
        assertResponse(new GenericContainer<>(TINY_IMAGE)
            .withCommand("top"),
            81);
    }

    @SneakyThrows
    protected void assertResponse(GenericContainer<?> container, int port) {
        try {
            container.start();

            String response = container.execInContainer("wget", "-O", "-", "http://host.testcontainers.internal:" + port).getStdout();

            assertEquals("received response", "Hello World!", response);
        } finally {
            container.stop();
        }
    }
}
