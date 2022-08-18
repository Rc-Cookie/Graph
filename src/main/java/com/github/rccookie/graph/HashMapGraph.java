package com.github.rccookie.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.rccookie.util.T2;
import com.github.rccookie.util.Utils;

import org.jetbrains.annotations.NotNull;

import static com.github.rccookie.util.Tuples.*;

public class HashMapGraph<N,V,E> extends HashGraph<N,E> implements MapGraph<N,V,E> {

    private final Map<N,V> map = new HashMap<>();
    private Map<N,V> mapView = null;


    public HashMapGraph() {
    }

    public HashMapGraph(boolean directed) {
        super(directed);
    }

    public HashMapGraph(@NotNull ReadableGraph<? extends N, ? extends E> graph) {
        super(graph);
    }

    public HashMapGraph(@NotNull ReadableGraph<? extends N, ? extends E> graph, boolean directed) {
        super(graph, directed);
    }

    public HashMapGraph(@NotNull MapGraph<? extends N, ? extends V, ? extends E> graph) {
        super(graph);
    }

    public HashMapGraph(@NotNull MapGraph<? extends N, ? extends V, ? extends E> graph, boolean directed) {
        super(graph, directed);
    }

    @Override
    public boolean add(@NotNull N node) {
        return add(node, null);
    }

    @Override
    public boolean add(@NotNull N node, V value) {
        map.put(node, value);
        return super.add(node);
    }

    @Override
    public V put(@NotNull N node, V value) {
        super.add(node);
        return map.put(node, value);
    }

    @Override
    public V get(@NotNull N node) {
        return map.get(node);
    }

    @Override
    public @NotNull Collection<V> values() {
        return map.values();
    }

    @Override
    public Set<Map.Entry<N, V>> entrySet() {
        return map.entrySet();
    }

    @Override
    public Map<N,V> asMap() {
        return mapView != null ? mapView : (mapView = Utils.view(map));
    }

    @Override
    public Graph<T2<N,V>,E> toTupleGraph() {
        Graph<T2<N,V>,E> graph = new HashGraph<>();
        map.forEach((n,v) -> {
            T2<N,V> t = t(n,v);
            graph.add(t);
            nodes.get(n).forEach((m,e) -> graph.connect(t, t(m,map.get(m)), e));
        });
        return graph;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof HashMapGraph)) return false;
        if(!super.equals(o)) return false;
        HashMapGraph<?, ?, ?> that = (HashMapGraph<?, ?, ?>) o;
        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), map);
    }

    @Override
    public String toString() {
        int edges = edgeCount();
        return (directed ? "Directed map" : "Map") + " graph with " + size() + " nodes and " + (directed ? weights().size() : (edges / 2 + "(" + edges + ")")) + " edges";
    }

    @Override
    public @NotNull HashMapGraph<N,V,E> clone() {
        HashMapGraph<N,V,E> clone = (HashMapGraph<N,V,E>) super.clone();
        clone.map.putAll(map);
        return clone;
    }

    @Override
    public @NotNull HashMapGraph<N,V,E> newInstance() {
        return new HashMapGraph<>(directed);
    }
}
