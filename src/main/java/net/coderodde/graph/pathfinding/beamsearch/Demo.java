package net.coderodde.graph.pathfinding.beamsearch;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;
import net.coderodde.graph.AbstractGraph;
import net.coderodde.graph.DirectedGraph;

public final class Demo {

    /**
     * The width of the plane containing all the graph nodes.
     */
    private static final double GRAPH_LAYOUT_WIDTH  = 1000.0;
    
    /**
     * The height of the plane containing all the graph nodes.
     */
    private static final double GRAPH_LAYOUT_HEIGHT = 1000.0;
    
    /**
     * Given two nodes {@code u} and {@code v}, the cost of the arc
     * {@code (u,v)} will be their Euclidean distance times this factor.
     */
    private static final double ARC_LENGTH_FACTOR = 1.2;
    
    /**
     * The number of nodes in the graph.
     */
    private static final int NODES = 250_000;
    
    /**
     * The number of arcs in the graph.
     */
    private static final int ARCS = 1_500_000;
    
    /**
     * The beam width used in the demonstration.
     */
    private static final int BEAM_WIDTH = 4;
    
    public static void main(String[] args) {
        long seed = System.currentTimeMillis();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);
        GraphData data = createDirectedGraph(NODES, ARCS, random);
        warmup(data.graph, data.heuristicFunction, new Random(seed));
        benchmark(data.graph, data.heuristicFunction, new Random(seed));
    }
    
    private static final void 
        warmup(DirectedGraph graph, 
               HeuristicFunction<Integer> heuristicFunction,
               Random random) {
        perform(graph, heuristicFunction, random, false);
    }
    
    private static final void 
        benchmark(DirectedGraph graph, 
                  HeuristicFunction<Integer> heuristicFunction,
                  Random random) {
        perform(graph, heuristicFunction, random, true);
    }
    
    private static final void 
        perform(DirectedGraph graph, 
                HeuristicFunction<Integer> heuristicFunction,
                Random random,
                boolean output) {
        Integer sourceNode = random.nextInt(graph.size());
        Integer targetNode = random.nextInt(graph.size());
        
        BeamSearchPathfinder finder1 = new BeamSearchPathfinder();
        BidirectionalBeamSearchPathfinder finder2 = 
                new BidirectionalBeamSearchPathfinder();
        
        finder1.setBeamWidth(BEAM_WIDTH);
        finder2.setBeamWidth(BEAM_WIDTH);
        
        long start = System.currentTimeMillis();
        List<Integer> path1 = finder1.search(graph,
                                             sourceNode,
                                             targetNode,
                                             heuristicFunction);
        long end = System.currentTimeMillis();
        
        if (output) {
            System.out.println(finder1.getClass().getSimpleName() + ":");
            System.out.println("Path: " + path1 + ", length = " +
                               getPathLength(path1, graph));
            System.out.println("Time: " + (end - start) + " milliseconds.");
        }
        
        start = System.currentTimeMillis();
        List<Integer> path2 = finder2.search(graph,
                                             sourceNode, 
                                             targetNode, 
                                             heuristicFunction);
        end = System.currentTimeMillis();
        
        if (output) {
            System.out.println(finder2.getClass().getSimpleName() + ":");
            System.out.println("Path: " + path2 + ", length = " + 
                               getPathLength(path2, graph));
            System.out.println("Time: " + (end - start) + " milliseconds.");
        }
    }
    
    private static double getPathLength(List<Integer> path,
                                        AbstractGraph graph) {
        double sum = 0.0;
        
        for (int i = 0; i < path.size() - 1; ++i) {
            sum += graph.getEdgeWeight(path.get(i), path.get(i + 1));
        }
        
        return sum;
    }
        
    private static final class GraphData {
        DirectedGraph graph;
        HeuristicFunction<Integer> heuristicFunction;
    }
    
    private static final Coordinates getRandomCoordinates(AbstractGraph graph,
                                                          Random random) {
        Coordinates coordinates = new Coordinates();
        
        for (Integer node : graph.getAllNodes()) {
            coordinates.put(node, createRandomPoint(GRAPH_LAYOUT_WIDTH,
                                                    GRAPH_LAYOUT_HEIGHT,
                                                    random));
        }
        
        return coordinates;
    }
    
    private static final Point2D.Double
         createRandomPoint(double graphLayoutWidth,
                           double graphLayoutHeight,
                           Random random) {
        return new Point2D.Double(random.nextDouble() * graphLayoutWidth,
                                  random.nextDouble() * graphLayoutHeight);
    }
         
    private static final GraphData createDirectedGraph(int nodes,
                                                       int arcs,
                                                       Random random) {
        DirectedGraph graph = new DirectedGraph();
        
        for (int node = 0; node < nodes; ++node) {
            graph.addNode(node);
        }
        
        Coordinates coordinates = getRandomCoordinates(graph, random);
        HeuristicFunction<Integer> heuristicFunction =
                new DefaultHeuristicFunction(coordinates);
        
        for (int arc = 0; arc < arcs; ++arc) {
            Integer source = random.nextInt(nodes);
            Integer target = random.nextInt(nodes);
            
            double euclideanDistance = heuristicFunction.estimate(source,
                                                                  target);
            
            graph.addEdge(source,
                          target, 
                          ARC_LENGTH_FACTOR * euclideanDistance);
        }
        
        GraphData data = new GraphData();
        data.graph = graph;
        data.heuristicFunction = heuristicFunction;
        return data;
    }
}
