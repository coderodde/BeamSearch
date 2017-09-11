package net.coderodde.graph.pathfinding.beamsearch;

/**
 * This interface defines the API for heuristic functions.
 * 
 * @author Rodion "rodde" Efremov
 * @param <Node> the actual node type.
 * @version 1.6 (Sep 10, 2017)
 */
public interface HeuristicFunction<Node> {

    /**
     * Returns an optimistic estimate for the path from {@code source} to 
     * {@code target}.
     * 
     * @param source the source node.
     * @param target the target node.
     * @return distance estimate.
     */
    public double estimate(Node source, Node target);
}
