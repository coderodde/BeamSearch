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

public final class BidirectionalBeamSearchPathfinder implements Pathfinder {

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
        
        Queue<HeapNode> openForward            = new PriorityQueue<>();
        Queue<HeapNode> openBackward           = new PriorityQueue<>();
        Set<Integer> closedForward             = new HashSet<>();
        Set<Integer> closedBackward            = new HashSet<>();
        Map<Integer, Integer> parentsForward   = new HashMap<>();
        Map<Integer, Integer> parentsBackward  = new HashMap<>();
        Map<Integer, Double> distancesForward  = new HashMap<>();
        Map<Integer, Double> distancesBackward = new HashMap<>();
        
        double bestPathLength = Double.POSITIVE_INFINITY;
        Integer touchNode = null;
        
        openForward.add(new HeapNode(sourceNode, 0.0));
        openBackward.add(new HeapNode(targetNode, 0.0));
        parentsForward.put(sourceNode, null);
        parentsBackward.put(targetNode, null);
        distancesForward.put(sourceNode, 0.0);
        distancesBackward.put(targetNode, 0.0);
        
        while (!openForward.isEmpty() && !openBackward.isEmpty()) {
            if (touchNode != null) {
                Integer minA = openForward.peek().node;
                Integer minB = openBackward.peek().node;
                
                double distanceA = distancesForward.get(minA) +
                                   heuristicFunction.estimate(minA, targetNode);
                double distanceB = distancesBackward.get(minB) +
                                   heuristicFunction.estimate(minB, sourceNode);
                
                if (bestPathLength <= Math.max(distanceA, distanceB)) {
                    return tracebackPath(touchNode, 
                                         parentsForward, 
                                         parentsBackward);
                }
            }
            
            if (openForward.size() + closedForward.size() <
                    openBackward.size() + closedBackward.size()) {
                Integer currentNode = openForward.remove().node;
                
                if (closedForward.contains(currentNode)) {
                    continue;
                }
                
                closedForward.add(currentNode);
                
                List<Integer> successors = 
                        getForwardSuccessors(graph,
                                             openBackward.peek().node,
                                             currentNode, 
                                             targetNode,
                                             distancesForward,
                                             heuristicFunction,
                                             beamWidth);
                
                for (Integer childNode : successors) {
                    if (closedForward.contains(childNode)) {
                        continue;
                    }
                    
                    double tentativeScore = 
                            distancesForward.get(currentNode) +
                            graph.getEdgeWeight(currentNode, childNode);
                    
                    if (!distancesForward.containsKey(childNode) 
                            || distancesForward.get(childNode) > 
                               tentativeScore) {
                        distancesForward.put(childNode, tentativeScore);
                        parentsForward.put(childNode, currentNode);
                        openForward.add(
                                new HeapNode(
                                        childNode, 
                                        tentativeScore + heuristicFunction
                                        .estimate(childNode, targetNode)));
                        
                        if (closedBackward.contains(childNode)) {
                            double pathLength = 
                                    distancesBackward.get(childNode) +
                                    tentativeScore;
                            
                            if (bestPathLength > pathLength) {
                                bestPathLength = pathLength;
                                touchNode = childNode;
                            }
                        }
                    }
                }
            } else {
                Integer currentNode = openBackward.remove().node;
                
                if (closedBackward.contains(currentNode)) {
                    continue;
                }
                
                closedBackward.add(currentNode);
                
                List<Integer> successors = 
                        getBackwardSuccessors(graph,
                                              openForward.peek().node,
                                              currentNode, 
                                              sourceNode,
                                              distancesBackward,
                                              heuristicFunction,
                                              beamWidth);
                
                for (Integer parentNode : successors) {
                    if (closedBackward.contains(parentNode)) {
                        continue;
                    }
                    
                    double tentativeScore = 
                            distancesBackward.get(currentNode) +
                            graph.getEdgeWeight(parentNode, currentNode);
                    
                    if (!distancesBackward.containsKey(parentNode)
                            || distancesBackward.get(parentNode) >
                               tentativeScore) {
                        distancesBackward.put(parentNode, tentativeScore);
                        parentsBackward.put(parentNode, currentNode);
                        openBackward.add(
                                new HeapNode(
                                    parentNode,
                                tentativeScore + heuristicFunction
                                .estimate(parentNode, sourceNode)));
                        
                        if (closedForward.contains(parentNode)) {
                            double pathLength = 
                                    distancesForward.get(parentNode) + 
                                    tentativeScore;
                            
                            if (bestPathLength > pathLength) {
                                bestPathLength = pathLength;
                                touchNode = parentNode;
                            }
                        }
                    }
                }
            }
        }
        
        throw new PathNotFoundException(
                "Target node " + targetNode + " is not reachable from " +
                sourceNode);
    }
    
    private static List<Integer> 
        getForwardSuccessors(AbstractGraph graph,
                             Integer backwardTop,
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
                        heuristicFunction.estimate(successor, backwardTop));
        }

        Collections.sort(successors, (a, b) -> {
            return Double.compare(costMap.get(a), costMap.get(b));
        });

        return successors.subList(0, Math.min(successors.size(), 
                                              beamWidth));
    }
     
    private static List<Integer>
            getBackwardSuccessors(AbstractGraph graph,
                                  Integer forwardTop,
                                  Integer currentNode, 
                                  Integer sourceNode,
                                  Map<Integer, Double> distances,
                                  HeuristicFunction<Integer> heuristicFunction,
                                  int beamWidth) {
        List<Integer> successors = new ArrayList<>();
        Map<Integer, Double> costMap = new HashMap<>();
        
        for (Integer successor : graph.getParentsOf(currentNode)) {
            successors.add(successor);
            costMap.put(
                    successor,
                    distances.get(currentNode) +
                        graph.getEdgeWeight(successor, currentNode) +
                        heuristicFunction.estimate(successor, forwardTop));
        }
        
        Collections.sort(successors, (a, b) -> {
            return Double.compare(costMap.get(a), costMap.get(b));
        });
        
        return successors.subList(0, Math.min(successors.size(),
                                              beamWidth));
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
