package com.github.rccookie.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.IterableMap;
import com.github.rccookie.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashForest<N,E> implements Forest<N,E> {

    final Graph<N,E> graph = new HashGraph<>();
    private final Set<N> roots = new HashSet<>();
    private Set<N> rootsView = null;

    @Override
    public IterableMap<N, E> adj(Object node) {
        return graph.adj(node);
    }

    @Override
    public Map<N, ? extends Map<N, E>> adjacencyList() {
        return graph.adjacencyList();
    }

    @Override
    public E edge(@NotNull Object a, @NotNull Object b) {
        return graph.edge(a, b);
    }

    @Override
    public boolean connected(@NotNull Object a, @NotNull Object b) {
        return graph.connected(a, b);
    }

    @Override
    public Set<N> nodes() {
        return graph.nodes();
    }

    @Override
    public @NotNull Set<? extends Edge<N, E>> edges() {
        return graph.edges();
    }

    @Override
    public @NotNull Collection<E> weights() {
        return graph.weights();
    }

    @Override
    public boolean contains(Object node) {
        return graph.contains(node);
    }

    @Override
    public int size() {
        return graph.size();
    }

    @Override
    public int edgeCount() {
        return graph.size();
    }

    @Override
    public boolean isEmpty() {
        return graph.isEmpty();
    }

    @Override
    public void forEach(EdgeConsumer<? super N, ? super E> action) {
        graph.forEach(action);
    }

    @Override
    public void clear() {
        graph.clear();
        roots.clear();
    }

    @Override
    public void add(@NotNull N node, @Nullable N parent, E edge) {
        if(node == parent || parent == null) {
            graph.add(node);
            roots.add(node);
            return;
        }
        // Disconnect from potential parents
        for(N n : this)
            if(graph.disconnect(n, node) != null) break; // Edge could also have value null, but probably not
        roots.remove(node);
        graph.connect(parent, node, edge);
    }

    @Override
    public void addRoot(@NotNull N node) {
        // Disconnect from potential parents
        for(N n : this)
            if(graph.disconnect(n, node) != null) break;
        graph.add(node);
        roots.add(node);
    }

    @Override
    public void addTree(@NotNull Tree<? extends N, ? extends E> tree) {
        addTree(tree, null, null);
    }

    @Override
    public void addTree(@NotNull Tree<? extends N, ? extends E> tree, @Nullable N parent, E edge) {
        if(tree.isEmpty()) return;
        if(parent == null)
            addRoot(tree.root());
        else add(tree.root(), parent, edge);
        tree.forEach((n,m,e) -> add(m,n,e));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void set(@NotNull Object old, @NotNull N now) {
        if(Arguments.checkNull(old, "old")
                .equals(Arguments.checkNull(now, "now"))) return;

        // Disconnect new node from previous parent
        if(!roots.remove(now)) {
            for (N n : graph) {
                if(graph.connected(n, now)) {
                    graph.disconnect(n, now);
                    break;
                }
            }
        }

        // Set parent of new node to parent of old node
        if(!roots.remove(old)) {
            for (N n : graph) {
                if(graph.connected(n, old)) {
                    graph.connect(n, now, graph.edge(n, old));
                    break;
                }
            }
        }
        else roots.add(now);

        // Connect children
        graph.adj(old).forEach((n,e) -> graph.connect(n, now, e));
        // Remove old node
        graph.remove(old);
    }

    @Override
    public boolean disconnect(@NotNull Object node) {
        IterableMap<N,?> adj = graph.adj(node);
        if(adj == null) return false;
        for(N child : adj)
            disconnect(child);
        //noinspection SuspiciousMethodCalls
        graph.remove(node);
        return true;
    }

    @Override
    public Set<N> roots() {
        return rootsView != null ? rootsView : (rootsView = Utils.view(roots));
    }

    @Override
    public @NotNull HashForest<N, E> clone() {
        HashForest<N,E> clone = new HashForest<>();
        clone.graph.addAll(graph);
        for(Edge<N,E> e : graph.edges())
            clone.graph.connect(e.a, e.b, e.value);
        return clone;
    }

    @Override
    public @NotNull HashForest<N, E> newInstance() {
        return new HashForest<>();
    }

    @NotNull
    @Override
    public Iterator<N> iterator() {
        return graph.iterator();
    }
}
