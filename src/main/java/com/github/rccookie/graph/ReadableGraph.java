package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.github.rccookie.util.IterableMap;

import org.jetbrains.annotations.NotNull;

/**
 * Base interface for all graph-based data structures.
 *
 * @param <N> Type of the nodes. Null is not permitted as node
 * @param <E> Content type of the edges (also referred to as "weights"). Null <b>is</b> permitted
 */
public interface ReadableGraph<N,E> extends Iterable<N> {

    /**
     * Returns the nodes connected directly to the given node, mapped to the
     * edges to each respective one. If the given node is not in this graph,
     * the method returns <code>null</code>.
     *
     * @param node The node to get the adjacent nodes for
     * @return The adjacent nodes mapped to their connection's weights, or
     *         <code>null</code>
     */
    IterableMap<N,E> adj(Object node);

    /**
     * Returns a map with the adjacent nodes mapped to their edges for each
     * node in the graph.
     *
     * @return The adjacency list
     * @see #adj(N)
     */
    Map<N, ? extends Map<N,E>> adjacencyList();

    /**
     * Returns the edge that connects the given two nodes. If the nodes
     * are not connected or a node is not in the graph, the method returns
     * <code>null</code>.
     *
     * @param a The first node
     * @param b The second node
     * @return The edge from a to b, or <code>null</code>
     */
    E edge(@NotNull Object a, @NotNull Object b);

    /**
     * Returns whether an edge from a to b exists. It does not matter whether
     * the edge has the value <code>null</code>.
     *
     * @param a The first node
     * @param b The second node
     * @return Whether a and b are in this graph and there is an edge from a to b
     */
    boolean connected(@NotNull Object a, @NotNull Object b);


    /**
     * Returns all nodes currently in the graph.
     *
     * @return The nodes in this graph.
     */
    Set<N> nodes();

    /**
     * Returns all edges currently in the graph.
     *
     * @return The edges in this graph
     */
    @NotNull
    Set<? extends Edge<N,E>> edges();

    /**
     * Returns all weights currently in the graph.
     *
     * @return The weights in this graph
     */
    @NotNull Collection<E> weights();

    /**
     * Returns whether the graph contains the given node.
     *
     * @param node The node to check
     * @return Whether the given object is in this graph
     */
    boolean contains(Object node);

    /**
     * Returns the number of nodes in this graph.
     *
     * @return The number of nodes
     */
    int size();

    /**
     * Returns the number of edges in this graph.
     *
     * @return The number of edges
     */
    int edgeCount();

    /**
     * Returns whether this graph has no nodes (and thus edges).
     *
     * @return Whether this graph is empty
     */
    boolean isEmpty();

    /**
     * Returns whether this graph is directional.
     *
     * @return True if this graph allows edges that are not
     *         non-directional.
     */
    boolean isDirected();

    /**
     * Runs the given action for each edge in the graph. In a non-directed
     * graph, the action should be run twice for each edge, once in each direction.
     *
     * @param action The action to run with each edge
     */
    void forEach(EdgeConsumer<? super N,? super E> action);
}
