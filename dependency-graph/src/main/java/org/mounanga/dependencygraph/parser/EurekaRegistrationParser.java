package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;
import org.yaml.snakeyaml.Yaml;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EurekaRegistrationParser implements GraphParser {

    private static final Pattern PROPERTIES_LINE = Pattern.compile("^\\s*([^#=]+?)\\s*=\\s*(.*)$");
    private static final String EUREKA_URL_KEY = "eureka.client.service-url.defaultzone";
    private static final String APP_NAME_KEY = "spring.application.name";
    private static final String DATASOURCE_URL_KEY = "spring.datasource.url";
    private static final String DATASOURCE_USER_KEY = "spring.datasource.username";
    private static final String MAIL_HOST_KEY = "spring.mail.host";
    private static final String SERVER_PORT_KEY = "server.port";

    @Override
    public DependencyGraph parse(String content) throws GraphParseException {
        if (content == null || content.isBlank()) {
            throw new GraphParseException("Eureka registration content must not be null or blank");
        }

        Map<String, String> properties = isYaml(content) ? flattenYaml(content) : parseProperties(content);

        String appName = properties.get(APP_NAME_KEY);
        if (appName == null || appName.isBlank()) {
            throw new GraphParseException("Missing 'spring.application.name' in configuration");
        }

        DependencyGraph graph = new DependencyGraph();
        String serviceId = appName.toLowerCase();
        NodeType serviceType = classifyByName(appName);

        Map<String, String> metadata = new LinkedHashMap<>();
        String port = properties.get(SERVER_PORT_KEY);
        if (port != null) metadata.put("port", port);
        metadata.put("applicationName", appName);

        graph.addNode(new Node(serviceId, appName, serviceType, metadata));

        String eurekaUrl = properties.get(EUREKA_URL_KEY);
        if (eurekaUrl != null && !eurekaUrl.isBlank()) {
            String discoveryId = "discovery-service";
            if (graph.findNode(discoveryId).isEmpty()) {
                graph.addNode(new Node(discoveryId, "Discovery Service", NodeType.DISCOVERY,
                        Map.of("url", stripPlaceholder(eurekaUrl))));
            }
            graph.addEdge(new Edge(serviceId, discoveryId, EdgeType.EUREKA_REGISTER,
                    Map.of("eurekaUrl", stripPlaceholder(eurekaUrl))));
        }

        String datasourceUrl = properties.get(DATASOURCE_URL_KEY);
        if (datasourceUrl != null && datasourceUrl.contains("jdbc:")) {
            String dbId = deriveDbNodeId(datasourceUrl);
            String dbName = extractDatabaseName(datasourceUrl);
            if (graph.findNode(dbId).isEmpty()) {
                graph.addNode(new Node(dbId, dbId, NodeType.DATABASE, Map.of("url", stripPlaceholder(datasourceUrl))));
            }
            Map<String, String> edgeMeta = new LinkedHashMap<>();
            if (dbName != null) edgeMeta.put("database", dbName);
            String user = properties.get(DATASOURCE_USER_KEY);
            if (user != null) edgeMeta.put("username", stripPlaceholder(user));
            graph.addEdge(new Edge(serviceId, dbId, EdgeType.JDBC, edgeMeta));
        }

        String mailHost = properties.get(MAIL_HOST_KEY);
        if (mailHost != null && !mailHost.isBlank()) {
            String mailId = "mail-server";
            if (graph.findNode(mailId).isEmpty()) {
                graph.addNode(new Node(mailId, "Mail Server", NodeType.MAIL_SERVER,
                        Map.of("host", stripPlaceholder(mailHost))));
            }
            graph.addEdge(new Edge(serviceId, mailId, EdgeType.SMTP));
        }

        String axonServers = properties.get("axon.axonserver.servers");
        if (axonServers != null && !axonServers.isBlank()) {
            String axonId = "axon-server";
            if (graph.findNode(axonId).isEmpty()) {
                graph.addNode(new Node(axonId, "Axon Server", NodeType.EVENT_STORE,
                        Map.of("servers", stripPlaceholder(axonServers))));
            }
            graph.addEdge(new Edge(serviceId, axonId, EdgeType.AXON));
        }

        return graph;
    }

    private boolean isYaml(String content) {
        return content.contains(":") && (content.contains("\n  ") || content.contains("\n\t"));
    }

    private Map<String, String> flattenYaml(String content) {
        try {
            Map<String, Object> yaml = new Yaml().load(content);
            if (yaml == null) {
                throw new GraphParseException("YAML content is empty");
            }
            Map<String, String> flat = new LinkedHashMap<>();
            flatten("", yaml, flat);
            return flat;
        } catch (GraphParseException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphParseException("Failed to parse YAML configuration", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void flatten(String prefix, Map<String, Object> map, Map<String, String> result) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatten(key, (Map<String, Object>) value, result);
            } else if (value != null) {
                result.put(key.toLowerCase(), String.valueOf(value));
            }
        }
    }

    private Map<String, String> parseProperties(String content) {
        Map<String, String> props = new LinkedHashMap<>();
        try (Scanner scanner = new Scanner(new StringReader(content))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Matcher m = PROPERTIES_LINE.matcher(line);
                if (m.matches()) {
                    props.put(m.group(1).toLowerCase().trim(), m.group(2).trim());
                }
            }
        }
        return props;
    }

    private NodeType classifyByName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("gateway")) return NodeType.GATEWAY;
        if (lower.contains("discovery") || lower.contains("eureka")) return NodeType.DISCOVERY;
        return NodeType.SERVICE;
    }

    private String stripPlaceholder(String value) {
        if (value.contains("${")) {
            int colon = value.indexOf(':');
            int brace = value.indexOf('}');
            if (colon > 0 && colon < brace) {
                return value.substring(colon + 1, brace);
            }
        }
        return value;
    }

    private String deriveDbNodeId(String url) {
        String stripped = stripPlaceholder(url);
        if (stripped.contains("mysql")) return "mysql";
        if (stripped.contains("mariadb")) return "mariadb";
        if (stripped.contains("postgresql") || stripped.contains("postgres")) return "postgresql";
        return "database";
    }

    private String extractDatabaseName(String url) {
        String stripped = stripPlaceholder(url);
        int lastSlash = stripped.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < stripped.length() - 1) {
            String rest = stripped.substring(lastSlash + 1);
            int q = rest.indexOf('?');
            return q > 0 ? rest.substring(0, q) : rest;
        }
        return null;
    }
}
