package org.mounanga.dependencygraph.model;

import java.util.Objects;

public final class Edge {

    private final String sourceId;
    private final String targetId;
    private final EdgeType type;
    private final String label;

    public Edge(String sourceId, String targetId, EdgeType type, String label) {
        this.sourceId = Objects.requireNonNull(sourceId, "sourceId must not be null");
        this.targetId = Objects.requireNonNull(targetId, "targetId must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.label = label != null ? label : "";
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public EdgeType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge other)) return false;
        return sourceId.equals(other.sourceId)
                && targetId.equals(other.targetId)
                && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, targetId, type);
    }

    @Override
    public String toString() {
        return "Edge{%s -[%s]-> %s, label='%s'}".formatted(sourceId, type, targetId, label);
    }
}
