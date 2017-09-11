package net.coderodde.graph.pathfinding.beamsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.coderodde.graph.AbstractGraph;

public interface Pathfinder {

    /**
     * Searches for a path from {@code source} to {@code target} in 
     * {@code graph} using {@code heuristicFunction} as a guide.
     * 
     * @param graph             the graph to search in.
     * @param source            the source (start) node.
     * @param target            the target (goal) node.
     * @param heuristicFunction the heuristic function.
     * @return 
     */
    public List<Integer> search(AbstractGraph graph,
                                Integer source, 
                                Integer target,
                                HeuristicFunction<Integer> heuristicFunction);
    
    default List<Integer> tracebackPath(Integer target,
                                        Map<Integer, Integer> parents) {
        List<Integer> path = new ArrayList<>();
        Integer currentNode = target;
        
        while (currentNode != null) {
            path.add(currentNode);
            currentNode = parents.get(currentNode);
        }
        
        Collections.<Integer>reverse(path);
        return path;
    }
    
    default List<Integer> tracebackPath(Integer touch, 
                                        Map<Integer, Integer> forwardParents,
                                        Map<Integer, Integer> backwardParents) {
        List<Integer> prefixPath = tracebackPath(touch, forwardParents);
        Integer currentNode = backwardParents.get(touch);
        
        while (currentNode != null) {
            prefixPath.add(currentNode);
            currentNode = backwardParents.get(currentNode);
        }
        
        return prefixPath;
    }
    
    /**
     * Makes sure that both {@code source} and {@code target} are in the
     * {@code graph}.
     * 
     * @param graph  the graph.
     * @param source the source node.
     * @param target the target node.
     */
    default void checkNodes(AbstractGraph graph, Integer source, Integer target) {
        if (!graph.hasNode(source)) {
            throw new IllegalArgumentException(
                    "The source node " + source + " is not in the graph.");
        }
        
        if (!graph.hasNode(target)) {
            throw new IllegalArgumentException(
                    "The target node " + target + " is not in the graph.");
        }
    }
}
