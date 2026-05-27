package org.mounanga.dependencygraph.model;

import java.util.*;
import java.util.stream.Collectors;

public final class DependencyGraph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null");
        }
        nodes.put(node.getId(), node);
    }

    public void addEdge(Edge edge) {
        if (edge == null) {
            throw new IllegalArgumentException("Edge must not be null");
        }
        edges.add(edge);
    }

    public Optional<Node> findNode(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Node> getNodesByType(NodeType type) {
        return nodes.values().stream()
                .filter(n -> n.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Edge> getEdgesByType(EdgeType type) {
        return edges.stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    public List<Edge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getSourceId().equals(nodeId))
                .collect(Collectors.toList());
    }

    public List<Edge> getIncomingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getTargetId().equals(nodeId))
                .collect(Collectors.toList());
    }

    public void merge(DependencyGraph other) {
        if (other == null) return;
        other.getNodes().forEach(this::addNode);
        other.getEdges().forEach(this::addEdge);
    }

    @Override
    public String toString() {
        return "DependencyGraph{nodes=%d, edges=%d}".formatted(nodes.size(), edges.size());
    }
}
