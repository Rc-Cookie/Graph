package com.github.rccookie.graph;

import java.util.Set;

import com.github.rccookie.util.Cloneable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * General representation of a (possibly directional) weighted graph.
 *
 * @param <N> Type of the nodes. Null is not permitted as node
 * @param <E> Content type of the edges (also referred to as "weights"). Null <b>is</b> permitted
 */
public interface Graph<N,E> extends ReadableGraph<N,E>, Set<N>, Cloneable<Graph<N,E>> {

    /**
     * Adds the given node to this graph, if not already present.
     *
     * @param node The node to add
     * @return Whether the node was new
     */
    @Override
    boolean add(@NotNull N node);

    /**
     * Removes the given node.
     *
     * @param node The node to remove
     * @return Whether the node was present before
     */
    @Override
    boolean remove(@NotNull Object node);

    /**
     * Replaces the specified old node with a new value. If the old node is
     * identical to the new node or was not in the graph, this method does
     * nothing. Otherwise, all incoming and outgoing connections from and
     * into the old one will be added to the new node (and potentially override
     * its edges, if it was already in this graph). If the old node was in this
     * graph, it will get removed, and the new node will be added if needed.
     *
     * @param old The node to replace
     * @param now The replacement
     * @return Whether the graph changed as a result of the method call
     */
    boolean set(@NotNull Object old, @NotNull N now);

    /**
     * Joins the nodes a and b into a new node and removes a and b from
     * the graph. If neither a nor b has been in the graph, or all nodes
     * are identical, this method does nothing. Otherwise, all incoming and
     * outgoing connections from and to a and b will be added to the new
     * node (and will potentially override existing edges). If a and b are
     * both connected from or to a same node, the weight from b's connection
     * will be used. Finally, a and b will be removed (except one was equal
     * to the new node).
     * <p>If you want to join two nodes into one of them, you can also use
     * {@link #set(Object, Object)}.</p>
     *
     * @param a The first node to join
     * @param b The second node to join
     * @param into The node to join a and b into
     * @return Whether the graph changed as a result of the method call
     */
    boolean join(@NotNull Object a, @NotNull Object b, @NotNull N into);


    /**
     * Connects the two given nodes. If they already are, the edge weight gets overridden.
     * If one or both nodes are not yet in the graph, they will be added to it. If this
     * graph is non-directional this also adds the reverse connection.
     *
     * @param a The first node
     * @param b The second node
     * @param edge The edge weight
     * @return The previous edge weight, or <code>null</code> if the edges were not
     *         connected before
     * @throws IllegalArgumentException If the specific graph implementation does not allow
     *                                  the given connection
     */
    E connect(@NotNull N a, @NotNull N b, E edge);

    /**
     * Removes the connection from <code>a</code> to <code>b</code>, if
     * a and b are present in this graph and were connected.
     *
     * @param a The first node
     * @param b The second node
     * @return The edge that was connecting a and b, or null of they were
     *         not connected (or a or b were not in the graph). For non-weighted
     *         graphs this will always return <code>null</code>.
     */
    E disconnect(@NotNull Object a, @NotNull Object b);

    /**
     * Disconnects all outgoing connections from the given node.
     *
     * @param node The node to disconnect
     * @return Whether the graph has changed
     */
    boolean disconnectAll(@NotNull Object node);

    /**
     * Returns the nodes in this graph. Note that {@link Graph} implements the {@link Set}
     * interface directly so there is usually no need to use this method.
     *
     * @return A view of this graph
     */
    @Contract(pure = true)
    @Override
    Set<N> nodes();

    /**
     * Removes all nodes (and thus all edges) from this graph.
     */
    @Override
    void clear();

    /**
     * Returns a shallow copy of this graph. This means that the node values themselves
     * are not cloned, only the graph data structure.
     *
     * @return A copy of this graph
     */
    @SuppressWarnings("override")
    @NotNull
    Graph<N,E> clone();

    /**
     * Returns a new instance of this graph type with the same properties as this one,
     * but empty.
     *
     * @return A new empty graph of the same type
     */
    @NotNull
    Graph<N,E> newInstance();
}
