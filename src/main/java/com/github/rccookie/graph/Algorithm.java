package com.github.rccookie.graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.find.Element;
import com.github.rccookie.util.find.UnionFind;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Algorithm {

    interface MST extends Algorithm {

        MST PRIM = new MST() {
            @Override
            public <N,E> Tree<N,E> compute(ReadableGraph<N, ? extends E> graph, Comparator<? super E> comparator) {

                Map<N,N> prev = new HashMap<>();
                Map<N,E> dist = new HashMap<>();

                Comparator<E> comp = Comparator.nullsLast(comparator);
                Heap<N> q = new BinaryHeap<>(graph.nodes(), Comparator.comparing(dist::get, comp));
                if(q.isEmpty()) return Tree.empty();

                N root = q.peek();

                while(!q.isEmpty()) {
                    N n = q.dequeue();
                    for(N m : graph.adj(n)) {
                        E nm = graph.edge(n,m);
                        if(q.contains(m) && comp.compare(nm, dist.get(m)) < 0) {
                            prev.put(m, n);
                            dist.put(m, nm);
                            q.updateDecreased(m);
                        }
                    }
                }

                Forest<N,E> mst = new HashForest<>();
                for(N n : graph) {
                    N p = prev.get(n);
                    if(p == null) mst.addRoot(n);
                    else mst.add(n, p, dist.get(n));
                }
                return Graphs.spanningTree(mst, root);
            }
        };

        MST KRUSKAL = new MST() {
            @SuppressWarnings("unchecked")
            @Override
            public <N,E> Tree<N,E> compute(ReadableGraph<N, ? extends E> graph, Comparator<? super E> comparator) {
                Graph<N,E> mst = new HashGraph<>(false);

                Map<N,Element> elements = new HashMap<>();
                for(N n : graph)
                    elements.put(n, UnionFind.makeSet());

                Edge<N,E>[] edges = graph.edges().toArray(Edge[]::new);
                Arrays.sort(edges, Comparator.comparing(e -> e.value, comparator));

                for(Edge<N,E> e : edges) {
                    if(UnionFind.find(elements.get(e.a)) != UnionFind.find(elements.get(e.b))) {
                        mst.connect(e.a, e.b, e.value);
                        UnionFind.union(elements.get(e.a), elements.get(e.b));
                    }
                }

                if(mst.isEmpty()) return Tree.empty();
                return Graphs.spanningTree(mst, mst.iterator().next());
            }
       };

        <N,E> Tree<N,E> compute(ReadableGraph<N,? extends E> graph, Comparator<? super E> comparator);
    }

    interface AnyPath extends Algorithm {

        AnyPath BREATH_FIRST = new AnyPath() {
            @Override
            public <N, E> Path<N, E> compute(@NotNull ReadableGraph<N, E> graph, @NotNull N source, @NotNull N target) {
                return computeBreathOrDepthFirst(graph, source, target, Deque::remove);
            }
        };

        AnyPath DEPTH_FIRST = new AnyPath() {
            @Override
            public <N, E> Path<N, E> compute(@NotNull ReadableGraph<N, E> graph, @NotNull N source, @NotNull N target) {
                return computeBreathOrDepthFirst(graph, source, target, Deque::removeLast);
            }
        };

        <N,E> Path<N,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target);

        private static <N,E> Path<N,E> computeBreathOrDepthFirst(ReadableGraph<N,E> graph, N source, N target, Function<Deque<N>,N> remove) {

            Arguments.checkNull(graph, "graph");
            Arguments.checkNull(source, "source");
            Arguments.checkNull(target, "target");
            assert remove != null;

            if(!graph.contains(source) || !graph.contains(target)) return null;
            if(source.equals(target)) return new ArrayPath<>(source);

            Map<N,N> p = new HashMap<>();
            Deque<N> q = new ArrayDeque<>();
            p.put(source, null);
            q.add(source);

            while(!q.isEmpty()) {
                N n = remove.apply(q);
                if(n.equals(target))
                    return Algorithm.buildPath(graph, target, p, ArrayPath::new);
                for(N m : graph.adj(n)) {
                    if(!p.containsKey(m)) {
                        p.put(m,n);
                        q.add(m);
                    }
                }
            }

            return null;
        }
    }

    interface ShortestPath extends Algorithm {

        ShortestPath A_STAR = new ShortestPath() {
            @Override
            public <N,E> DistancePath<N,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                                   @NotNull ToDoubleFunction<? super E> edgeLength, @Nullable ToDoubleBiFunction<? super N, ? super N> heuristic) {

                Arguments.checkNull(graph, "graph");
                Arguments.checkNull(source, "source");
                Arguments.checkNull(target, "target");

                ToDoubleBiFunction<? super N, ? super N> h = heuristic != null ? heuristic : (a,b) -> 0;

                Map<N,N> p = new HashMap<>();
                Map<N,Double> d = new HashMap<>();

                for(N n : graph) d.put(n, Double.POSITIVE_INFINITY);
                d.put(source, 0d);

                Heap<N> q = new BinaryHeap<>(Comparator.comparing(n -> d.get(n) + h.applyAsDouble(n, target)));
                q.enqueue(source);

                int count = 0;

                while(!q.isEmpty()) {
                    N n = q.dequeue();
                    count++;
                    if(n.equals(target)){
                        Console.mapDebug("Iterations", count);
                        return buildPath(graph, edgeLength, target, p);
                    }

                    double nd = d.get(n);
                    graph.adj(n).forEach((m,e) -> {
                        double dist = nd + edgeLength.applyAsDouble(e);
                        if(dist < d.get(m)) {
                            d.put(m,dist);
                            p.put(m,n);
                            if(!q.updateDecreased(m))
                                q.enqueue(m);
                        }
                    });
                }

                return null;
            }
        };

        <N,E> DistancePath<N,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                        @NotNull ToDoubleFunction<? super E> edgeLength, @Nullable ToDoubleBiFunction<? super N, ? super N> heuristic);
    }

    interface SingleSourceShortestPath extends Algorithm {

        SingleSourceShortestPath DIJKSTRA = new SingleSourceShortestPath() {
            @Override
            @NotNull
            public <N, E> MapGraph<N, Double, E> compute(@NotNull ReadableGraph<N, E> graph, @NotNull N source, @NotNull ToDoubleFunction<? super E> edgeLength) {
                Arguments.checkNull(graph, "graph");
                Arguments.checkNull(source, "source");
                Arguments.checkNull(edgeLength, "edgeLength");

                MapGraph<N,Double,E> p = new HashMapGraph<>();
                for(N n : graph) p.add(n, Double.POSITIVE_INFINITY);
                p.put(source, 0d);

                Heap<N> q = new BinaryHeap<>(Comparator.comparing(p::get));
                q.enqueue(source);

                while(!q.isEmpty()) {
                    N n = q.dequeue();

                    double dn = p.get(n);
                    graph.adj(n).forEach((m,e) -> {
                        double dist = dn + edgeLength.applyAsDouble(e);
                        if(dist < p.get(m)) {
                            p.put(m, dist);
                            p.disconnectAll(m);
                            p.connect(m,n,e);
                            if(!q.updateDecreased(m))
                                q.enqueue(m);
                        }
                    });
                }

                return p;
            }
        };

        @NotNull
        <N,E> MapGraph<N,Double,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull ToDoubleFunction<? super E> edgeLength);
    }

    interface AllPairsShortestPath extends Algorithm {

        AllPairsShortestPath FLOYD_WARSHALL = new AllPairsShortestPath() {
            @Override
            public @NotNull <N,E> ShortestPaths<N,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull ToDoubleFunction<? super E> edgeLength) {
                Arguments.checkNull(graph, "graph");
                Arguments.checkNull(edgeLength, "edgeLength");

                Graph<N,Double> d = new HashGraph<>();
                Graph<N,N> p = new HashGraph<>();
                d.addAll(graph.nodes());
                p.addAll(graph.nodes());

                graph.forEach((n,m,e) -> {
                    p.connect(n,m,n);
                    d.connect(n,m,edgeLength.applyAsDouble(e));
                });
                for(N n : graph.nodes()) d.connect(n,n,0d);

                for(N k : graph.nodes()) {
                    for(N i : graph.nodes()) {
                        for(N j : graph.nodes()) {
                            Double ik = d.edge(i,k);
                            if(ik == null) continue;
                            Double kj = d.edge(k,j);
                            if(kj == null) continue;
                            Double ij = d.edge(i,j);
                            double ikj = ik+kj;
                            if(ij == null || ij > ikj) {
                                d.connect(i,j, ikj);
                                p.connect(i,j,p.edge(k,j));
                            }
                        }
                    }
                }

                return new ShortestPaths<>(graph, edgeLength, d, p);
            }
        };

        @NotNull
        <N,E> ShortestPaths<N,E> compute(@NotNull ReadableGraph<N,E> graph, @NotNull ToDoubleFunction<? super E> edgeLength);
    }

    interface MaxFlowAlg extends Algorithm {

        MaxFlowAlg EDMONDS_KARP = new MaxFlowAlg() {
            @Override
            public @NotNull <N,E> Flow<N> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N drain,
                                                  @NotNull ToDoubleFunction<? super E> edgeCapacity) {

                Arguments.checkNull(graph,        "graph");
                Arguments.checkNull(source,       "source");
                Arguments.checkNull(drain,        "drain");
                Arguments.checkNull(edgeCapacity, "edgeCapacity");

                Graph<N,Double> flowGraph = new HashGraph<>();
                flowGraph.addAll(graph.nodes());

                if(source.equals(drain))
                    return new Flow<>(flowGraph, Double.POSITIVE_INFINITY, source, drain);

                Graph<N,Double> residual = new HashGraph<>();
                residual.addAll(graph.nodes());
                graph.forEach((n,m,e) -> {
                    double c = edgeCapacity.applyAsDouble(e);
                    if(c > 0)
                        residual.connect(n,m,c);
                    else if(c < 0)
                        throw new IllegalArgumentException("Graph has negative capacities: " + new Edge<>(n,m,e));
                });
                double flow = 0;

                Path<N,Double> p;
                while((p = Graphs.shortestTopologicalPath(residual, source, drain)) != null) {

                    double f = p.weights().stream().min(Comparator.naturalOrder()).get();
                    flow += f;

                    p.forEach((n,m,of) -> {
                        // Decrease potential flow increase
                        if(of == f)
                            residual.disconnect(n,m);
                        else residual.connect(n,m,of-f);
                        // Increase potential flow decrease
                        Double orf = residual.edge(m,n);
                        if(orf == null)
                            residual.connect(m,n,f);
                        else residual.connect(m,n,orf+f);
                        // Update current flow
                        orf = flowGraph.edge(m,n);
                        if(orf == null) {
                            Double of1 = flowGraph.edge(n,m);
                            if(of1 == null)
                                flowGraph.connect(n,m,f);
                            else flowGraph.connect(n,m,of1+f);
                        }
                        else if(orf > f)
                            flowGraph.connect(m,n,orf-f);
                        else {
                            flowGraph.disconnect(m,n);
                            if(orf != f)
                                flowGraph.connect(n,m,f-orf);
                        }
                    });
                }

                return new Flow<>(flowGraph, flow, source, drain);
            }
        };

        MaxFlowAlg DINIC = new MaxFlowAlg() {
            @SuppressWarnings("DuplicatedCode")
            @Override
            public @NotNull <N, E> Flow<N> compute(@NotNull ReadableGraph<N, E> graph, @NotNull N source, @NotNull N drain, @NotNull ToDoubleFunction<? super E> edgeCapacity) {
                Arguments.checkNull(graph,        "graph");
                Arguments.checkNull(source,       "source");
                Arguments.checkNull(drain,        "drain");
                Arguments.checkNull(edgeCapacity, "edgeCapacity");

                Graph<N,Double> flowGraph = new HashGraph<>();
                flowGraph.addAll(graph.nodes());

                if(source.equals(drain))
                    return new Flow<>(flowGraph, Double.POSITIVE_INFINITY, source, drain);

                Graph<N,Double> residual = new HashGraph<>();
                residual.addAll(graph.nodes());
                graph.forEach((n,m,e) -> {
                    double c = edgeCapacity.applyAsDouble(e);
                    if(c > 0)
                        residual.connect(n,m,c);
                    else if(c < 0)
                        throw new IllegalArgumentException("Graph has negative capacities: " + new Edge<>(n,m,e));
                });

                Graph<N,Double> levelGraph;
                while((levelGraph = MaxFlowAlg.levelGraph(residual, source, drain)) != null) {

                    blockingFlow(levelGraph, source, drain).forEach((n,m,f) -> {
                        // Decrease potential flow increase
                        Double of = residual.edge(n,m); // TODO: is Double needed?
                        if((double) of == f)
                            residual.disconnect(n,m);
                        else residual.connect(n,m,of-f);
                        // Increase potential flow decrease
                        Double orf = residual.edge(m,n);
                        if(orf == null)
                            residual.connect(m,n,f);
                        else residual.connect(m,n,orf+f);
                        // Update current flow
                        orf = flowGraph.edge(m,n);
                        if(orf == null) {
                            of = flowGraph.edge(n,m);
                            if(of == null)
                                flowGraph.connect(n,m,f);
                            else flowGraph.connect(n,m,of+f);
                        }
                        else if(orf > f)
                            flowGraph.connect(m,n,orf-f);
                        else {
                            flowGraph.disconnect(m, n);
                            if((double) orf != f)
                                flowGraph.connect(n, m, f - orf);
                        }
                    });
                }

                return new Flow<>(flowGraph, source, drain);
            }
        };

        @NotNull
        <N,E> Flow<N> compute(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N drain,
                              @NotNull ToDoubleFunction<? super E> edgeCapacity);

        @SuppressWarnings("DuplicatedCode")
        private static <N> Flow<N> blockingFlow(Graph<N,Double> levelGraph, N source, N drain) {

            assert !source.equals(drain);

            Graph<N,Double> flowGraph = new HashGraph<>();
            flowGraph.addAll(levelGraph.nodes());
            double flow = 0;

            Path<N,Double> p;
            while((p = Graphs.shortestTopologicalPath(levelGraph, source, drain)) != null) {

                double f = p.weights().stream().mapToDouble(c->c).min().getAsDouble();
                flow += f;

                p.forEach((n,m,of) -> {
                    // Decrease potential flow increase
                    if(of == f)
                        levelGraph.disconnect(n,m);
                    else levelGraph.connect(n,m,of-f);
                    // Update current flow
                    Double orf = flowGraph.edge(m,n);
                    if(orf == null) {
                        Double of1 = flowGraph.edge(n,m);
                        if(of1 == null)
                            flowGraph.connect(n,m,f);
                        else flowGraph.connect(n,m,of1+f);
                    }
                    else if(orf > f)
                        flowGraph.connect(m,n,orf-f);
                    else {
                        flowGraph.disconnect(m,n);
                        if(orf != f)
                            flowGraph.connect(n,m,f-orf);
                    }
                });
            }

            return new Flow<>(flowGraph, flow, source, drain);
        }

        private static <N> Graph<N,Double> levelGraph(ReadableGraph<N,Double> residual, N source, N drain) {
            Map<N,Integer> levels = new HashMap<>();
            levels.put(source, 0);
            for(N n : Graphs.traverseBreathFirst(residual, source)) {
                if(drain.equals(n)) break;
                int l = levels.get(n) + 1;
                for(N m : residual.adj(n))
                    levels.putIfAbsent(m,l);
            }
            if(!levels.containsKey(drain)) return null;

            Graph<N,Double> levelGraph = new HashGraph<>();
            // Don't add nodes manually: only include ones that are connected
            residual.forEach((n,m,c) -> {
                Integer nl = levels.get(n);
                if(nl == null) return;
                Integer ml = levels.get(m);
                if(ml == null || nl+1 != ml) return;
                assert c > 0;
                levelGraph.connect(n,m,c);
            });
            return levelGraph;
        }
    }

    private static <N,E> DistancePath<N,E> buildPath(ReadableGraph<N,E> graph, ToDoubleFunction<? super E> edgeLength, N target, Map<N,N> p) {
        return buildPath(graph, target, p, n -> new DistancePath<>(n, edgeLength));
    }

    private static <N,E,P extends Path<N,E>> P buildPath(ReadableGraph<N,E> graph, N target, Map<N,N> p, Function<N,P> pathCtor) {
        N next, current = target;
        P path = pathCtor.apply(current);
        while((current = p.get(next = current)) != null)
            path.insertBefore(0, current, graph.edge(current, next));
        return path;
    }
}
