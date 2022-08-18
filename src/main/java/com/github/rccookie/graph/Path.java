package com.github.rccookie.graph;

import java.util.List;
import java.util.Set;

import com.github.rccookie.util.Cloneable;

import org.jetbrains.annotations.NotNull;

public interface Path<N,E> extends ReadableGraph<N,E>, Cloneable<Path<N,E>> {

    boolean append(N node, E edge);

    boolean insertBefore(Object after, N node, E edge);

    boolean insertBefore(int index,    N node, E edge);

    boolean insertAfter( Object after, N node, E edge);

    boolean insertAfter( int index,    N node, E edge);

    boolean set(@NotNull Object old, @NotNull N now);

    boolean set(int index,  N node);

    E setEdgeBefore(Object node, E edge);

    E setEdgeBefore(int index,   E edge);

    E setEdgeAfter( Object node, E edge);

    E setEdgeAfter( int index,   E edge);

    boolean remove(Object node);

    N remove(int index);

    boolean clear();

    N first();

    N last();

    N get(int index);

    default E getEdge(int index) {
        return getEdgeBefore(index);
    }

    E getEdgeBefore(int index);

    E getEdgeAfter(int index);

    int indexOf(Object node);

    int indexOfEdge(Object edge);

    int lastIndexOfEdge(Object edge);



    @Override
    Set<N> nodes();

    @Override
    @NotNull List<E> weights();

    @NotNull
    Tree<N,E> asTree();
}
