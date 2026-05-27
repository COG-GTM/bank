package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public class GatewayRouteParser implements GraphParser {

    @Override
    public DependencyGraph parse(String content) throws GraphParseException {
        if (content == null || content.isBlank()) {
            throw new GraphParseException("Gateway route content must not be null or blank");
        }

        Map<String, Object> root;
        try {
            root = new Yaml().load(content);
        } catch (Exception e) {
            throw new GraphParseException("Failed to parse Gateway YAML configuration", e);
        }

        if (root == null) {
            throw new GraphParseException("Gateway YAML configuration is empty");
        }

        DependencyGraph graph = new DependencyGraph();

        String appName = extractNestedString(root, "spring", "application", "name");
        String gatewayId = appName != null ? appName.toLowerCase() : "gateway-service";

        Map<String, String> gatewayMeta = new LinkedHashMap<>();
        String port = extractNestedString(root, "server", "port");
        if (port != null) gatewayMeta.put("port", port);
        if (appName != null) gatewayMeta.put("applicationName", appName);

        boolean discoveryEnabled = isDiscoveryEnabled(root);
        List<Map<String, Object>> routes = extractRoutes(root);

        if (discoveryEnabled && routes.isEmpty()) {
            gatewayMeta.put("routingMode", "discovery-first");
        }

        graph.addNode(new Node(gatewayId, appName != null ? appName : "Gateway Service", NodeType.GATEWAY, gatewayMeta));

        if (discoveryEnabled) {
            String eurekaUrl = extractNestedString(root, "eureka", "client", "service-url", "defaultZone");
            String discoveryId = "discovery-service";
            Map<String, String> discoveryMeta = new LinkedHashMap<>();
            if (eurekaUrl != null) discoveryMeta.put("url", eurekaUrl);
            graph.addNode(new Node(discoveryId, "Discovery Service", NodeType.DISCOVERY, discoveryMeta));
            graph.addEdge(new Edge(gatewayId, discoveryId, EdgeType.EUREKA_REGISTER));
        }

        for (Map<String, Object> route : routes) {
            processRoute(graph, gatewayId, route);
        }

        return graph;
    }

    private void processRoute(DependencyGraph graph, String gatewayId, Map<String, Object> route) {
        String routeId = stringVal(route, "id");
        String uri = stringVal(route, "uri");
        if (uri == null) return;

        String targetServiceId = extractServiceFromUri(uri);
        if (targetServiceId == null) return;

        if (graph.findNode(targetServiceId).isEmpty()) {
            graph.addNode(new Node(targetServiceId, targetServiceId, NodeType.SERVICE));
        }

        Map<String, String> edgeMeta = new LinkedHashMap<>();
        edgeMeta.put("uri", uri);
        if (routeId != null) edgeMeta.put("routeId", routeId);

        List<String> predicates = extractPredicates(route);
        if (!predicates.isEmpty()) {
            edgeMeta.put("predicates", String.join("; ", predicates));
        }

        graph.addEdge(new Edge(gatewayId, targetServiceId, EdgeType.GATEWAY_ROUTE, edgeMeta));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractRoutes(Map<String, Object> root) {
        Object gateway = extractNested(root, "spring", "cloud", "gateway");
        if (!(gateway instanceof Map<?, ?> gatewayMap)) return Collections.emptyList();

        Object routes = gatewayMap.get("routes");
        if (!(routes instanceof List<?> routeList)) return Collections.emptyList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object r : routeList) {
            if (r instanceof Map<?, ?> routeMap) {
                result.add((Map<String, Object>) routeMap);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractPredicates(Map<String, Object> route) {
        Object predicates = route.get("predicates");
        if (predicates instanceof List<?> predList) {
            return predList.stream().map(p -> {
                if (p instanceof String s) return s;
                if (p instanceof Map<?, ?> m) return m.toString();
                return String.valueOf(p);
            }).toList();
        }
        return Collections.emptyList();
    }

    private String extractServiceFromUri(String uri) {
        if (uri.startsWith("lb://")) {
            return uri.substring(5).toLowerCase();
        }
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            String hostPart = uri.substring(uri.indexOf("//") + 2);
            int colon = hostPart.indexOf(':');
            int slash = hostPart.indexOf('/');
            int end = Math.min(
                    colon > 0 ? colon : hostPart.length(),
                    slash > 0 ? slash : hostPart.length()
            );
            return hostPart.substring(0, end).toLowerCase();
        }
        return null;
    }

    private boolean isDiscoveryEnabled(Map<String, Object> root) {
        String enabled = extractNestedString(root, "spring", "cloud", "discovery", "enabled");
        return enabled == null || "true".equalsIgnoreCase(enabled);
    }

    @SuppressWarnings("unchecked")
    private Object extractNested(Map<String, Object> map, String... keys) {
        Object current = map;
        for (String key : keys) {
            if (!(current instanceof Map)) return null;
            current = ((Map<String, Object>) current).get(key);
            if (current == null) return null;
        }
        return current;
    }

    private String extractNestedString(Map<String, Object> map, String... keys) {
        Object val = extractNested(map, keys);
        return val != null ? String.valueOf(val) : null;
    }

    private String stringVal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }
}
