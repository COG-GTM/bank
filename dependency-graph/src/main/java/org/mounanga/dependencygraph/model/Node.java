package org.mounanga.dependencygraph.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Node {

    private final String id;
    private final String label;
    private final NodeType type;
    private final Map<String, String> metadata;

    public Node(String id, String label, NodeType type, Map<String, String> metadata) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Node id must not be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Node type must not be null");
        }
        this.id = id;
        this.label = label != null ? label : id;
        this.type = type;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public Node(String id, String label, NodeType type) {
        this(id, label, type, null);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public NodeType getType() {
        return type;
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Node{id='%s', label='%s', type=%s}".formatted(id, label, type);
    }
}
