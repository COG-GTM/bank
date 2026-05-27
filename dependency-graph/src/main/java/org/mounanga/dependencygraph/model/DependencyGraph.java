package org.mounanga.dependencygraph.model;

import java.util.*;
import java.util.stream.Collectors;

public final class DependencyGraph {

    private final Map<String, Node> nodes = new LinkedHashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    public Optional<Node> getNode(String id) {
        return Optional.ofNullable(nodes.get(id));
    }

    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodes.values());
    }

    public List<Edge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    public Set<Node> getNodesByType(NodeType type) {
        return nodes.values().stream()
                .filter(n -> n.getType() == type)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<Edge> getOutgoingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getSourceId().equals(nodeId))
                .toList();
    }

    public List<Edge> getIncomingEdges(String nodeId) {
        return edges.stream()
                .filter(e -> e.getTargetId().equals(nodeId))
                .toList();
    }

    /**
     * Returns all nodes that directly depend on the given node (i.e. have an edge targeting it).
     */
    public Set<Node> getDirectDependents(String nodeId) {
        return getIncomingEdges(nodeId).stream()
                .map(e -> nodes.get(e.getSourceId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Returns all nodes that the given node directly depends on.
     */
    public Set<Node> getDirectDependencies(String nodeId) {
        return getOutgoingEdges(nodeId).stream()
                .map(e -> nodes.get(e.getTargetId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Computes the full blast radius: all nodes transitively affected if {@code nodeId} goes down.
     */
    public Set<Node> getBlastRadius(String nodeId) {
        Set<Node> affected = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(nodeId);
        Set<String> visited = new HashSet<>();
        visited.add(nodeId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Node dependent : getDirectDependents(current)) {
                if (visited.add(dependent.getId())) {
                    affected.add(dependent);
                    queue.add(dependent.getId());
                }
            }
        }
        return affected;
    }

    /**
     * Traces the dependency chain from {@code fromId} to {@code toId} using BFS.
     * Returns the ordered list of edges forming the shortest path, or empty if no path exists.
     */
    public List<Edge> traceDependencyChain(String fromId, String toId) {
        if (fromId.equals(toId)) return List.of();

        Map<String, Edge> parentEdge = new LinkedHashMap<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(fromId);
        Set<String> visited = new HashSet<>();
        visited.add(fromId);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (Edge edge : getOutgoingEdges(current)) {
                String target = edge.getTargetId();
                if (visited.add(target)) {
                    parentEdge.put(target, edge);
                    if (target.equals(toId)) {
                        return reconstructPath(parentEdge, fromId, toId);
                    }
                    queue.add(target);
                }
            }
        }
        return List.of();
    }

    private List<Edge> reconstructPath(Map<String, Edge> parentEdge, String from, String to) {
        List<Edge> path = new ArrayList<>();
        String current = to;
        while (!current.equals(from)) {
            Edge edge = parentEdge.get(current);
            path.add(edge);
            current = edge.getSourceId();
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("DependencyGraph{\n  nodes=[\n");
        nodes.values().forEach(n -> sb.append("    ").append(n).append("\n"));
        sb.append("  ],\n  edges=[\n");
        edges.forEach(e -> sb.append("    ").append(e).append("\n"));
        sb.append("  ]\n}");
        return sb.toString();
    }
}
