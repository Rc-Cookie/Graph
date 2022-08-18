package com.github.rccookie.graph;

import java.util.Objects;

import com.github.rccookie.util.Arguments;

import org.jetbrains.annotations.NotNull;

/**
 * A directional edge connecting two nodes.
 *
 * @param <N> The node type
 */
public class Edge<N, E> {

    /**
     * The starting node of the edge.
     */
    @NotNull
    public final N a;
    /**
     * The end node of the edge.
     */
    @NotNull
    public final N b;
    /**
     * The weight of the edge.
     */
    public final E value;

    /**
     * Creates a new edge.
     *
     * @param a     The starting node
     * @param b     The end node
     * @param value The edge weight
     */
    public Edge(@NotNull N a, @NotNull N b, E value) {
        this.a = Arguments.checkNull(a, "a");
        this.b = Arguments.checkNull(b, "b");
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Edge)) return false;
        Edge<?, ?> edge = (Edge<?, ?>) o;
        return a.equals(edge.a) && b.equals(edge.b) && Objects.equals(value, edge.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, value);
    }

    @Override
    public String toString() {
        String valueStr = Objects.toString(value);
        return a + " ----" + (valueStr.startsWith("-") ? " " : "") + valueStr + "---> " + b;
    }
}
