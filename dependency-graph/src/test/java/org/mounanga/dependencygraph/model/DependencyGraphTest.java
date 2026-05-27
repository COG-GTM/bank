package org.mounanga.dependencygraph.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class DependencyGraphTest {

    @Test
    void addNode_retrievableById() {
        DependencyGraph graph = new DependencyGraph();
        Node node = new Node("svc-1", "Service 1", NodeType.SERVICE);
        graph.addNode(node);

        assertThat(graph.findNode("svc-1")).isPresent();
        assertThat(graph.findNode("svc-1").get().getLabel()).isEqualTo("Service 1");
    }

    @Test
    void addEdge_retrievable() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new Node("a", "A", NodeType.SERVICE));
        graph.addNode(new Node("b", "B", NodeType.DATABASE));
        graph.addEdge(new Edge("a", "b", EdgeType.JDBC));

        assertThat(graph.getEdges()).hasSize(1);
        assertThat(graph.getOutgoingEdges("a")).hasSize(1);
        assertThat(graph.getIncomingEdges("b")).hasSize(1);
    }

    @Test
    void getNodesByType_filtersCorrectly() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new Node("svc", "Service", NodeType.SERVICE));
        graph.addNode(new Node("db", "DB", NodeType.DATABASE));
        graph.addNode(new Node("gw", "Gateway", NodeType.GATEWAY));

        assertThat(graph.getNodesByType(NodeType.SERVICE)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.DATABASE)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.GATEWAY)).hasSize(1);
        assertThat(graph.getNodesByType(NodeType.MESSAGE_BROKER)).isEmpty();
    }

    @Test
    void getEdgesByType_filtersCorrectly() {
        DependencyGraph graph = new DependencyGraph();
        graph.addEdge(new Edge("a", "b", EdgeType.HTTP));
        graph.addEdge(new Edge("a", "c", EdgeType.JDBC));
        graph.addEdge(new Edge("a", "d", EdgeType.HTTP));

        assertThat(graph.getEdgesByType(EdgeType.HTTP)).hasSize(2);
        assertThat(graph.getEdgesByType(EdgeType.JDBC)).hasSize(1);
        assertThat(graph.getEdgesByType(EdgeType.AMQP)).isEmpty();
    }

    @Test
    void merge_combinesGraphs() {
        DependencyGraph g1 = new DependencyGraph();
        g1.addNode(new Node("a", "A", NodeType.SERVICE));
        g1.addEdge(new Edge("a", "b", EdgeType.HTTP));

        DependencyGraph g2 = new DependencyGraph();
        g2.addNode(new Node("b", "B", NodeType.DATABASE));
        g2.addEdge(new Edge("b", "c", EdgeType.JDBC));

        g1.merge(g2);

        assertThat(g1.getNodes()).hasSize(2);
        assertThat(g1.getEdges()).hasSize(2);
        assertThat(g1.findNode("b")).isPresent();
    }

    @Test
    void merge_nullGraph_noException() {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode(new Node("a", "A", NodeType.SERVICE));
        graph.merge(null);

        assertThat(graph.getNodes()).hasSize(1);
    }

    @Test
    void nodeEquality_basedOnId() {
        Node n1 = new Node("svc", "Service", NodeType.SERVICE);
        Node n2 = new Node("svc", "Different Label", NodeType.DATABASE);
        assertThat(n1).isEqualTo(n2);
    }

    @Test
    void edgeEquality_basedOnSourceTargetType() {
        Edge e1 = new Edge("a", "b", EdgeType.HTTP);
        Edge e2 = new Edge("a", "b", EdgeType.HTTP, Map.of("key", "val"));
        assertThat(e1).isEqualTo(e2);

        Edge e3 = new Edge("a", "b", EdgeType.JDBC);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    void nodeMetadata_isUnmodifiable() {
        Node node = new Node("svc", "Service", NodeType.SERVICE, Map.of("key", "val"));
        assertThatThrownBy(() -> node.getMetadata().put("new", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void edgeMetadata_isUnmodifiable() {
        Edge edge = new Edge("a", "b", EdgeType.HTTP, Map.of("key", "val"));
        assertThatThrownBy(() -> edge.getMetadata().put("new", "val"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void nodeWithNullId_throwsException() {
        assertThatThrownBy(() -> new Node(null, "Label", NodeType.SERVICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nodeWithBlankId_throwsException() {
        assertThatThrownBy(() -> new Node("  ", "Label", NodeType.SERVICE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nodeWithNullType_throwsException() {
        assertThatThrownBy(() -> new Node("id", "Label", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void edgeWithNullSourceId_throwsException() {
        assertThatThrownBy(() -> new Edge(null, "b", EdgeType.HTTP))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void edgeWithNullTargetId_throwsException() {
        assertThatThrownBy(() -> new Edge("a", null, EdgeType.HTTP))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void edgeWithNullType_throwsException() {
        assertThatThrownBy(() -> new Edge("a", "b", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addNullNode_throwsException() {
        DependencyGraph graph = new DependencyGraph();
        assertThatThrownBy(() -> graph.addNode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addNullEdge_throwsException() {
        DependencyGraph graph = new DependencyGraph();
        assertThatThrownBy(() -> graph.addEdge(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
