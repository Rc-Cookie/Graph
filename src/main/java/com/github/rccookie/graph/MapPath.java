package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.rccookie.util.T2;

import org.jetbrains.annotations.NotNull;

public interface MapPath<N,V,E> extends Path<N,E> {

    boolean add(@NotNull N node, V value);

    V put(@NotNull N node, V value);

    V get(@NotNull N node);

    @NotNull
    Collection<V> values();

    Set<Map.Entry<N,V>> entrySet();

    Map<N,V> asMap();

    Path<T2<N,V>,E> toTuplePath();
}
