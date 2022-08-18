package com.github.rccookie.graph;

import java.util.function.ToDoubleFunction;

import com.github.rccookie.util.Arguments;

import org.jetbrains.annotations.NotNull;

public class ShortestPaths<N,E> {

    private final ReadableGraph<N,E> graph;
    private final ToDoubleFunction<? super E> edgeLength;
    private final ReadableGraph<N,Double> distances;
    private final ReadableGraph<N,N> previous;

    public ShortestPaths(ReadableGraph<N, E> graph, ToDoubleFunction<? super E> edgeLength, @NotNull ReadableGraph<N, Double> distances, @NotNull ReadableGraph<N, N> previous) {
        this.graph = graph;
        this.edgeLength = edgeLength;
        this.distances = Arguments.checkNull(distances, "distances");
        this.previous = Arguments.checkNull(previous, "previous");
    }

    @NotNull
    public ReadableGraph<N, Double> distances() {
        return distances;
    }

    @NotNull
    public ReadableGraph<N, N> previous() {
        return previous;
    }

    public double distance(@NotNull N a, @NotNull N b) {
        return distances.edge(a,b);
    }

    public N previous(@NotNull N from, @NotNull N to) {
        return previous.edge(from, to);
    }

    public DistancePath<N,E> getPath(@NotNull N from, @NotNull N to) {
        Arguments.checkNull(from, "from");
        Arguments.checkNull(to, "to");
        if(!distances.connected(from, to)) return null;
        DistancePath<N,E> path = new DistancePath<>(to, edgeLength);
        if(from.equals(to)) return path;
        N next, current = to;
        while((current = previous(from, next = current)) != null)
            path.insertBefore(0, current, graph.edge(current, next));
        return path;
    }
}
