package org.mounanga.dependencygraph.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import java.io.*;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DockerComposeParserTest {

    private DockerComposeParser parser;

    @BeforeEach
    void setUp() {
        parser = new DockerComposeParser();
    }

    @Test
    void parseValidCompose_extractsAllServices() {
        DependencyGraph graph = parser.parse(readerFor("/valid-docker-compose.yml"));
        Set<String> nodeIds = graph.getNodes().stream().map(Node::getId).collect(Collectors.toSet());

        assertTrue(nodeIds.contains("discovery-service"));
        assertTrue(nodeIds.contains("gateway-service"));
        assertTrue(nodeIds.contains("notification-service"));
        assertTrue(nodeIds.contains("customer-service"));
        assertTrue(nodeIds.contains("account-service"));
    }

    @Test
    void parseValidCompose_allServicesAreServiceType() {
        DependencyGraph graph = parser.parse(readerFor("/valid-docker-compose.yml"));

        for (Node node : graph.getNodes()) {
            if (!node.getId().startsWith("db-")) {
                assertEquals(NodeType.SERVICE, node.getType(),
                        "Expected SERVICE type for " + node.getId());
            }
        }
    }

    @Test
    void parseValidCompose_dependsOnCreatesEdges() {
        DependencyGraph graph = parser.parse(readerFor("/valid-docker-compose.yml"));

        boolean gatewayDependsOnDiscovery = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("gateway-service")
                        && e.getTargetId().equals("discovery-service"));
        assertTrue(gatewayDependsOnDiscovery);

        boolean notificationDependsOnDiscovery = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("notification-service")
                        && e.getTargetId().equals("discovery-service"));
        assertTrue(notificationDependsOnDiscovery);
    }

    @Test
    void parseValidCompose_mysqlEnvCreatesJdbcEdge() {
        DependencyGraph graph = parser.parse(readerFor("/valid-docker-compose.yml"));

        boolean customerHasJdbc = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("customer-service")
                        && e.getType() == EdgeType.JDBC);
        assertTrue(customerHasJdbc, "customer-service should have a JDBC edge");

        boolean accountHasJdbc = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("account-service")
                        && e.getType() == EdgeType.JDBC);
        assertTrue(accountHasJdbc, "account-service should have a JDBC edge");
    }

    @Test
    void parseComposeWithBroker_classifiesNodesCorrectly() {
        DependencyGraph graph = parser.parse(readerFor("/docker-compose-with-broker.yml"));

        Node rabbitmq = graph.getNode("rabbitmq").orElseThrow();
        assertEquals(NodeType.MESSAGE_BROKER, rabbitmq.getType());

        Node mysql = graph.getNode("mysql-db").orElseThrow();
        assertEquals(NodeType.DATABASE, mysql.getType());

        Node app = graph.getNode("app-service").orElseThrow();
        assertEquals(NodeType.SERVICE, app.getType());
    }

    @Test
    void parseComposeWithBroker_createsAmqpAndJdbcEdges() {
        DependencyGraph graph = parser.parse(readerFor("/docker-compose-with-broker.yml"));

        boolean hasAmqp = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("app-service")
                        && e.getType() == EdgeType.AMQP);
        assertTrue(hasAmqp, "app-service should have an AMQP edge to rabbitmq");

        boolean hasJdbc = graph.getEdges().stream()
                .anyMatch(e -> e.getSourceId().equals("app-service")
                        && e.getType() == EdgeType.JDBC);
        assertTrue(hasJdbc, "app-service should have a JDBC edge to mysql-db");
    }

    @Test
    void parseMalformedCompose_missingServicesKey_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(readerFor("/malformed-docker-compose.yml")));
    }

    @Test
    void parseEmptyCompose_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(readerFor("/empty-docker-compose.yml")));
    }

    @Test
    void parseMalformedServices_notAMap_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(readerFor("/malformed-services-docker-compose.yml")));
    }

    private Reader readerFor(String resource) {
        InputStream is = getClass().getResourceAsStream(resource);
        assertNotNull(is, "Test resource not found: " + resource);
        return new InputStreamReader(is);
    }
}
