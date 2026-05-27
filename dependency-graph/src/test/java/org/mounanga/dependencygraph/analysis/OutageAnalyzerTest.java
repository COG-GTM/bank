package org.mounanga.dependencygraph.analysis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mounanga.dependencygraph.model.*;

import static org.junit.jupiter.api.Assertions.*;

class OutageAnalyzerTest {

    private OutageAnalyzer analyzer;
    private DependencyGraph graph;

    @BeforeEach
    void setUp() {
        analyzer = new OutageAnalyzer();
        graph = buildBankGraph();
    }

    @Test
    void analyzeOutage_notificationToDiscovery_findsChain() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "notification-service", "discovery-service");

        assertFalse(report.dependencyChain().isEmpty(),
                "Should find dependency chain from notification-service to discovery-service");
        assertEquals("notification-service", report.dependencyChain().get(0).getSourceId());
    }

    @Test
    void analyzeOutage_discoveryDown_blastRadiusIncludesAllServices() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "notification-service", "discovery-service");

        assertTrue(report.blastRadius().size() >= 4,
                "Discovery outage should affect at least 4 services");

        boolean affectsGateway = report.blastRadius().stream()
                .anyMatch(n -> n.getId().equals("gateway-service"));
        assertTrue(affectsGateway, "Gateway should be in blast radius");

        boolean affectsNotification = report.blastRadius().stream()
                .anyMatch(n -> n.getId().equals("notification-service"));
        assertTrue(affectsNotification, "Notification should be in blast radius");
    }

    @Test
    void analyzeOutage_hasEvidence() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "notification-service", "discovery-service");

        assertFalse(report.evidence().isEmpty());
        assertTrue(report.evidence().stream()
                .anyMatch(e -> e.contains("discovery-service")));
    }

    @Test
    void analyzeOutage_hasRecommendations() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "notification-service", "discovery-service");

        assertFalse(report.resilienceRecommendations().isEmpty());
        assertTrue(report.resilienceRecommendations().stream()
                .anyMatch(r -> r.toLowerCase().contains("circuit breaker")
                        || r.toLowerCase().contains("resilience")));
    }

    @Test
    void analyzeOutage_markdownReportIsNonEmpty() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "notification-service", "discovery-service");

        String md = report.toMarkdown();
        assertFalse(md.isBlank());
        assertTrue(md.contains("# Outage Investigation Report"));
        assertTrue(md.contains("notification-service"));
        assertTrue(md.contains("discovery-service"));
    }

    @Test
    void analyzeOutage_noPathBetweenDisconnectedNodes() {
        DependencyGraph isolated = new DependencyGraph();
        isolated.addNode(new Node("a", "A", NodeType.SERVICE));
        isolated.addNode(new Node("b", "B", NodeType.SERVICE));

        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(isolated, "a", "b");
        assertTrue(report.dependencyChain().isEmpty());
    }

    @Test
    void blastRadius_leafServiceDown_minimalImpact() {
        OutageAnalyzer.OutageReport report = analyzer.analyzeOutage(
                graph, "account-service", "account-service");

        // account-service is a leaf; nothing depends on it transitively in the base graph
        // (customer-service does not depend on account-service)
        assertTrue(report.blastRadius().isEmpty() || report.blastRadius().size() <= 1);
    }

    private DependencyGraph buildBankGraph() {
        DependencyGraph g = new DependencyGraph();

        g.addNode(new Node("discovery-service", "discovery-service", NodeType.SERVICE));
        g.addNode(new Node("gateway-service", "gateway-service", NodeType.SERVICE));
        g.addNode(new Node("notification-service", "notification-service", NodeType.SERVICE));
        g.addNode(new Node("authentication-service", "authentication-service", NodeType.SERVICE));
        g.addNode(new Node("customer-service", "customer-service", NodeType.SERVICE));
        g.addNode(new Node("account-service", "account-service", NodeType.SERVICE));

        // depends_on edges from docker-compose
        g.addEdge(new Edge("gateway-service", "discovery-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("notification-service", "discovery-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("notification-service", "gateway-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("authentication-service", "discovery-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("authentication-service", "gateway-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("authentication-service", "notification-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("customer-service", "discovery-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("customer-service", "gateway-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("customer-service", "notification-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("account-service", "discovery-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("account-service", "gateway-service", EdgeType.HTTP, "depends_on"));
        g.addEdge(new Edge("account-service", "customer-service", EdgeType.HTTP, "depends_on"));

        // Eureka registration edges
        g.addEdge(new Edge("gateway-service", "discovery-service", EdgeType.EUREKA, "registers with"));
        g.addEdge(new Edge("notification-service", "discovery-service", EdgeType.EUREKA, "registers with"));
        g.addEdge(new Edge("authentication-service", "discovery-service", EdgeType.EUREKA, "registers with"));
        g.addEdge(new Edge("customer-service", "discovery-service", EdgeType.EUREKA, "registers with"));
        g.addEdge(new Edge("account-service", "discovery-service", EdgeType.EUREKA, "registers with"));

        return g;
    }
}
