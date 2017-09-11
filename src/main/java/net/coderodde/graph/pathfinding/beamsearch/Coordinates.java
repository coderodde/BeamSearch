package net.coderodde.graph.pathfinding.beamsearch;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

public final class Coordinates {

    private final Map<Integer, Point2D.Double> map = new HashMap<>();
    
    public Point2D.Double get(Integer node) {
        return map.get(node);
    }
    
    public void put(Integer node, Point2D.Double point) {
        map.put(node, point);
    }
}
