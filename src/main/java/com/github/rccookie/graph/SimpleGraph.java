package com.github.rccookie.graph;

import java.util.Set;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.NotNull;

public interface SimpleGraph<N> extends Graph<N,Integer> {

    boolean connect(@NotNull N a, @NotNull N b);

    @Override
    default Integer connect(@NotNull N a, @NotNull N b, Integer edge) {
        return connect(a, b) ? 1 : null;
    }

    @Override
    @NotNull Set<? extends SimpleEdge<N>> edges();

    @Override
    @NotNull SimpleGraph<N> clone();

    @Override
    @NotNull SimpleGraph<N> newInstance();

    /**
     * An unweighted edge connecting two nodes.
     *
     * @param <N> The node type
     */
    class SimpleEdge<N> extends Edge<N,Integer> {

        /**
         * Creates a new edge.
         *
         * @param a     The starting node
         * @param b     The end node
         */
        public SimpleEdge(@NotNull N a, @NotNull N b) {
            super(a, b, 1);
        }

        @Override
        public String toString() {
            return a + " ---> " + b;
        }
    }

    /**
     * Runs the given action for each edge in the graph. In a non-directed
     * graph, the action should be run twice for each edge, once in each direction.
     *
     * @param action The action to run with each edge
     */
    void forEach(BiConsumer<N,N> action);
}
