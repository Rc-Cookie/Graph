package com.github.rccookie.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

public class SimpleHashGraph<N> extends HashGraph<N,Integer> implements SimpleGraph<N> {

    public SimpleHashGraph() {
    }

    public SimpleHashGraph(boolean directed) {
        super(directed);
    }

    @Override
    public boolean connect(@NotNull N a, @NotNull N b) {
        return connect(a, b, 1) == null;
    }

    @Override
    public Integer connect(@NotNull N a, @NotNull N b, Integer edge) {
        return super.connect(a, b, edge);
    }

    @Override
    public @NotNull Set<? extends SimpleEdge<N>> edges() {
        Set<SimpleEdge<N>> edges = new HashSet<>();
        nodes.forEach((a,adj) -> adj.forEach((b,e) -> edges.add(new SimpleEdge<>(a,b){})));
        return edges;
    }

    @Override
    public @NotNull SimpleHashGraph<N> clone() {
        SimpleHashGraph<N> clone = newInstance();
        clone.addAll(this);
        for(SimpleEdge<N> e : edges()) clone.connect(e.a, e.b);
        return clone;
    }

    @Override
    public @NotNull SimpleHashGraph<N> newInstance() {
        return new SimpleHashGraph<>(directed);
    }

    @Override
    public void forEach(BiConsumer<N, N> action) {
        nodes.forEach((n,adj) -> {
            for(N m : adj.keySet())
                action.accept(n,m);
        });
    }
}
