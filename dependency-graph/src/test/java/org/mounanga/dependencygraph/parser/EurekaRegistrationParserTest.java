package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EurekaRegistrationParserTest {

    private EurekaRegistrationParser parser;

    @BeforeEach
    void setUp() {
        parser = new EurekaRegistrationParser();
    }

    @Test
    void parseValidProperties_extractsServiceNode() {
        String content = loadResource("valid-eureka-registration.properties");
        DependencyGraph graph = parser.parse(content);

        Node service = graph.findNode("customer-service").orElseThrow();
        assertThat(service.getType()).isEqualTo(NodeType.SERVICE);
        assertThat(service.getMetadata()).containsEntry("applicationName", "CUSTOMER-SERVICE");
        assertThat(service.getMetadata()).containsEntry("port", "8886");
    }

    @Test
    void parseValidProperties_extractsEurekaRegistration() {
        String content = loadResource("valid-eureka-registration.properties");
        DependencyGraph graph = parser.parse(content);

        List<Edge> eurekaEdges = graph.getEdgesByType(EdgeType.EUREKA_REGISTER);
        assertThat(eurekaEdges).hasSize(1);
        assertThat(eurekaEdges.get(0).getSourceId()).isEqualTo("customer-service");
        assertThat(eurekaEdges.get(0).getTargetId()).isEqualTo("discovery-service");
    }

    @Test
    void parseValidProperties_extractsJdbcConnection() {
        String content = loadResource("valid-eureka-registration.properties");
        DependencyGraph graph = parser.parse(content);

        List<Edge> jdbcEdges = graph.getEdgesByType(EdgeType.JDBC);
        assertThat(jdbcEdges).hasSize(1);
        assertThat(jdbcEdges.get(0).getSourceId()).isEqualTo("customer-service");

        Node dbNode = graph.getNodesByType(NodeType.DATABASE).get(0);
        assertThat(dbNode).isNotNull();
    }

    @Test
    void parseWithAxon_extractsAxonConnection() {
        String content = loadResource("valid-eureka-with-axon.properties");
        DependencyGraph graph = parser.parse(content);

        List<Edge> axonEdges = graph.getEdgesByType(EdgeType.AXON);
        assertThat(axonEdges).hasSize(1);
        assertThat(axonEdges.get(0).getSourceId()).isEqualTo("account-service");
        assertThat(axonEdges.get(0).getTargetId()).isEqualTo("axon-server");

        Node axonNode = graph.findNode("axon-server").orElseThrow();
        assertThat(axonNode.getType()).isEqualTo(NodeType.EVENT_STORE);
    }

    @Test
    void parseNotificationService_extractsMailConnection() {
        String content = loadResource("valid-eureka-notification.properties");
        DependencyGraph graph = parser.parse(content);

        List<Edge> smtpEdges = graph.getEdgesByType(EdgeType.SMTP);
        assertThat(smtpEdges).hasSize(1);
        assertThat(smtpEdges.get(0).getSourceId()).isEqualTo("notification-service");
        assertThat(smtpEdges.get(0).getTargetId()).isEqualTo("mail-server");
    }

    @Test
    void parseYamlFormat_worksCorrectly() {
        String content = loadResource("valid-gateway-discovery.yml");
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.findNode("gateway-service")).isPresent();
        Node gateway = graph.findNode("gateway-service").orElseThrow();
        assertThat(gateway.getType()).isEqualTo(NodeType.GATEWAY);
    }

    @Test
    void parseNullContent_throwsException() {
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseBlankContent_throwsException() {
        assertThatThrownBy(() -> parser.parse(""))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("must not be null or blank");
    }

    @Test
    void parseMissingAppName_throwsException() {
        String content = """
                server.port=8080
                eureka.client.service-url.defaultZone=http://localhost:8761/eureka
                """;
        assertThatThrownBy(() -> parser.parse(content))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("spring.application.name");
    }

    @Test
    void parseMalformedProperties_missingAppName_throwsException() {
        String content = """
                # some comment
                server.port=8080
                some.random.key=value
                """;
        assertThatThrownBy(() -> parser.parse(content))
                .isInstanceOf(GraphParseException.class)
                .hasMessageContaining("spring.application.name");
    }

    @Test
    void parsePropertiesWithNoEureka_producesServiceNodeOnly() {
        String content = """
                spring.application.name=STANDALONE-SERVICE
                server.port=9090
                """;
        DependencyGraph graph = parser.parse(content);

        assertThat(graph.findNode("standalone-service")).isPresent();
        assertThat(graph.getEdgesByType(EdgeType.EUREKA_REGISTER)).isEmpty();
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
