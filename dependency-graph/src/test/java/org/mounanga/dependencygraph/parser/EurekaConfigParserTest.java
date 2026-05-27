package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class EurekaConfigParserTest {

    private EurekaConfigParser parser;

    @BeforeEach
    void setUp() {
        parser = new EurekaConfigParser();
    }

    @Test
    void parseValidProperties_extractsServiceName() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/valid-eureka.properties"));
        assertEquals("NOTIFICATION-SERVICE", metadata.serviceName());
    }

    @Test
    void parseValidProperties_extractsEurekaUrl() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/valid-eureka.properties"));
        assertNotNull(metadata.eurekaUrl());
        assertTrue(metadata.eurekaUrl().contains("localhost:8761"));
    }

    @Test
    void parseValidProperties_registerWithEurekaDefaultsTrue() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/valid-eureka.properties"));
        assertTrue(metadata.registerWithEureka());
        assertTrue(metadata.fetchRegistry());
    }

    @Test
    void parseDiscoveryServiceConfig_registerIsFalse() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/eureka-no-register.properties"));
        assertEquals("DISCOVERY-SERVICE", metadata.serviceName());
        assertFalse(metadata.registerWithEureka());
        assertFalse(metadata.fetchRegistry());
    }

    @Test
    void addToGraph_createsNodeAndEurekaEdge() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/valid-eureka.properties"));

        DependencyGraph graph = new DependencyGraph();
        parser.addToGraph(graph, metadata);

        assertTrue(graph.getNode("notification-service").isPresent());
        boolean hasEurekaEdge = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("notification-service")
                        && e.getType() == EdgeType.EUREKA);
        assertTrue(hasEurekaEdge, "Should create EUREKA edge for registering service");
    }

    @Test
    void addToGraph_discoveryServiceDoesNotCreateSelfEdge() throws IOException {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseProperties(
                readerFor("/eureka-no-register.properties"));

        DependencyGraph graph = new DependencyGraph();
        parser.addToGraph(graph, metadata);

        assertTrue(graph.getNode("discovery-service").isPresent());
        boolean hasEurekaEdge = graph.getEdges().stream()
                .anyMatch(e -> e.getType() == EdgeType.EUREKA);
        assertFalse(hasEurekaEdge, "Discovery service with register=false should not create EUREKA edge");
    }

    @Test
    void parseMalformedProperties_missingAppName_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parseProperties(readerFor("/malformed-eureka.properties")));
    }

    @Test
    void parseValidYaml_extractsMetadata() {
        EurekaConfigParser.EurekaMetadata metadata = parser.parseYaml(
                readerFor("/valid-gateway.yml"));
        assertEquals("GATEWAY-SERVICE", metadata.serviceName());
        assertNotNull(metadata.eurekaUrl());
    }

    @Test
    void parseEmptyYaml_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parseYaml(new StringReader("")));
    }

    private Reader readerFor(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        assertNotNull(is, "Test resource not found: " + resource);
        return new InputStreamReader(is);
    }
}
