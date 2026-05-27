package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GatewayRouteParserTest {

    private GatewayRouteParser parser;

    @BeforeEach
    void setUp() {
        parser = new GatewayRouteParser();
    }

    @Test
    void parseDiscoveryFirstGateway_returnsDiscoveryRoute() {
        List<GatewayRouteParser.GatewayRoute> routes = parser.parse(
                readerFor("/valid-gateway.yml"));

        assertFalse(routes.isEmpty(), "Should detect discovery-first routing");
        assertTrue(routes.stream().anyMatch(r -> r.id().contains("discovery")));
    }

    @Test
    void parseExplicitRoutes_returnsAllRoutes() {
        List<GatewayRouteParser.GatewayRoute> routes = parser.parse(
                readerFor("/gateway-explicit-routes.yml"));

        assertEquals(3, routes.size());
        assertTrue(routes.stream().anyMatch(r -> r.targetService().equals("CUSTOMER-SERVICE")));
        assertTrue(routes.stream().anyMatch(r -> r.targetService().equals("ACCOUNT-SERVICE")));
        assertTrue(routes.stream().anyMatch(r -> r.targetService().equals("NOTIFICATION-SERVICE")));
    }

    @Test
    void parseExplicitRoutes_extractsPredicatePaths() {
        List<GatewayRouteParser.GatewayRoute> routes = parser.parse(
                readerFor("/gateway-explicit-routes.yml"));

        GatewayRouteParser.GatewayRoute customerRoute = routes.stream()
                .filter(r -> r.targetService().equals("CUSTOMER-SERVICE"))
                .findFirst().orElseThrow();
        assertEquals("/api/customers/**", customerRoute.predicatePath());
    }

    @Test
    void addToGraph_createsHttpEdgesFromGateway() {
        List<GatewayRouteParser.GatewayRoute> routes = parser.parse(
                readerFor("/gateway-explicit-routes.yml"));

        DependencyGraph graph = new DependencyGraph();
        parser.addToGraph(graph, "gateway-service", routes);

        assertTrue(graph.getNode("gateway-service").isPresent());
        assertEquals(3, graph.getEdges().stream()
                .filter(e -> e.getSourceId().equals("gateway-service") && e.getType() == EdgeType.HTTP)
                .count());
    }

    @Test
    void parseMalformedGateway_returnsEmptyRoutes() {
        List<GatewayRouteParser.GatewayRoute> routes = parser.parse(
                readerFor("/malformed-gateway.yml"));
        assertTrue(routes.isEmpty(), "No gateway routes should be found");
    }

    @Test
    void parseEmptyYaml_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(new StringReader("")));
    }

    @Test
    void parseNullYaml_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(new StringReader("---\n")));
    }

    private Reader readerFor(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        assertNotNull(is, "Test resource not found: " + resource);
        return new InputStreamReader(is);
    }
}
