package com.github.rccookie.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.BoolWrapper;
import com.github.rccookie.util.IterableMap;
import com.github.rccookie.util.MappingIterator;
import com.github.rccookie.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * HashMap based implementation of {@link Graph}. This is the core implementation of
 * {@link Graph} and {@link ReadableGraph}. A hash graph can be directed or undirected.
 */
public class HashGraph<N,E> implements Graph<N,E> {

    final boolean directed;
    final Map<N, Map<N,E>> nodes = new HashMap<>();
    private Map<N, Map<N,E>> adjacencyView = null;

    /**
     * Creates a new, empty, directed hash graph.
     */
    public HashGraph() {
        this(true);
    }

    /**
     * Creates a new, empty hash graph.
     *
     * @param directed Whether the graph should be directed
     */
    public HashGraph(boolean directed) {
        this.directed = directed;
    }

    /**
     * Creates a new hash graph with the same nodes and edges as the
     * given graph. It is directed if and only if the given graph
     * is directed.
     *
     * @param graph The graph to copy
     */
    public HashGraph(@NotNull ReadableGraph<? extends N, ? extends E> graph) {
        this(graph, Arguments.checkNull(graph, "graph").isDirected());
    }

    /**
     * Creates a new hash graph with the same nodes and edges as the
     * given graph.
     *
     * @param graph The graph to copy
     * @param directed Whether the new graph should be directed
     */
    public HashGraph(@NotNull ReadableGraph<? extends N, ? extends E> graph, boolean directed) {
        this(directed);
        Arguments.checkNull(graph, "graph");
        for(N n : graph) add(n);
        for(Edge<N,E> e : edges())
            connect(e.a, e.b, e.value);
    }

    @Override
    public boolean add(@NotNull N node) {
        BoolWrapper diff = new BoolWrapper();
        nodes.computeIfAbsent(Arguments.checkNull(node, "node"), n -> {
            diff.value = true;
            return new HashMap<>();
        });
        return diff.value;
    }

    @Override
    public boolean remove(@NotNull Object node) {
        if(nodes.remove(node) == null) return false;
        nodes.remove(node);
        for(Map<N,E> adj : nodes.values())
            adj.remove(node);
        return true;
    }

    @Override
    public boolean set(@NotNull Object old, @NotNull N now) {
        Arguments.checkNull(old, "old");
        Arguments.checkNull(now, "now");
        if(!contains(old) || old.equals(now)) return false;
        BoolWrapper diff = new BoolWrapper(add(now));
        for(N n : this) {
            if(connected(n, old)) {
                connect(n, now, edge(n, old));
                diff.value = true;
            }
        }
        //noinspection SuspiciousMethodCalls
        nodes.get(old).forEach((n,e) -> {
            diff.value |= !connected(now, n);
            diff.value |= connect(now, n, e) != e;
        });
        return diff.value;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean join(@NotNull Object a, @NotNull Object b, @NotNull N into) {
        Arguments.checkNull(a, "a");
        Arguments.checkNull(b, "b");
        Arguments.checkNull(into, "into");

        if(a.equals(b) || a.equals(into) || !contains(a))
            return set(b, into);
        if(b.equals(into) || !contains(b))
            return set(a, into);

        BoolWrapper diff = new BoolWrapper(add(into));
        for(N n : this) {
            if(connected(n, b)) {
                connect(n, into, edge(n, b));
                diff.value = true;
            }
            else if(connected(n,a)) {
                connect(n, into, edge(n, a));
                diff.value = true;
            }
        }
        nodes.get(a).forEach((n,e) -> {
            diff.value |= !connected(into, n);
            diff.value |= connect(into, n, e) != e;
        });
        nodes.get(b).forEach((n,e) -> {
            diff.value |= !connected(into, n);
            diff.value |= connect(into, n, e) != e;
        });
        remove(a);
        remove(b);
        return diff.value;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return nodes.keySet().containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends N> c) {
        BoolWrapper diff = new BoolWrapper(false);
        for(N node : c) nodes.computeIfAbsent(node, n -> {
            diff.value = true;
            return new HashMap<>();
        });
        return diff.value;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        Set<Object> remove = new HashSet<>();
        for(N n : this)
            if(!c.contains(n))
                remove.add(n);
        return removeAll(remove);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean diff = false;
        for(Object o : c) diff |= remove(o);
        return diff;
    }

    @Override
    public E connect(@NotNull N a, @NotNull N b, E edge) {
        Arguments.checkNull(a, "a");
        Arguments.checkNull(b, "b");
        if(directed)
            nodes.computeIfAbsent(b, n -> new HashMap<>());
        else
            nodes.computeIfAbsent(b, n -> new HashMap<>()).put(a, edge);
        return nodes.computeIfAbsent(a, n -> new HashMap<>()).put(b, edge);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public E disconnect(@NotNull Object a, @NotNull Object b) {
        Map<N,E> adj = nodes.get(a);
        if(adj == null) return null;
        if(directed)
            return adj.remove(b);
        adj.remove(b);
        adj = nodes.get(b);
        if(adj == null) return null;
        return adj.remove(a);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean disconnectAll(@NotNull Object node) {
        Map<N,E> adj = nodes.get(node);
        if(adj == null || adj.isEmpty()) return false;
        if(!directed)
            for(N n : adj.keySet())
                if(!Objects.equals(n, node))
                    nodes.get(n).remove(node);
        adj.clear();
        return true;
    }

    @Override
    public Set<N> nodes() {
        return nodes.keySet();
    }

    @Override
    public IterableMap<N,E> adj(Object node) {
        //noinspection SuspiciousMethodCalls
        return IterableMap.of(Utils.view(nodes.get(node)));
    }

    @Override
    public Map<N, ? extends Map<N, E>> adjacencyList() {
        if(adjacencyView != null) return adjacencyView;
        return adjacencyView = new AdjacencyView();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public E edge(@NotNull Object a, @NotNull Object b) {
        Map<N,E> adj = nodes.get(a);
        if(adj == null) return null;
        return adj.get(b);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean connected(@NotNull Object a, @NotNull Object b) {
        Map<N,E> adj = nodes.get(a);
        return adj != null && adj.containsKey(b);
    }

    @Override
    public @NotNull Set<? extends Edge<N, E>> edges() {
        Set<Edge<N,E>> edges = new HashSet<>();
        nodes.forEach((a,adj) -> adj.forEach((b,e) -> edges.add(new Edge<>(a,b,e))));
        return edges;
    }

    @Override
    public @NotNull Collection<E> weights() {
        Collection<E> weights = new ArrayList<>();
        for(Map<N,E> adj : nodes.values()) weights.addAll(adj.values());
        return weights;
    }

    @Override
    public boolean contains(Object node) {
        //noinspection SuspiciousMethodCalls
        return nodes.containsKey(node);
    }

    @NotNull
    @Override
    public Iterator<N> iterator() {
        return nodes.keySet().iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return nodes.keySet().toArray();
    }

    @NotNull
    @Override
    public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
        return nodes.keySet().toArray(a);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public int edgeCount() {
        return weights().size();
    }

    @Override
    public boolean isEmpty() {
        return nodes.isEmpty();
    }

    @Override
    public void clear() {
        nodes.clear();
    }

    @Override
    public boolean isDirected() {
        return directed;
    }

    @Override
    public void forEach(EdgeConsumer<? super N, ? super E> action) {
        nodes.forEach((n,adj) -> adj.forEach((m,e) -> action.accept(n,m,e)));
    }

    @Override
    public @NotNull HashGraph<N,E> clone() {
        HashGraph<N,E> clone = newInstance();
        clone.addAll(this);
        nodes.forEach((n,adj) -> clone.nodes.get(n).putAll(adj));
        return clone;
    }

    @Override
    public @NotNull HashGraph<N,E> newInstance() {
        return new HashGraph<>(directed);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof Graph)) return false;
        Graph<?,?> hashGraph = (Graph<?,?>) o;
        return directed == hashGraph.isDirected() && nodes.equals(hashGraph.adjacencyList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(directed, nodes);
    }

    @Override
    public String toString() {
        int edges = edgeCount();
        return (directed ? "Directed graph" : "Graph") + " with " + size() + " nodes and " + (directed ? weights().size() : (edges/2 + "("+edges+")")) + " edges";
    }

    private class AdjacencyView implements Map<N, Map<N,E>> {

        @Override
        public int size() {
            return nodes.size();
        }

        @Override
        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return nodes.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return nodes.containsValue(value);
        }

        @Override
        public Map<N, E> get(Object key) {
            return Utils.view(nodes.get(key));
        }

        @Nullable
        @Override
        public Map<N, E> put(N key, Map<N, E> value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<N, E> remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(@NotNull Map<? extends N, ? extends Map<N, E>> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        @Override
        public Set<N> keySet() {
            return nodes.keySet();
        }

        @NotNull
        @Override
        public Collection<Map<N, E>> values() {
            return new Collection<>() {
                @Override
                public int size() {
                    return nodes.values().size();
                }

                @Override
                public boolean isEmpty() {
                    return nodes.values().isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    //noinspection SuspiciousMethodCalls
                    return nodes.containsValue(o);
                }

                @NotNull
                @Override
                public Iterator<Map<N, E>> iterator() {
                    Iterator<Map<N,E>> it = nodes.values().iterator();
                    return new Iterator<>() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Map<N, E> next() {
                            return Utils.view(it.next());
                        }
                    };
                }

                @NotNull
                @Override
                public Object @NotNull [] toArray() {
                    Object[] array = nodes.values().toArray();
                    for(int i=0; i<array.length; i++)
                        array[i] = Utils.view((Map<?,?>) array[i]);
                    return array;
                }

                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    T[] array = nodes.values().toArray(a);
                    for(int i=0; i<array.length; i++)
                        //noinspection unchecked
                        array[i] = (T) Utils.view((Map<?,?>) array[i]);
                    return array;
                }

                @Override
                public boolean add(Map<N, E> neMap) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean containsAll(@NotNull Collection<?> c) {
                    return nodes.values().containsAll(c);
                }

                @Override
                public boolean addAll(@NotNull Collection<? extends Map<N, E>> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(@NotNull Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(@NotNull Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @NotNull
        @Override
        public Set<Entry<N, Map<N, E>>> entrySet() {
            return new Set<>() {
                @Override
                public int size() {
                    return nodes.size();
                }

                @Override
                public boolean isEmpty() {
                    return nodes.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return nodes.entrySet().contains(o);
                }

                @NotNull
                @Override
                public Iterator<Entry<N, Map<N, E>>> iterator() {
                    //noinspection Convert2Lambda
                    return new MappingIterator<>(nodes.entrySet().iterator(), new Function<>() {
                        @Override
                        public Entry<N, Map<N, E>> apply(Entry<N, Map<N, E>> e) {
                            return new Entry<>() {
                                @Override
                                public N getKey() {
                                    return e.getKey();
                                }

                                @Override
                                public Map<N, E> getValue() {
                                    return Utils.view(e.getValue());
                                }

                                @Override
                                public Map<N, E> setValue(Map<N, E> value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }
                    });
                }

                @NotNull
                @Override
                public Object @NotNull [] toArray() {
                    return toArray(new Object[0]);
                }

                @SuppressWarnings("unchecked")
                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    T[] array = nodes.entrySet().toArray(a);
                    for(int i=0; i<array.length; i++) {
                        Entry<N, Map<N,E>> e = (Entry<N, Map<N,E>>) array[i];
                        array[i] = (T) new Entry<N, Map<N,E>>() {
                            @Override
                            public N getKey() {
                                return e.getKey();
                            }

                            @Override
                            public Map<N, E> getValue() {
                                return Utils.view(e.getValue());
                            }

                            @Override
                            public Map<N, E> setValue(Map<N, E> value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                    return array;
                }

                @Override
                public boolean add(Entry<N, Map<N, E>> nMapEntry) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean remove(Object o) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean containsAll(@NotNull Collection<?> c) {
                    return nodes.entrySet().containsAll(c);
                }

                @Override
                public boolean addAll(@NotNull Collection<? extends Entry<N, Map<N, E>>> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(@NotNull Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(@NotNull Collection<?> c) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}
