package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.rccookie.util.T2;

import org.jetbrains.annotations.NotNull;

public interface MapGraph<N,V,E> extends Graph<N,E> {

    boolean add(@NotNull N node, V value);

    V put(@NotNull N node, V value);

    V get(@NotNull N node);

    @NotNull
    Collection<V> values();

    Set<Map.Entry<N,V>> entrySet();

    Map<N,V> asMap();

    Graph<T2<N,V>,E> toTupleGraph();

    @Override
    @NotNull MapGraph<N,V,E> clone();

    @Override
    @NotNull MapGraph<N,V,E> newInstance();
}
