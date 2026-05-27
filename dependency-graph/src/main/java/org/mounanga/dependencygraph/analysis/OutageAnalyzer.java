package org.mounanga.dependencygraph.analysis;

import org.mounanga.dependencygraph.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes dependency graphs to investigate outage scenarios: traces dependency chains,
 * computes blast radius, and generates evidence-based investigation reports.
 */
public class OutageAnalyzer {

    public record OutageReport(
            String failingService,
            String rootCause,
            List<Edge> dependencyChain,
            Set<Node> blastRadius,
            List<String> evidence,
            List<String> resilienceRecommendations
    ) {

        public String toMarkdown() {
            StringBuilder sb = new StringBuilder();
            sb.append("# Outage Investigation Report\n\n");

            sb.append("## Failing Service\n");
            sb.append("**%s**\n\n".formatted(failingService));

            sb.append("## Root Cause\n");
            sb.append("%s\n\n".formatted(rootCause));

            sb.append("## Dependency Chain\n");
            if (dependencyChain.isEmpty()) {
                sb.append("No direct dependency chain found.\n\n");
            } else {
                sb.append("```\n");
                for (int i = 0; i < dependencyChain.size(); i++) {
                    Edge edge = dependencyChain.get(i);
                    if (i == 0) sb.append(edge.getSourceId());
                    sb.append(" --[%s: %s]--> %s".formatted(edge.getType(), edge.getLabel(), edge.getTargetId()));
                    if (i < dependencyChain.size() - 1) sb.append("\n");
                }
                sb.append("\n```\n\n");
            }

            sb.append("## Blast Radius\n");
            sb.append("If **%s** goes down, the following %d service(s) are affected:\n\n".formatted(
                    rootCause, blastRadius.size()));
            for (Node node : blastRadius) {
                sb.append("- **%s** (%s)\n".formatted(node.getName(), node.getType()));
            }
            sb.append("\n");

            sb.append("## Evidence\n");
            for (int i = 0; i < evidence.size(); i++) {
                sb.append("%d. %s\n".formatted(i + 1, evidence.get(i)));
            }
            sb.append("\n");

            sb.append("## Resilience Recommendations\n");
            for (int i = 0; i < resilienceRecommendations.size(); i++) {
                sb.append("%d. %s\n".formatted(i + 1, resilienceRecommendations.get(i)));
            }

            return sb.toString();
        }
    }

    public OutageReport analyzeOutage(DependencyGraph graph, String failingServiceId,
                                      String unreachableServiceId) {
        List<Edge> chain = graph.traceDependencyChain(failingServiceId, unreachableServiceId);
        Set<Node> blastRadius = graph.getBlastRadius(unreachableServiceId);
        List<String> evidence = gatherEvidence(graph, failingServiceId, unreachableServiceId, chain);
        List<String> recommendations = generateRecommendations(graph, failingServiceId,
                unreachableServiceId, chain);

        return new OutageReport(
                failingServiceId,
                unreachableServiceId,
                chain,
                blastRadius,
                evidence,
                recommendations
        );
    }

    private List<String> gatherEvidence(DependencyGraph graph, String failingId,
                                        String unreachableId, List<Edge> chain) {
        List<String> evidence = new ArrayList<>();

        Optional<Node> failingNode = graph.getNode(failingId);
        Optional<Node> unreachableNode = graph.getNode(unreachableId);

        if (failingNode.isPresent()) {
            evidence.add("Service '%s' (type: %s) is present in the dependency graph."
                    .formatted(failingNode.get().getName(), failingNode.get().getType()));
        }

        if (unreachableNode.isPresent()) {
            evidence.add("Service '%s' (type: %s) is present in the dependency graph."
                    .formatted(unreachableNode.get().getName(), unreachableNode.get().getType()));
        }

        if (!chain.isEmpty()) {
            String chainStr = chain.stream()
                    .map(e -> "%s -[%s]-> %s".formatted(e.getSourceId(), e.getType(), e.getTargetId()))
                    .collect(Collectors.joining(" → "));
            evidence.add("Dependency chain exists: %s".formatted(chainStr));
        }

        List<Edge> eurekaEdges = graph.getEdges().stream()
                .filter(e -> e.getType() == EdgeType.EUREKA && e.getTargetId().equals(unreachableId))
                .toList();
        if (!eurekaEdges.isEmpty()) {
            String registrants = eurekaEdges.stream()
                    .map(Edge::getSourceId)
                    .collect(Collectors.joining(", "));
            evidence.add("Services registered via Eureka with '%s': [%s]"
                    .formatted(unreachableId, registrants));
        }

        List<Edge> dependsOnEdges = graph.getEdges().stream()
                .filter(e -> e.getTargetId().equals(unreachableId) && "depends_on".equals(e.getLabel()))
                .toList();
        if (!dependsOnEdges.isEmpty()) {
            String dependents = dependsOnEdges.stream()
                    .map(Edge::getSourceId)
                    .collect(Collectors.joining(", "));
            evidence.add("Docker Compose depends_on references to '%s': [%s]"
                    .formatted(unreachableId, dependents));
        }

        Set<Node> blastRadius = graph.getBlastRadius(unreachableId);
        Set<Node> services = blastRadius.stream()
                .filter(n -> n.getType() == NodeType.SERVICE)
                .collect(Collectors.toSet());
        evidence.add("Blast radius: %d service(s) transitively depend on '%s'."
                .formatted(services.size(), unreachableId));

        return evidence;
    }

    private List<String> generateRecommendations(DependencyGraph graph, String failingId,
                                                 String unreachableId, List<Edge> chain) {
        List<String> recommendations = new ArrayList<>();

        boolean hasEurekaEdge = chain.stream().anyMatch(e -> e.getType() == EdgeType.EUREKA);
        if (hasEurekaEdge) {
            recommendations.add(
                    "Add Eureka client retry and failover configuration to %s: "
                            .formatted(failingId)
                            + "set `eureka.client.eureka-connection-idle-timeout-seconds`, "
                            + "`eureka.client.initial-instance-info-replication-interval-seconds`, "
                            + "and configure multiple Eureka server URLs for high availability.");
        }

        recommendations.add(
                "Add a circuit breaker (e.g. Resilience4j) around service discovery calls "
                        + "so that %s can degrade gracefully when %s is temporarily unreachable."
                        .formatted(failingId, unreachableId));

        recommendations.add(
                "Enable Eureka client caching: set `eureka.client.registry-fetch-interval-seconds=5` "
                        + "and `eureka.client.disable-delta=false` so services retain a local "
                        + "registry cache and can continue routing even if discovery is briefly down.");

        recommendations.add(
                "Configure `eureka.instance.lease-renewal-interval-in-seconds=10` and "
                        + "`eureka.instance.lease-expiration-duration-in-seconds=30` on all services "
                        + "to extend the window before instances are evicted during a discovery outage.");

        recommendations.add(
                "Run multiple discovery-service replicas behind a load balancer and configure "
                        + "`eureka.client.service-url.defaultZone` with comma-separated URLs "
                        + "(e.g. `http://eureka1:8761/eureka/,http://eureka2:8762/eureka/`) "
                        + "for high-availability peer replication.");

        recommendations.add(
                "Add health checks with retries to the Docker Compose `depends_on` conditions "
                        + "(using `condition: service_healthy`) so downstream services wait until "
                        + "discovery-service is fully ready before starting.");

        return recommendations;
    }
}
