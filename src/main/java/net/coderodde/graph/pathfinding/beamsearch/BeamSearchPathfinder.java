package net.coderodde.graph.pathfinding.beamsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import net.coderodde.graph.AbstractGraph;

public final class BeamSearchPathfinder implements Pathfinder {

    /**
     * The default width of the beam.
     */
    private static final int DEFAULT_BEAM_WIDTH = Integer.MAX_VALUE;
    
    /**
     * The minimum allowed beam width.
     */
    private static final int MINIMUM_BEAM_WIDHT = 1;
    
    /**
     * The current beam width.
     */
    private int beamWidth = DEFAULT_BEAM_WIDTH;
    
    public int getBeamWidth() {
        return beamWidth;
    }
    
    public void setBeamWidth(int beamWidth) {
        this.beamWidth = Math.max(beamWidth, MINIMUM_BEAM_WIDHT);
    }
    
    @Override
    public List<Integer> search(AbstractGraph graph,
                                Integer sourceNode,
                                Integer targetNode, 
                                HeuristicFunction<Integer> heuristicFunction) {
        Objects.requireNonNull(graph, "The input graph is null.");
        Objects.requireNonNull(sourceNode, "The source node is null.");
        Objects.requireNonNull(targetNode, "The target node is null.");
        Objects.requireNonNull(heuristicFunction,
                               "The heuristic function is null.");
        
        checkNodes(graph, sourceNode, targetNode);
        
        Queue<HeapNode> open           = new PriorityQueue<>();
        Set<Integer> closed            = new HashSet<>();
        Map<Integer, Integer> parents  = new HashMap<>();
        Map<Integer, Double> distances = new HashMap<>();
     
        open.add(new HeapNode(sourceNode, 0.0));
        parents.put(sourceNode, null);
        distances.put(sourceNode, 0.0);
        
        while (!open.isEmpty()) {
            Integer currentNode = open.remove().node;
            
            if (currentNode.equals(targetNode)) {
                return tracebackPath(targetNode, parents);
            }
            
            if (closed.contains(currentNode)) {
                continue;
            }
            
            closed.add(currentNode);
            List<Integer> successorNodes = getSuccessors(graph,
                                                         currentNode,
                                                         targetNode,
                                                         distances,
                                                         heuristicFunction,
                                                         beamWidth);
            for (Integer childNode : successorNodes) {
                if (closed.contains(childNode)) {
                    continue;
                }
                
                double tentativeDistance = 
                        distances.get(currentNode) +
                        graph.getEdgeWeight(currentNode, childNode);
                
                if (!distances.containsKey(childNode)
                        || distances.get(childNode) > tentativeDistance) {
                    distances.put(childNode, tentativeDistance);
                    parents.put(childNode, currentNode);
                    open.add(
                            new HeapNode(childNode, 
                                         tentativeDistance + 
                                         heuristicFunction.estimate(
                                                 childNode, 
                                                 targetNode)));
                }
            }
        }
        
        throw new PathNotFoundException(
                "Path from " + sourceNode + " to " + targetNode + 
                " not found.");
    }
    
    private static List<Integer> 
        getSuccessors(AbstractGraph graph,
                      Integer currentNode,
                      Integer targetNode,
                      Map<Integer, Double> distances,
                      HeuristicFunction<Integer> heuristicFunction,
                      int beamWidth) {
        List<Integer> successors = new ArrayList<>();
        Map<Integer, Double> costMap = new HashMap<>();
        
        for (Integer successor : graph.getChildrenOf(currentNode)) {
            successors.add(successor);
            costMap.put(
                    successor, 
                    distances.get(currentNode) + 
                        graph.getEdgeWeight(currentNode, successor) +
                        heuristicFunction.estimate(successor, targetNode));
        }
        
        Collections.sort(successors, (a, b) -> {
            return Double.compare(costMap.get(a), costMap.get(b));
        });
        
        return successors.subList(0, Math.min(successors.size(), beamWidth));
    }
    
    private static final class HeapNode implements Comparable<HeapNode> {
        Integer node;
        double fScore;
        
        HeapNode(Integer node, double fScore) {
            this.node = node;
            this.fScore = fScore;
        }

        @Override
        public int compareTo(HeapNode o) {
            return Double.compare(fScore, o.fScore);
        }
    }
}
