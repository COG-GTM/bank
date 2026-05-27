package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DockerComposeParserTest {

    private DockerComposeParser parser;

    @BeforeEach
    void setUp() {
        parser = new DockerComposeParser();
    }

    @Test
    void parseValidCompose_extractsAllServices() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.getNodes()).isNotEmpty();
        assertThat(graph.getNodesByType(NodeType.SERVICE)).hasSizeGreaterThanOrEqualTo(2);
        assertThat(graph.findNode("discovery-service")).isPresent();
        assertThat(graph.findNode("gateway-service")).isPresent();
        assertThat(graph.findNode("customer-service")).isPresent();
        assertThat(graph.findNode("account-service")).isPresent();
        assertThat(graph.findNode("notification-service")).isPresent();
    }

    @Test
    void parseValidCompose_classifiesDiscoveryService() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        Node discovery = graph.findNode("discovery-service").orElseThrow();
        assertThat(discovery.getType()).isEqualTo(NodeType.DISCOVERY);
    }

    @Test
    void parseValidCompose_classifiesGatewayService() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        Node gateway = graph.findNode("gateway-service").orElseThrow();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
    }

    @Test
    void parseValidCompose_extractsDependsOnEdges() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> gatewayEdges = graph.getOutgoingEdges("gateway-service");
        assertThat(gatewayEdges).anyMatch(e -> e.getTargetId().equals("discovery-service"));
    }

    @Test
    void parseValidCompose_infersJdbcEdges() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> jdbcEdges = graph.getEdgesByType(EdgeType.JDBC);
        assertThat(jdbcEdges).isNotEmpty();
        assertThat(jdbcEdges).anyMatch(e -> e.getSourceId().equals("customer-service"));
        assertThat(jdbcEdges).anyMatch(e -> e.getSourceId().equals("account-service"));
    }

    @Test
    void parseValidCompose_infersAxonEdge() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> axonEdges = graph.getEdgesByType(EdgeType.AXON);
        assertThat(axonEdges).hasSize(1);
        assertThat(axonEdges.get(0).getSourceId()).isEqualTo("account-service");
    }

    @Test
    void parseValidCompose_infersMailEdge() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        List<Edge> smtpEdges = graph.getEdgesByType(EdgeType.SMTP);
        assertThat(smtpEdges).hasSize(1);
        assertThat(smtpEdges.get(0).getSourceId()).isEqualTo("notification-service");
    }

    @Test
    void parseValidCompose_extractsPortMetadata() {
        String content = loadResource("valid-docker-compose.yml");
        DependencyGraph graph = parser.parse(content);

        Node discovery = graph.findNode("discovery-service").orElseThrow();
        assertThat(discovery.getMetadata()).containsKey("ports");
        assertThat(discovery.getMetadata().get("ports")).contains("8761");
    }

    @Test
    void parseNullContent_throwsException() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseBlankContent_throwsException() {
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseMalformedYaml_throwsException() {
        assertThatThrownBy(() -> parser.parse("{{invalid yaml:::"))
                .isInstanceOf(GraphParseException.class);
    }

    @Test
    void parseMissingServicesKey_throwsException() {
        String content = "version: '3.8'\nnetworks:\n  default:\n    driver: bridge\n";
        assertThatThrownBy(() -> parser.parse(content))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("No 'services' key");
    }

    @Test
    void parseEmptyServicesBlock_throwsException() {
        String content = "version: '3.8'\nservices:\n";
        assertThatThrownBy(() -> parser.parse(content))
                .isInstanceOf(GraphParseException.class);
    }

    @Test
    void parseComposeWithMapStyleDependsOn() {
        String content = """
                version: '3.8'
                services:
                  web:
                    image: web-app
                    depends_on:
                      db:
                        condition: service_healthy
                      redis:
                        condition: service_started
                  db:
                    image: mysql:8
                  redis:
                    image: redis:7
                """;
        DependencyGraph graph = parser.parse(content);

        List<Edge> webEdges = graph.getOutgoingEdges("web");
        assertThat(webEdges).anyMatch(e -> e.getTargetId().equals("db"));
        assertThat(webEdges).anyMatch(e -> e.getTargetId().equals("redis"));
    }

    @Test
    void parseComposeWithMapStyleEnvironment() {
        String content = """
                version: '3.8'
                services:
                  app:
                    image: app
                    environment:
                      MYSQL_HOST: db-host
                      MYSQL_DATABASE: mydb
                """;
        DependencyGraph graph = parser.parse(content);

        List<Edge> jdbcEdges = graph.getEdgesByType(EdgeType.JDBC);
        assertThat(jdbcEdges).hasSize(1);
        assertThat(jdbcEdges.get(0).getSourceId()).isEqualTo("app");
    }

    @Test
    void parseComposeWithRabbitMQ() {
        String content = """
                version: '3.8'
                services:
                  worker:
                    image: worker
                    environment:
                      - RABBITMQ_HOST=rabbit-server
                  rabbitmq:
                    image: rabbitmq:3-management
                """;
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.getNodesByType(NodeType.MESSAGE_BROKER)).isNotEmpty();
        assertThat(graph.getEdgesByType(EdgeType.AMQP)).hasSize(1);
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
