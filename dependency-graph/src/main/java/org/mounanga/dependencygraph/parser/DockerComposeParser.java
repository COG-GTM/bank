package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

public class DockerComposeParser implements GraphParser {

    private static final Set<String> MYSQL_INDICATORS = Set.of("mysql", "mariadb");
    private static final Set<String> RABBIT_INDICATORS = Set.of("rabbitmq", "rabbit");
    private static final Set<String> AXON_INDICATORS = Set.of("axonserver", "axon-server");
    private static final Set<String> MAIL_INDICATORS = Set.of("mailhog", "maildev", "mailpit", "smtp");

    @Override
    public DependencyGraph parse(String content) throws GraphParseException {
        if (content == null || content.isBlank()) {
            throw new GraphParseException("Docker Compose content must not be null or blank");
        }

        Map<String, Object> root;
        try {
            root = new Yaml().load(content);
        } catch (Exception e) {
            throw new GraphParseException("Failed to parse Docker Compose YAML", e);
        }

        if (root == null) {
            throw new GraphParseException("Docker Compose YAML is empty");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> services = (Map<String, Object>) root.get("services");
        if (services == null || services.isEmpty()) {
            throw new GraphParseException("No 'services' key found in Docker Compose file");
        }

        DependencyGraph graph = new DependencyGraph();

        for (Map.Entry<String, Object> entry : services.entrySet()) {
            String serviceName = entry.getKey();
            @SuppressWarnings("unchecked")
            Map<String, Object> serviceConfig = (Map<String, Object>) entry.getValue();
            if (serviceConfig == null) {
                continue;
            }
            processService(graph, serviceName, serviceConfig);
        }

        return graph;
    }

    private void processService(DependencyGraph graph, String serviceName, Map<String, Object> config) {
        NodeType nodeType = classifyService(serviceName, config);
        Map<String, String> metadata = extractMetadata(config);
        graph.addNode(new Node(serviceName, serviceName, nodeType, metadata));

        inferInfrastructureNodes(graph, serviceName, config);

        List<String> dependsOn = extractDependsOn(config);
        for (String dep : dependsOn) {
            graph.addEdge(new Edge(serviceName, dep, EdgeType.HTTP, Map.of("source", "depends_on")));
        }
    }

    private NodeType classifyService(String name, Map<String, Object> config) {
        String image = stringVal(config, "image");
        String combined = (name + " " + image).toLowerCase();

        if (combined.contains("gateway")) return NodeType.GATEWAY;
        if (combined.contains("eureka") || combined.contains("discovery")) return NodeType.DISCOVERY;
        if (MYSQL_INDICATORS.stream().anyMatch(combined::contains)) return NodeType.DATABASE;
        if (RABBIT_INDICATORS.stream().anyMatch(combined::contains)) return NodeType.MESSAGE_BROKER;
        if (AXON_INDICATORS.stream().anyMatch(combined::contains)) return NodeType.EVENT_STORE;
        if (MAIL_INDICATORS.stream().anyMatch(combined::contains)) return NodeType.MAIL_SERVER;

        return NodeType.SERVICE;
    }

    private void inferInfrastructureNodes(DependencyGraph graph, String serviceName, Map<String, Object> config) {
        Map<String, String> envVars = extractEnvironment(config);

        for (Map.Entry<String, String> env : envVars.entrySet()) {
            String key = env.getKey().toUpperCase();
            String value = env.getValue();

            if (key.contains("MYSQL") && key.contains("HOST") || key.contains("DATASOURCE") && value.contains("jdbc:mysql")) {
                String dbNodeId = "mysql-" + extractHost(value, envVars);
                if (graph.findNode(dbNodeId).isEmpty()) {
                    graph.addNode(new Node(dbNodeId, "MySQL", NodeType.DATABASE, Map.of("host", extractHost(value, envVars))));
                }
                graph.addEdge(new Edge(serviceName, dbNodeId, EdgeType.JDBC,
                        Map.of("database", envVars.getOrDefault("MYSQL_DATABASE", envVars.getOrDefault("DATABASE", "unknown")))));
            }

            if (key.contains("AXON") && key.contains("HOST")) {
                String axonNodeId = "axon-server";
                if (graph.findNode(axonNodeId).isEmpty()) {
                    graph.addNode(new Node(axonNodeId, "Axon Server", NodeType.EVENT_STORE, Map.of("host", value)));
                }
                graph.addEdge(new Edge(serviceName, axonNodeId, EdgeType.AXON));
            }

            if (key.contains("RABBITMQ") && key.contains("HOST") || key.equals("SPRING_RABBITMQ_HOST")) {
                String rabbitNodeId = "rabbitmq-" + value;
                if (graph.findNode(rabbitNodeId).isEmpty()) {
                    graph.addNode(new Node(rabbitNodeId, "RabbitMQ", NodeType.MESSAGE_BROKER, Map.of("host", value)));
                }
                graph.addEdge(new Edge(serviceName, rabbitNodeId, EdgeType.AMQP));
            }

            if (key.contains("MAIL_HOST") || key.equals("SPRING_MAIL_HOST")) {
                String mailNodeId = "mail-" + value;
                if (graph.findNode(mailNodeId).isEmpty()) {
                    graph.addNode(new Node(mailNodeId, "Mail Server", NodeType.MAIL_SERVER, Map.of("host", value)));
                }
                graph.addEdge(new Edge(serviceName, mailNodeId, EdgeType.SMTP));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractEnvironment(Map<String, Object> config) {
        Object envObj = config.get("environment");
        if (envObj == null) return Collections.emptyMap();

        Map<String, String> result = new LinkedHashMap<>();
        if (envObj instanceof Map<?, ?> envMap) {
            envMap.forEach((k, v) -> result.put(String.valueOf(k), String.valueOf(v)));
        } else if (envObj instanceof List<?> envList) {
            for (Object item : envList) {
                String s = String.valueOf(item);
                int eq = s.indexOf('=');
                if (eq > 0) {
                    result.put(s.substring(0, eq), s.substring(eq + 1));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> extractDependsOn(Map<String, Object> config) {
        Object depsObj = config.get("depends_on");
        if (depsObj == null) return Collections.emptyList();

        if (depsObj instanceof List<?> depsList) {
            return depsList.stream().map(String::valueOf).toList();
        }
        if (depsObj instanceof Map<?, ?> depsMap) {
            return new ArrayList<>(depsMap.keySet().stream().map(String::valueOf).toList());
        }
        return Collections.emptyList();
    }

    private Map<String, String> extractMetadata(Map<String, Object> config) {
        Map<String, String> metadata = new LinkedHashMap<>();
        Object ports = config.get("ports");
        if (ports instanceof List<?> portsList) {
            metadata.put("ports", portsList.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(""));
        }
        String image = stringVal(config, "image");
        if (image != null) {
            metadata.put("image", image);
        }
        return metadata;
    }

    private String extractHost(String value, Map<String, String> envVars) {
        String host = envVars.getOrDefault("MYSQL_HOST", "");
        if (!host.isEmpty()) return host;
        if (value.contains("//")) {
            String afterSlash = value.substring(value.indexOf("//") + 2);
            int colon = afterSlash.indexOf(':');
            int slash = afterSlash.indexOf('/');
            int end = Math.min(colon > 0 ? colon : afterSlash.length(), slash > 0 ? slash : afterSlash.length());
            return afterSlash.substring(0, end);
        }
        return "unknown";
    }

    private String stringVal(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }
}
