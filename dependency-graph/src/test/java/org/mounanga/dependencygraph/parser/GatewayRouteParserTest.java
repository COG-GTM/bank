package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GatewayRouteParserTest {

    private GatewayRouteParser parser;

    @BeforeEach
    void setUp() {
        parser = new GatewayRouteParser();
    }

    @Test
    void parseDiscoveryFirstGateway_createsGatewayNode() {
        String content = loadResource("valid-gateway-discovery.yml");
        DependencyGraph graph = parser.parse(content);

        Node gateway = graph.findNode("gateway-service").orElseThrow();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
        assertThat(gateway.getMetadata()).containsEntry("port", "8888");
        assertThat(gateway.getMetadata()).containsEntry("applicationName", "GATEWAY-SERVICE");
    }

    @Test
    void parseDiscoveryFirstGateway_createsDiscoveryEdge() {
        String content = loadResource("valid-gateway-discovery.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> eurekaEdges = graph.getEdgesByType(EdgeType.EUREKA_REGISTER);
        assertThat(eurekaEdges).hasSize(1);
        assertThat(eurekaEdges.get(0).getSourceId()).isEqualTo("gateway-service");
        assertThat(eurekaEdges.get(0).getTargetId()).isEqualTo("discovery-service");
    }

    @Test
    void parseDiscoveryFirstGateway_noExplicitRoutes_setsRoutingMode() {
        String content = loadResource("valid-gateway-discovery.yml");
        DependencyGraph graph = parser.parse(content);

        Node gateway = graph.findNode("gateway-service").orElseThrow();
        assertThat(gateway.getMetadata()).containsEntry("routingMode", "discovery-first");
    }

    @Test
    void parseExplicitRoutes_createsRouteEdges() {
        String content = loadResource("valid-gateway-explicit-routes.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> routeEdges = graph.getEdgesByType(EdgeType.GATEWAY_ROUTE);
        assertThat(routeEdges).hasSize(4);
    }

    @Test
    void parseExplicitRoutes_extractsLoadBalancedTargets() {
        String content = loadResource("valid-gateway-explicit-routes.yml");
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.findNode("customer-service")).isPresent();
        assertThat(graph.findNode("account-service")).isPresent();
        assertThat(graph.findNode("notification-service")).isPresent();
    }

    @Test
    void parseExplicitRoutes_extractsHttpTargets() {
        String content = loadResource("valid-gateway-explicit-routes.yml");
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.findNode("auth-service")).isPresent();
    }

    @Test
    void parseExplicitRoutes_preservesPredicateMetadata() {
        String content = loadResource("valid-gateway-explicit-routes.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> routeEdges = graph.getEdgesByType(EdgeType.GATEWAY_ROUTE);
        Edge customerRoute = routeEdges.stream()
                .filter(e -> e.getTargetId().equals("customer-service"))
                .findFirst()
                .orElseThrow();

        assertThat(customerRoute.getMetadata()).containsKey("predicates");
        assertThat(customerRoute.getMetadata().get("predicates")).contains("Path=/bank/customers/**");
    }

    @Test
    void parseExplicitRoutes_preservesRouteIdMetadata() {
        String content = loadResource("valid-gateway-explicit-routes.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> routeEdges = graph.getEdgesByType(EdgeType.GATEWAY_ROUTE);
        Edge accountRoute = routeEdges.stream()
                .filter(e -> e.getTargetId().equals("account-service"))
                .findFirst()
                .orElseThrow();

        assertThat(accountRoute.getMetadata()).containsEntry("routeId", "account-route");
    }

    @Test
    void parseNullContent_throwsException() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseBlankContent_throwsException() {
        assertThatThrownBy(() -> parser.parse("  "))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseMalformedYaml_throwsException() {
        assertThatThrownBy(() -> parser.parse("{{not: valid:: yaml"))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void parseEmptyYaml_throwsException() {
        assertThatThrownBy(() -> parser.parse("---\n"))
                .isInstanceOf(GraphParseException.class);
    }

    @Test
    void parseYamlWithNoAppName_usesDefaultGatewayId() {
        String content = """
                server:
                  port: 9090
                spring:
                  cloud:
                    gateway:
                      routes:
                        - id: test
                          uri: lb://TEST-SERVICE
                          predicates:
                            - Path=/test/**
                """;
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.findNode("gateway-service")).isPresent();
        assertThat(graph.getEdgesByType(EdgeType.GATEWAY_ROUTE)).hasSize(1);
    }

    @Test
    void parseGatewayWithDiscoveryDisabled_noDiscoveryEdge() {
        String content = """
                server:
                  port: 8888
                spring:
                  application:
                    name: GATEWAY-SERVICE
                  cloud:
                    discovery:
                      enabled: false
                """;
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.getEdgesByType(EdgeType.EUREKA_REGISTER)).isEmpty();
        assertThat(graph.getNodesByType(NodeType.DISCOVERY)).isEmpty();
    }

    private String loadResource(String name) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(name)) {
            if (is == null) throw new IllegalStateException("Resource not found: " + name);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
