package org.mounanga.dependencygraph.model;

import java.util.Objects;

public final class Node {

    private final String id;
    private final String name;
    private final NodeType type;

    public Node(String id, String name, NodeType type) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Node{id='%s', name='%s', type=%s}".formatted(id, name, type);
    }
}
