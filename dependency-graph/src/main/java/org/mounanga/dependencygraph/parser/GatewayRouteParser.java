package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.*;

/**
 * Parses Spring Cloud Gateway route configuration from application.yml to extract
 * gateway-to-service routing edges. Handles both explicit route definitions and
 * discovery-first (Eureka-locator) routing.
 */
public class GatewayRouteParser {

    public record GatewayRoute(String id, String targetService, String predicatePath) {
    }

    public List<GatewayRoute> parse(Reader reader) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(reader);
        if (root == null) {
            throw new IllegalArgumentException("Gateway configuration is empty or invalid");
        }
        return extractRoutes(root);
    }

    public void addToGraph(DependencyGraph graph, String gatewayNodeId, List<GatewayRoute> routes) {
        if (graph.getNode(gatewayNodeId).isEmpty()) {
            graph.addNode(new Node(gatewayNodeId, gatewayNodeId, NodeType.SERVICE));
        }
        for (GatewayRoute route : routes) {
            String targetId = normalizeServiceName(route.targetService());
            if (graph.getNode(targetId).isEmpty()) {
                graph.addNode(new Node(targetId, route.targetService(), NodeType.SERVICE));
            }
            graph.addEdge(new Edge(gatewayNodeId, targetId, EdgeType.HTTP,
                    "route: %s".formatted(route.predicatePath() != null ? route.predicatePath() : route.id())));
        }
    }

    @SuppressWarnings("unchecked")
    private List<GatewayRoute> extractRoutes(Map<String, Object> root) {
        List<GatewayRoute> routes = new ArrayList<>();

        Map<String, Object> spring = getNestedMap(root, "spring");
        if (spring == null) return routes;

        Map<String, Object> cloud = getNestedMap(spring, "cloud");
        if (cloud == null) return routes;

        Map<String, Object> gateway = getNestedMap(cloud, "gateway");
        if (gateway == null) return routes;

        // Check for discovery-locator enabled (discovery-first routing)
        Map<String, Object> discovery = getNestedMap(gateway, "discovery");
        if (discovery != null) {
            Map<String, Object> locator = getNestedMap(discovery, "locator");
            if (locator != null) {
                Object enabled = locator.get("enabled");
                if (Boolean.TRUE.equals(enabled) || "true".equalsIgnoreCase(String.valueOf(enabled))) {
                    routes.add(new GatewayRoute("discovery-locator", "*", "/**"));
                }
            }
        }

        // Check for explicit routes
        Object routesObj = gateway.get("routes");
        if (routesObj instanceof List<?> routesList) {
            for (Object routeObj : routesList) {
                if (routeObj instanceof Map<?, ?> routeMap) {
                    parseExplicitRoute((Map<String, Object>) routeMap, routes);
                }
            }
        }

        // Check if discovery is enabled and gateway uses Eureka (discovery-first routing)
        // This is inferred from the presence of eureka config at root level
        boolean discoveryEnabled = isDiscoveryEnabled(root);
        boolean hasExplicitRoutes = routes.stream()
                .anyMatch(r -> !r.id().equals("discovery-locator"));

        if (discoveryEnabled && !hasExplicitRoutes && routes.isEmpty()) {
            routes.add(new GatewayRoute("discovery-first", "*", "/**"));
        }

        return routes;
    }

    @SuppressWarnings("unchecked")
    private void parseExplicitRoute(Map<String, Object> routeMap, List<GatewayRoute> routes) {
        String id = routeMap.containsKey("id") ? routeMap.get("id").toString() : "unknown";
        String uri = routeMap.containsKey("uri") ? routeMap.get("uri").toString() : "";

        String targetService = extractServiceFromUri(uri);
        if (targetService == null) return;

        String predicatePath = null;
        Object predicates = routeMap.get("predicates");
        if (predicates instanceof List<?> predList) {
            for (Object pred : predList) {
                String predStr = pred.toString();
                if (predStr.startsWith("Path=")) {
                    predicatePath = predStr.substring(5);
                }
            }
        }

        routes.add(new GatewayRoute(id, targetService, predicatePath));
    }

    private String extractServiceFromUri(String uri) {
        if (uri.startsWith("lb://")) {
            return uri.substring(5);
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            try {
                java.net.URI parsed = java.net.URI.create(uri);
                return parsed.getHost();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private boolean isDiscoveryEnabled(Map<String, Object> root) {
        Map<String, Object> eureka = getNestedMap(root, "eureka");
        if (eureka != null) return true;

        Map<String, Object> spring = getNestedMap(root, "spring");
        if (spring != null) {
            Map<String, Object> cloud = getNestedMap(spring, "cloud");
            if (cloud != null) {
                Map<String, Object> disc = getNestedMap(cloud, "discovery");
                if (disc != null) {
                    Object enabled = disc.get("enabled");
                    return enabled == null || Boolean.TRUE.equals(enabled)
                            || "true".equalsIgnoreCase(String.valueOf(enabled));
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Map<?, ?> nested) {
            return (Map<String, Object>) nested;
        }
        return null;
    }

    private String normalizeServiceName(String name) {
        return name.toLowerCase().replace('_', '-');
    }
}
