package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses Spring Boot application configuration files (application.properties and application.yml)
 * to extract Eureka discovery metadata: service names, Eureka server URLs, and registration edges.
 */
public class EurekaConfigParser {

    private static final Pattern EUREKA_URL_PATTERN = Pattern.compile(
            "https?://([^:/]+)(?::(\\d+))?/eureka/?");

    public record EurekaMetadata(String serviceName, String eurekaUrl, boolean registerWithEureka,
                                 boolean fetchRegistry) {
    }

    public EurekaMetadata parseProperties(Reader reader) throws IOException {
        Properties props = new Properties();
        props.load(reader);
        return extractFromProperties(props);
    }

    public EurekaMetadata parseYaml(Reader reader) {
        Yaml yaml = new Yaml();
        Map<String, Object> root = yaml.load(reader);
        if (root == null) {
            throw new IllegalArgumentException("YAML configuration is empty or invalid");
        }
        Properties props = flattenYaml(root, "");
        return extractFromProperties(props);
    }

    public void addToGraph(DependencyGraph graph, EurekaMetadata metadata) {
        graph.addNode(new Node(
                normalizeServiceName(metadata.serviceName()),
                metadata.serviceName(),
                NodeType.SERVICE));

        if (metadata.eurekaUrl() != null && metadata.registerWithEureka()) {
            String eurekaHost = extractEurekaHost(metadata.eurekaUrl());
            if (eurekaHost != null) {
                String eurekaNodeId = normalizeServiceName(eurekaHost);
                if (graph.getNode(eurekaNodeId).isEmpty()) {
                    graph.addNode(new Node(eurekaNodeId, eurekaHost, NodeType.SERVICE));
                }
                graph.addEdge(new Edge(
                        normalizeServiceName(metadata.serviceName()),
                        eurekaNodeId,
                        EdgeType.EUREKA,
                        "registers with"));
            }
        }
    }

    private EurekaMetadata extractFromProperties(Properties props) {
        String appName = props.getProperty("spring.application.name");
        if (appName == null || appName.isBlank()) {
            throw new IllegalArgumentException("Missing 'spring.application.name' in configuration");
        }

        String eurekaUrl = resolveProperty(props, "eureka.client.service-url.defaultZone");

        boolean registerWithEureka = Boolean.parseBoolean(
                props.getProperty("eureka.client.register-with-eureka", "true"));
        boolean fetchRegistry = Boolean.parseBoolean(
                props.getProperty("eureka.client.fetch-registry", "true"));

        return new EurekaMetadata(appName, eurekaUrl, registerWithEureka, fetchRegistry);
    }

    private String resolveProperty(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) return null;
        // Strip ${...} placeholders, keeping the default value after the colon
        return value.replaceAll("\\$\\{[^:}]+:([^}]+)}", "$1")
                .replaceAll("\\$\\{[^}]+}", "");
    }

    private String extractEurekaHost(String eurekaUrl) {
        Matcher matcher = EUREKA_URL_PATTERN.matcher(eurekaUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        try {
            URI uri = URI.create(eurekaUrl);
            return uri.getHost();
        } catch (Exception e) {
            return null;
        }
    }

    private Properties flattenYaml(Map<String, Object> map, String prefix) {
        Properties props = new Properties();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nested = (Map<String, Object>) value;
                props.putAll(flattenYaml(nested, key));
            } else if (value != null) {
                props.setProperty(key, value.toString());
            }
        }
        return props;
    }

    private String normalizeServiceName(String name) {
        return name.toLowerCase().replace('_', '-');
    }
}
