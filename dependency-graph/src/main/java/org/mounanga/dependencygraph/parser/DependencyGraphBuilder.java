package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.*;

import java.io.*;
import java.nio.file.*;
import java.util.List;

/**
 * Orchestrates parsing of Docker Compose, Eureka, and Gateway configurations
 * to build a unified dependency graph for the bank microservices system.
 */
public class DependencyGraphBuilder {

    private final DockerComposeParser dockerComposeParser = new DockerComposeParser();
    private final EurekaConfigParser eurekaConfigParser = new EurekaConfigParser();
    private final GatewayRouteParser gatewayRouteParser = new GatewayRouteParser();

    /**
     * Builds a complete dependency graph from a project root directory.
     * Expects docker-compose.yml at the root and service subdirectories
     * containing src/main/resources/application.properties or application.yml.
     */
    public DependencyGraph buildFromProjectRoot(Path projectRoot) throws IOException {
        Path composePath = projectRoot.resolve("docker-compose.yml");
        if (!Files.exists(composePath)) {
            throw new FileNotFoundException("docker-compose.yml not found in " + projectRoot);
        }

        DependencyGraph graph;
        try (Reader reader = Files.newBufferedReader(composePath)) {
            graph = dockerComposeParser.parse(reader);
        }

        enrichWithEurekaMetadata(graph, projectRoot);
        enrichWithGatewayRoutes(graph, projectRoot);

        return graph;
    }

    /**
     * Builds a graph from individual reader sources (useful for testing).
     */
    public DependencyGraph buildFromReaders(Reader dockerComposeReader,
                                            List<Reader> eurekaConfigReaders,
                                            Reader gatewayConfigReader) throws IOException {
        DependencyGraph graph = dockerComposeParser.parse(dockerComposeReader);

        for (Reader eurekaReader : eurekaConfigReaders) {
            try {
                EurekaConfigParser.EurekaMetadata metadata = eurekaConfigParser.parseProperties(eurekaReader);
                eurekaConfigParser.addToGraph(graph, metadata);
            } catch (Exception e) {
                // skip invalid configs
            }
        }

        if (gatewayConfigReader != null) {
            try {
                List<GatewayRouteParser.GatewayRoute> routes = gatewayRouteParser.parse(gatewayConfigReader);
                gatewayRouteParser.addToGraph(graph, "gateway-service", routes);
            } catch (Exception e) {
                // skip invalid gateway config
            }
        }

        return graph;
    }

    private void enrichWithEurekaMetadata(DependencyGraph graph, Path projectRoot) {
        for (Node node : graph.getNodesByType(NodeType.SERVICE)) {
            Path propsPath = projectRoot.resolve(node.getId())
                    .resolve("src/main/resources/application.properties");
            Path ymlPath = projectRoot.resolve(node.getId())
                    .resolve("src/main/resources/application.yml");

            try {
                if (Files.exists(propsPath)) {
                    try (Reader reader = Files.newBufferedReader(propsPath)) {
                        EurekaConfigParser.EurekaMetadata metadata = eurekaConfigParser.parseProperties(reader);
                        eurekaConfigParser.addToGraph(graph, metadata);
                    }
                } else if (Files.exists(ymlPath)) {
                    try (Reader reader = Files.newBufferedReader(ymlPath)) {
                        EurekaConfigParser.EurekaMetadata metadata = eurekaConfigParser.parseYaml(reader);
                        eurekaConfigParser.addToGraph(graph, metadata);
                    }
                }
            } catch (Exception e) {
                // skip services without valid config
            }
        }
    }

    private void enrichWithGatewayRoutes(DependencyGraph graph, Path projectRoot) {
        Path gatewayYml = projectRoot.resolve("gateway-service/src/main/resources/application.yml");
        if (!Files.exists(gatewayYml)) return;

        try (Reader reader = Files.newBufferedReader(gatewayYml)) {
            List<GatewayRouteParser.GatewayRoute> routes = gatewayRouteParser.parse(reader);
            gatewayRouteParser.addToGraph(graph, "gateway-service", routes);
        } catch (Exception e) {
            // skip if gateway config is invalid
        }
    }
}
