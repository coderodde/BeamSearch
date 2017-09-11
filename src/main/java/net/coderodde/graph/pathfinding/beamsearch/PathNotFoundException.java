package net.coderodde.graph.pathfinding.beamsearch;

public final class PathNotFoundException extends RuntimeException {

    public PathNotFoundException(String message) {
        super(message);
    }
}
