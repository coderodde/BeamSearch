package net.coderodde.graph.pathfinding.beamsearch;

import java.util.Objects;

public final class DefaultHeuristicFunction 
        implements HeuristicFunction<Integer> {

    private final Coordinates coordinates;
    
    public DefaultHeuristicFunction(Coordinates coordinates) {
        this.coordinates = 
                Objects.requireNonNull(coordinates, 
                                       "The coordinate function is null.");
    }
    
    @Override
    public double estimate(Integer source, Integer target) {
        return coordinates.get(source).distance(coordinates.get(target));
    }
}
