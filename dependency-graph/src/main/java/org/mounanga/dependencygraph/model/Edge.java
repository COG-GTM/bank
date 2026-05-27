package org.mounanga.dependencygraph.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Edge {

    private final String sourceId;
    private final String targetId;
    private final EdgeType type;
    private final Map<String, String> metadata;

    public Edge(String sourceId, String targetId, EdgeType type, Map<String, String> metadata) {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("Edge sourceId must not be null or blank");
        }
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("Edge targetId must not be null or blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("Edge type must not be null");
        }
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.type = type;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
    }

    public Edge(String sourceId, String targetId, EdgeType type) {
        this(sourceId, targetId, type, null);
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

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
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
        return "Edge{source='%s', target='%s', type=%s}".formatted(sourceId, targetId, type);
    }
}
