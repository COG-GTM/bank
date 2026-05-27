package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Docker Compose file to extract service nodes, infrastructure nodes (databases, brokers),
 * and dependency edges (depends_on, JDBC connections from env vars, AMQP connections).
 */
public class DockerComposeParser {

    private static final Pattern JDBC_URL_PATTERN = Pattern.compile(
            "jdbc:(mysql|mariadb|postgresql|h2)://([^:/]+)(?::(\\d+))?/([^?]+)");
    private static final Pattern AMQP_URL_PATTERN = Pattern.compile(
            "amqp://([^:/]+)");
    private static final Set<String> BROKER_IMAGE_KEYWORDS = Set.of(
            "rabbitmq", "activemq", "kafka", "artemis");
    private static final Set<String> DB_IMAGE_KEYWORDS = Set.of(
            "mysql", "mariadb", "postgres", "postgresql", "mongo", "redis", "h2");

    public DependencyGraph parse(Reader reader) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(reader);
        if (root == null) {
            throw new IllegalArgumentException("Docker Compose file is empty or invalid");
        }
        return parseRoot(root);
    }

    @SuppressWarnings("unchecked")
    private DependencyGraph parseRoot(Map<String, Object> root) {
        Object servicesObj = root.get("services");
        if (servicesObj == null) {
            throw new IllegalArgumentException("Docker Compose file has no 'services' key");
        }
        if (!(servicesObj instanceof Map<?, ?> servicesMap)) {
            throw new IllegalArgumentException("'services' must be a mapping");
        }

        DependencyGraph graph = new DependencyGraph();
        Set<String> dbHosts = new HashSet<>();
        Set<String> brokerHosts = new HashSet<>();

        for (Map.Entry<?, ?> entry : servicesMap.entrySet()) {
            String serviceName = entry.getKey().toString();
            if (!(entry.getValue() instanceof Map<?, ?> serviceConfig)) {
                continue;
            }
            Map<String, Object> config = (Map<String, Object>) serviceConfig;

            NodeType nodeType = classifyService(serviceName, config);
            graph.addNode(new Node(serviceName, serviceName, nodeType));

            if (nodeType == NodeType.DATABASE) {
                dbHosts.add(serviceName);
            } else if (nodeType == NodeType.MESSAGE_BROKER) {
                brokerHosts.add(serviceName);
            }
        }

        for (Map.Entry<?, ?> entry : servicesMap.entrySet()) {
            String serviceName = entry.getKey().toString();
            if (!(entry.getValue() instanceof Map<?, ?> serviceConfig)) {
                continue;
            }
            Map<String, Object> config = (Map<String, Object>) serviceConfig;

            parseDependsOn(graph, serviceName, config);
            parseEnvironmentConnections(graph, serviceName, config, dbHosts, brokerHosts);
        }

        return graph;
    }

    private NodeType classifyService(String name, Map<String, Object> config) {
        String image = config.containsKey("image") ? config.get("image").toString().toLowerCase() : "";
        String nameLower = name.toLowerCase();

        for (String keyword : DB_IMAGE_KEYWORDS) {
            if (image.contains(keyword) || nameLower.contains(keyword)) {
                return NodeType.DATABASE;
            }
        }
        for (String keyword : BROKER_IMAGE_KEYWORDS) {
            if (image.contains(keyword) || nameLower.contains(keyword)) {
                return NodeType.MESSAGE_BROKER;
            }
        }
        return NodeType.SERVICE;
    }

    @SuppressWarnings("unchecked")
    private void parseDependsOn(DependencyGraph graph, String serviceName, Map<String, Object> config) {
        Object dependsOn = config.get("depends_on");
        if (dependsOn == null) return;

        List<String> deps;
        if (dependsOn instanceof List<?> list) {
            deps = list.stream().map(Object::toString).toList();
        } else if (dependsOn instanceof Map<?, ?> map) {
            deps = map.keySet().stream().map(Object::toString).toList();
        } else {
            return;
        }

        for (String dep : deps) {
            if (graph.getNode(dep).isPresent()) {
                NodeType targetType = graph.getNode(dep).get().getType();
                EdgeType edgeType = switch (targetType) {
                    case DATABASE -> EdgeType.JDBC;
                    case MESSAGE_BROKER -> EdgeType.AMQP;
                    case SERVICE -> EdgeType.HTTP;
                };
                graph.addEdge(new Edge(serviceName, dep, edgeType, "depends_on"));
            }
        }
    }

    private void parseEnvironmentConnections(DependencyGraph graph, String serviceName,
                                             Map<String, Object> config,
                                             Set<String> dbHosts, Set<String> brokerHosts) {
        Map<String, String> envVars = extractEnvironment(config);

        for (Map.Entry<String, String> envEntry : envVars.entrySet()) {
            String value = envEntry.getValue();

            Matcher jdbcMatcher = JDBC_URL_PATTERN.matcher(value);
            if (jdbcMatcher.find()) {
                String dbHost = jdbcMatcher.group(2);
                String dbName = jdbcMatcher.group(4);
                String dbNodeId = findOrCreateDbNode(graph, dbHost, dbName, dbHosts);
                graph.addEdge(new Edge(serviceName, dbNodeId, EdgeType.JDBC,
                        "jdbc:%s/%s".formatted(jdbcMatcher.group(1), dbName)));
                continue;
            }

            Matcher amqpMatcher = AMQP_URL_PATTERN.matcher(value);
            if (amqpMatcher.find()) {
                String brokerHost = amqpMatcher.group(1);
                String brokerNodeId = findOrCreateBrokerNode(graph, brokerHost, brokerHosts);
                graph.addEdge(new Edge(serviceName, brokerNodeId, EdgeType.AMQP, "amqp"));
            }
        }

        String mysqlHost = envVars.get("MYSQL_HOST");
        if (mysqlHost != null) {
            String mysqlDb = envVars.getOrDefault("MYSQL_DATABASE", "unknown");
            String dbNodeId = findOrCreateDbNode(graph, mysqlHost, mysqlDb, dbHosts);
            graph.addEdge(new Edge(serviceName, dbNodeId, EdgeType.JDBC,
                    "jdbc:mysql/%s".formatted(mysqlDb)));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractEnvironment(Map<String, Object> config) {
        Object env = config.get("environment");
        if (env == null) return Map.of();

        Map<String, String> result = new LinkedHashMap<>();
        if (env instanceof List<?> list) {
            for (Object item : list) {
                String s = item.toString();
                int eq = s.indexOf('=');
                if (eq > 0) {
                    result.put(s.substring(0, eq), s.substring(eq + 1));
                }
            }
        } else if (env instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                result.put(e.getKey().toString(), e.getValue() != null ? e.getValue().toString() : "");
            }
        }
        return result;
    }

    private String findOrCreateDbNode(DependencyGraph graph, String host, String dbName,
                                      Set<String> dbHosts) {
        for (String existingHost : dbHosts) {
            if (existingHost.equalsIgnoreCase(host)) {
                return existingHost;
            }
        }
        String nodeId = "db-%s-%s".formatted(host, dbName);
        if (graph.getNode(nodeId).isEmpty()) {
            graph.addNode(new Node(nodeId, "MySQL(%s/%s)".formatted(host, dbName), NodeType.DATABASE));
        }
        return nodeId;
    }

    private String findOrCreateBrokerNode(DependencyGraph graph, String host,
                                          Set<String> brokerHosts) {
        for (String existingHost : brokerHosts) {
            if (existingHost.equalsIgnoreCase(host)) {
                return existingHost;
            }
        }
        String nodeId = "broker-%s".formatted(host);
        if (graph.getNode(nodeId).isEmpty()) {
            graph.addNode(new Node(nodeId, "Broker(%s)".formatted(host), NodeType.MESSAGE_BROKER));
        }
        return nodeId;
    }
}
