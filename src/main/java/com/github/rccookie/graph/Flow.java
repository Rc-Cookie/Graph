package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.IterableMap;

import org.jetbrains.annotations.NotNull;

public class Flow<N> implements ReadableGraph<N,Double> {

    @NotNull
    private final ReadableGraph<N,Double> flowGraph;

    public final double flow;
    @NotNull
    public final N source;
    @NotNull
    public final N drain;

    public Flow(ReadableGraph<N, Double> flowGraph, @NotNull N source, @NotNull N drain) {
        this(flowGraph, flowGraph.adj(source).values().stream().mapToDouble(f->f).sum(), source, drain);
    }

    public Flow(ReadableGraph<N, Double> flowGraph, double flow, @NotNull N source, @NotNull N drain) {
        this.flowGraph = Arguments.checkNull(flowGraph, "flowGraph");
        this.flow =      Arguments.checkRange(flow, 0d, null);
        this.source =    Arguments.checkNull(source, "source");
        this.drain =     Arguments.checkNull(drain,  "drain");
    }

    @Override
    public IterableMap<N, Double> adj(Object node) {
        return flowGraph.adj(node);
    }

    @Override
    public Map<N, ? extends Map<N, Double>> adjacencyList() {
        return flowGraph.adjacencyList();
    }

    @Override
    public Double edge(@NotNull Object a, @NotNull Object b) {
        return flowGraph.edge(a,b);
    }

    @Override
    public boolean connected(@NotNull Object a, @NotNull Object b) {
        return flowGraph.connected(a,b);
    }

    @Override
    public Set<N> nodes() {
        return flowGraph.nodes();
    }

    @Override
    public @NotNull Set<? extends Edge<N, Double>> edges() {
        return flowGraph.edges();
    }

    @Override
    public @NotNull Collection<Double> weights() {
        return flowGraph.weights();
    }

    @Override
    public boolean contains(Object node) {
        return flowGraph.contains(node);
    }

    @Override
    public int size() {
        return flowGraph.size();
    }

    @Override
    public int edgeCount() {
        return flowGraph.edgeCount();
    }

    @Override
    public boolean isEmpty() {
        return flowGraph.isEmpty();
    }

    @Override
    public boolean isDirected() {
        return flowGraph.isDirected();
    }

    @Override
    public void forEach(EdgeConsumer<? super N, ? super Double> action) {
        flowGraph.forEach(action);
    }

    @NotNull
    @Override
    public Iterator<N> iterator() {
        return flowGraph.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Flow)) return false;
        Flow<?> maxFlow = (Flow<?>) o;
        return Double.compare(maxFlow.flow, flow) == 0 && flowGraph.equals(maxFlow.flowGraph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowGraph, flow);
    }

    @Override
    public String toString() {
        return "MaxFlow with flow " + flow + " using flow network " + flowGraph;
    }
}
