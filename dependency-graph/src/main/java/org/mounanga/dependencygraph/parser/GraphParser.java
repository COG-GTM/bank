package org.mounanga.dependencygraph.parser;

import org.mounanga.dependencygraph.model.DependencyGraph;

public interface GraphParser {

    DependencyGraph parse(String content) throws GraphParseException;
}
