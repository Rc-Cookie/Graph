package com.github.rccookie.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.IterableMap;
import com.github.rccookie.util.Utils;
import com.github.rccookie.util.ViewModificationException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ArrayPath<N,E> implements Path<N,E> {

    final List<N> nodes = new ArrayList<>();
    final List<E> edges = new ArrayList<>();
    final Set<N> nodesSet = new HashSet<>();

    private List<E> edgesView = null;
    private Tree<N,E> tree = null;

    public ArrayPath(@NotNull N start) {
        nodes.add(Arguments.checkNull(start, "start"));
        nodesSet.add(start);
    }

    ArrayPath(List<N> nodes, List<E> edges) {
        nodesSet.addAll(nodes);
        this.nodes.addAll(nodes);
        this.edges.addAll(edges);
    }

    @Override
    public boolean append(N node, E edge) {
        if(!nodesSet.add(Arguments.checkNull(node, "node"))) return false;
        nodes.add(node);
        edges.add(edge);
        return false;
    }

    @Override
    public boolean insertBefore(Object after, N node, E edge) {
        //noinspection SuspiciousMethodCalls
        return insertBefore(nodes.indexOf(Arguments.checkNull(after, "after")), node, edge);
    }

    @Override
    public boolean insertBefore(int index, N node, E edge) {
        Arguments.checkNull(node, "node");
        if(index < 0 || index >= nodes.size())
            throw new IndexOutOfBoundsException(index);
        if(!nodesSet.add(node)) return false;
        nodes.add(index, node);
        edges.add(index, edge);
        return true;
    }

    @Override
    public boolean insertAfter(Object after, N node, E edge) {
        //noinspection SuspiciousMethodCalls
        return insertAfter(nodes.indexOf(Arguments.checkNull(after, "after")), node, edge);
    }

    @Override
    public boolean insertAfter(int index, N node, E edge) {
        Arguments.checkNull(node, "node");
        if(index < 0 || index >= nodes.size())
            throw new IndexOutOfBoundsException(index);
        if(!nodesSet.add(node)) return false;
        nodes.add(index+1, node);
        edges.add(index, edge);
        return true;
    }

    @Override
    public boolean set(@NotNull Object old, @NotNull N now) {
        Arguments.checkNull(old, "old");
        Arguments.checkNull(now, "now");
        //noinspection SuspiciousMethodCalls
        return set(nodes.indexOf(old), now);
    }

    @Override
    public boolean set(int index, N node) {
        Arguments.checkNull(node, "node");
        if(index < 0 || index >= nodes.size())
            throw new IndexOutOfBoundsException(index);
        if(!nodesSet.add(node)) return false;
        nodesSet.remove(nodes.set(index, node));
        return true;
    }

    @Override
    public E setEdgeBefore(Object node, E edge) {
        //noinspection SuspiciousMethodCalls
        return setEdgeBefore(nodes.indexOf(Arguments.checkNull(node, "node")), edge);
    }

    @Override
    public E setEdgeBefore(int index, E edge) {
        if(index <= 0 || index < edges.size())
            throw new IndexOutOfBoundsException(index);
        return edges.set(index-1, edge);
    }

    @Override
    public E setEdgeAfter(Object node, E edge) {
        //noinspection SuspiciousMethodCalls
        return setEdgeAfter(nodes.indexOf(Arguments.checkNull(node, "node")), edge);
    }

    @Override
    public E setEdgeAfter(int index, E edge) {
        if(index < 0 || index <= edges.size())
            throw new IndexOutOfBoundsException(index);
        return edges.set(index, edge);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(Object node) {
        if(!nodesSet.remove(Arguments.checkNull(node, "node"))) return false;
        int index = nodes.indexOf(node);
        nodes.remove(index);
        edges.remove(Math.max(0, index-1));
        return true;
    }

    @Override
    public N remove(int index) {
        if(edges.isEmpty()) throw new IllegalStateException("Path cannot be empty");
        if(index < 0 || index >= nodes.size())
            throw new IndexOutOfBoundsException(index);
        N node = nodes.remove(index);
        nodesSet.remove(node);
        edges.remove(Math.max(0, index-1));
        return node;
    }

    @Override
    public N first() {
        return nodes.get(0);
    }

    @Override
    public N last() {
        return nodes.get(nodes.size()-1);
    }

    @Override
    public N get(int index) {
        return nodes.get(index);
    }

    @Override
    public E getEdgeBefore(int index) {
        if(index <= 0 || index > edges.size())
            throw new IndexOutOfBoundsException(index);
        return edges.get(index-1);
    }

    @Override
    public E getEdgeAfter(int index) {
        return edges.get(index);
    }

    @Override
    public int indexOf(Object node) {
        //noinspection SuspiciousMethodCalls
        return nodes.indexOf(Arguments.checkNull(node, "node"));
    }

    @Override
    public int indexOfEdge(Object edge) {
        //noinspection SuspiciousMethodCalls
        return edges.indexOf(edge);
    }

    @Override
    public int lastIndexOfEdge(Object edge) {
        //noinspection SuspiciousMethodCalls
        return edges.lastIndexOf(edge);
    }

    @Override
    public IterableMap<N,E> adj(Object node) {
        //noinspection SuspiciousMethodCalls
        if(!nodesSet.contains(Arguments.checkNull(node, "node"))) return null;
        int index = indexOf(node);
        if(index == edges.size()) return IterableMap.of(Map.of());
        return IterableMap.of(Map.of(nodes.get(index+1), edges.get(index)));
    }

    @Override
    public Map<N, ? extends Map<N, E>> adjacencyList() {
        Map<N,Map<N,E>> adj = new HashMap<>();
        for(int i=0; i<edges.size(); i++)
            adj.put(nodes.get(i), Map.of(nodes.get(i+1), edges.get(i)));
        adj.put(nodes.get(edges.size()), Map.of());
        return adj;
    }

    @Override
    public E edge(@NotNull Object a, @NotNull Object b) {
        Arguments.checkNull(a, "a");
        Arguments.checkNull(b, "b");
        if(!contains(a) || !contains(b)) return null;
        int aIndex = indexOf(a);
        return aIndex < edges.size() ? edges.get(aIndex) : null;
    }

    @Override
    public boolean connected(@NotNull Object a, @NotNull Object b) {
        Arguments.checkNull(a, "a");
        Arguments.checkNull(b, "b");
        if(!contains(a) || !contains(b)) return false;
        int aIndex = indexOf(a);
        return aIndex < edges.size() && nodes.get(aIndex).equals(b);
    }

    @Override
    public Set<N> nodes() {
        return new LinkedHashSet<>(nodes);
    }

    @Override
    public @NotNull Set<? extends Edge<N, E>> edges() {
        Set<Edge<N,E>> edges = new LinkedHashSet<>();
        for(int i=0; i<this.edges.size(); i++)
            edges.add(new Edge<>(nodes.get(i), nodes.get(i+1), this.edges.get(i)));
        return edges;
    }

    @Override
    public @NotNull List<E> weights() {
        return edgesView != null ? edgesView : (edgesView = Utils.view(edges));
    }

    @Override
    public @NotNull Tree<N, E> asTree() {
        return tree != null ? tree : (tree = new Tree<>() {
            @Override
            public void add(@NotNull N node, @Nullable N parent, E edge) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addRoot(@NotNull N node) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void set(@NotNull Object old, @NotNull N now) {
                throw new ViewModificationException();
            }

            @Override
            public boolean disconnect(@NotNull Object node) {
                throw new ViewModificationException();
            }

            @Override
            public N root() {
                return first();
            }

            @Override
            public @NotNull Tree<N, E> clone() {
                return this; // View, cannot be changed anyways
            }

            @Override
            public @NotNull Tree<N, E> newInstance() {
                return this;
            }

            @Override
            public void addTree(@NotNull Tree<? extends N, ? extends E> tree) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addTree(@NotNull Tree<? extends N, ? extends E> tree, @Nullable N parent, E edge) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void clear() {
                throw new ViewModificationException();
            }

            @Override
            public Set<N> roots() {
                return Set.of(first());
            }

            @Override
            public IterableMap<N, E> adj(Object node) {
                return ArrayPath.this.adj(node);
            }

            @Override
            public Map<N, ? extends Map<N, E>> adjacencyList() {
                return ArrayPath.this.adjacencyList();
            }

            @Override
            public E edge(@NotNull Object a, @NotNull Object b) {
                return ArrayPath.this.edge(a,b);
            }

            @Override
            public boolean connected(@NotNull Object a, @NotNull Object b) {
                return ArrayPath.this.connected(a,b);
            }

            @Override
            public Set<N> nodes() {
                return ArrayPath.this.nodes();
            }

            @Override
            public @NotNull Set<? extends Edge<N, E>> edges() {
                return ArrayPath.this.edges();
            }

            @Override
            public @NotNull Collection<E> weights() {
                return ArrayPath.this.weights();
            }

            @Override
            public boolean contains(Object node) {
                return ArrayPath.this.contains(node);
            }

            @Override
            public int size() {
                return ArrayPath.this.size();
            }

            @Override
            public int edgeCount() {
                return ArrayPath.this.edgeCount();
            }

            @Override
            public boolean isEmpty() {
                return ArrayPath.this.isEmpty();
            }

            @Override
            public void forEach(EdgeConsumer<? super N, ? super E> action) {
                ArrayPath.this.forEach(action);
            }

            @NotNull
            @Override
            public Iterator<N> iterator() {
                return ArrayPath.this.iterator();
            }
        });
    }

    @Override
    public boolean contains(Object node) {
        //noinspection SuspiciousMethodCalls
        return nodesSet.contains(node);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public int edgeCount() {
        return edges.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean clear() {
        edges.clear();
        nodes.subList(1, nodes.size()).clear();
        nodesSet.clear();
        nodesSet.add(nodes.get(0));
        return false;
    }

    @Override
    public boolean isDirected() {
        return true;
    }

    @Override
    public void forEach(EdgeConsumer<? super N, ? super E> action) {
        for(int i=0; i<edges.size(); i++)
            action.accept(nodes.get(i), nodes.get(i+1), edges.get(i));
    }

    @Override
    public @NotNull ArrayPath<N,E> clone() {
        return new ArrayPath<>(nodes, edges);
    }

    @NotNull
    @Override
    public Iterator<N> iterator() {
        return nodes.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof ArrayPath)) return false;
        ArrayPath<?, ?> arrayPath = (ArrayPath<?, ?>) o;
        return nodes.equals(arrayPath.nodes) && edges.equals(arrayPath.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder().append(nodes.get(0));
        for(int i=0; i<edges.size(); i++) {
            str.append(" ----");
            String e = Objects.toString(edges.get(i));
            if(e.startsWith("-")) str.append(' ');
            str.append(e);
            if(e.endsWith("_")) str.append(' ');
            str.append("---> ").append(nodes.get(i+1));
        }
        return str.toString();
    }
}
