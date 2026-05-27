package org.mounanga.dependencygraph.parser;

public class GraphParseException extends RuntimeException {

    public GraphParseException(String message) {
        super(message);
    }

    public GraphParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
