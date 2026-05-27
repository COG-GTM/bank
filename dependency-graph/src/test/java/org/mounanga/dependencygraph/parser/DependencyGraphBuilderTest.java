package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyGraphBuilderTest {

    private DependencyGraphBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DependencyGraphBuilder();
    }

    @Test
    void buildFromReaders_combinesAllSources() throws IOException {
        Reader composeReader = readerFor("/valid-docker-compose.yml");
        List<Reader> eurekaReaders = List.of(
                readerFor("/valid-eureka.properties"),
                readerFor("/eureka-no-register.properties")
        );
        Reader gatewayReader = readerFor("/gateway-explicit-routes.yml");

        DependencyGraph graph = builder.buildFromReaders(composeReader, eurekaReaders, gatewayReader);

        assertFalse(graph.getNodes().isEmpty());
        assertFalse(graph.getEdges().isEmpty());

        assertTrue(graph.getNode("discovery-service").isPresent());
        assertTrue(graph.getNode("gateway-service").isPresent());
    }

    @Test
    void buildFromReaders_withNullGateway_doesNotFail() throws IOException {
        Reader composeReader = readerFor("/valid-docker-compose.yml");

        DependencyGraph graph = builder.buildFromReaders(composeReader, List.of(), null);

        assertFalse(graph.getNodes().isEmpty());
    }

    @Test
    void buildFromReaders_graphHasMixedEdgeTypes() throws IOException {
        Reader composeReader = readerFor("/docker-compose-with-broker.yml");

        DependencyGraph graph = builder.buildFromReaders(composeReader, List.of(), null);

        boolean hasAmqp = graph.getEdges().stream().anyMatch(e -> e.getType() == EdgeType.AMQP);
        boolean hasJdbc = graph.getEdges().stream().anyMatch(e -> e.getType() == EdgeType.JDBC);
        assertTrue(hasAmqp, "Should have AMQP edges");
        assertTrue(hasJdbc, "Should have JDBC edges");
    }

    private Reader readerFor(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        assertNotNull(is, "Test resource not found: " + resource);
        return new InputStreamReader(is);
    }
}
