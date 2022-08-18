package com.github.rccookie.graph;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToDoubleFunction;

import com.github.rccookie.geometry.performance.floatN;
import com.github.rccookie.geometry.performance.int2;
import com.github.rccookie.geometry.performance.intN;
import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.ListStream;
import com.github.rccookie.util.RecursiveStepIterator;
import com.github.rccookie.util.Stopwatch;
import com.github.rccookie.util.T2;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import static com.github.rccookie.util.Tuples.*;

public final class Graphs {

    private Graphs() {
        throw new UnsupportedOperationException();
    }


    @NotNull
    public static <N> ListStream<N> traversePrefix(Tree<N,?> tree) {
        if(tree.isEmpty()) return ListStream.empty();
        return traverseDepthFirst(tree, tree.root());
    }

    @NotNull
    public static <N> ListStream<N> traversePostfix(Tree<N,?> tree) {
        if(tree.isEmpty()) return ListStream.empty();

        Deque<T2<N,Iterator<N>>> queued = new ArrayDeque<>();
        queued.add(t(tree.root(), tree.adj(tree.root()).iterator()));

        return ListStream.of((Iterator<N>) new RecursiveStepIterator<N>() {
            @Override
            protected N getNext() {
                if(queued.isEmpty()) return null;
                Iterator<N> it;
                while((it = queued.getFirst().b).hasNext()) {
                    N n = it.next();
                    queued.add(t(n, tree.adj(n).iterator()));
                }
                return queued.pop().a;
            }
        });
    }

    @NotNull
    public static <N> ListStream<N> traverseBreathFirst(@NotNull ReadableGraph<N,?> graph, @NotNull N source) {
        Arguments.checkNull(graph, "graph");
        Arguments.checkNull(source, "source");
        if(!graph.contains(source)) return ListStream.empty();
        return traverse(graph, source, Deque::poll, new HashSet<>());
    }

    @NotNull
    public static <N> ListStream<N> traverseBreathFirst(ReadableGraph<N,?> graph) {
        return traverse(graph, Deque::poll);
    }

    @NotNull
    public static <N> ListStream<N> traverseDepthFirst(@NotNull ReadableGraph<N,?> graph, @NotNull N source) {
        Arguments.checkNull(graph, "graph");
        Arguments.checkNull(source, "source");
        if(!graph.contains(source)) return ListStream.empty();
        return traverse(graph, source, Deque::pop, new HashSet<>());
    }

    @NotNull
    public static <N> ListStream<N> traverseDepthFirst(ReadableGraph<N,?> graph) {
        return traverse(graph, Deque::pop);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static <N> ListStream<N> traverse(ReadableGraph<N,?> graph, Function<Deque<N>,N> removeFunction) {
        if(graph instanceof Tree)
            return traverse(graph, ((Tree<N,?>) graph).root(), removeFunction, new HashSet<>());

        Set<N> visited = new HashSet<>();
        Iterator<N> nodes = graph.iterator();
        return ListStream.of((Iterator<N>) new RecursiveStepIterator<N>() {
            @Override
            protected N getNext() {
                while(!it.hasNext() && nodes.hasNext())
                    it = traverse(graph, nodes.next(), removeFunction, visited).iterator();
                return it.hasNext() ? it.next() : null;
            }
        });
    }

    @NotNull
    private static <N> ListStream<N> traverse(ReadableGraph<N,?> graph, N source, Function<Deque<N>,N> removeFunction, Set<N> visited) {
        if(visited.contains(source)) return ListStream.empty();

        Deque<N> queued = new ArrayDeque<>();
        queued.add(source);

        return ListStream.of((Iterator<N>) new RecursiveStepIterator<N>() {
            @Override
            protected N getNext() {
                N next;
                do {
                    if(queued.isEmpty()) return null;
                    next = removeFunction.apply(queued);
                    for (N n : graph.adj(next))
                        if(!visited.contains(n))
                            queued.add(n);
                } while(!visited.add(next));
                return next;
            }
        });
    }

    @NotNull
    public static <N,E> Tree<N,E> spanningTree(ReadableGraph<N,E> graph, N root) {
        Arguments.checkNull(graph, "graph");
        Arguments.checkNull(root, "root");

        if(!graph.contains(root)) return Tree.empty();

        Tree<N,E> tree = new HashTree<>(root);

        Queue<N> queue = new ArrayDeque<>();
        queue.add(root);

        while(!queue.isEmpty()) {
            N n = queue.remove();
            graph.adj(n).forEach((m,e) -> {
                if(!tree.contains(m)) {
                    tree.add(m, n, e);
                    queue.add(m);
                }
            });
        }
        return tree;
    }

    public static <N,E extends Comparable<? super E>> Tree<N,E> minimalSpanningTree(ReadableGraph<N,? extends E> graph) {
        return Graphs.minimalSpanningTree(graph, Comparator.naturalOrder());
    }

    public static <N,E> Tree<N,E> minimalSpanningTree(ReadableGraph<N,? extends E> graph, Comparator<? super E> comparator) {
        return minimalSpanningTree(graph, comparator, Algorithm.MST.PRIM);
    }

    public static <N,E> Tree<N,E> minimalSpanningTree(ReadableGraph<N,? extends E> graph, Comparator<? super E> comparator, Algorithm.MST algorithm) {
        return algorithm.compute(graph, comparator);
    }

    @NotNull
    public static <N,E> Set<Tree<N,E>> trees(Forest<N,E> forest) {
        Set<Tree<N,E>> trees = new HashSet<>();
        for(N root : forest.roots()) {
            Tree<N,E> tree = new HashTree<>(root);
            for(N n : traverseBreathFirst(forest, root))
                forest.adj(n).forEach((m,e) -> tree.add(m,n,e));
            trees.add(tree);
        }
        return trees;
    }

    @NotNull
    public static <N,E> Set<Graph<N,E>> components(@NotNull ReadableGraph<N,E> graph) {
        Arguments.checkNull(graph, "graph");
        Set<Graph<N,E>> components = new HashSet<>();
        if(graph instanceof Forest)
            for(N root : ((Forest<N,E>) graph).roots())
                components.add(getComponent(graph, root));
        else {
            Set<N> visited = new HashSet<>();
            for(N r : graph) {
                if(!visited.add(r)) continue;
                Graph<N, E> component = getComponent(graph, r);
                components.add(component);
                visited.addAll(component);
            }
        }
        return components;
    }

    @NotNull
    public static <N,E> Graph<N,E> getComponent(@NotNull ReadableGraph<N,E> graph, @NotNull N source) {
        Arguments.checkNull(graph, "graph");
        Arguments.checkNull(source, "source");
        if(!graph.contains(source)) return new HashGraph<>(graph.isDirected());

        Graph<N,E> component = new HashGraph<>(graph.isDirected());
        component.addAll(traverseBreathFirst(graph, source));
        for(N n : component)
            graph.adj(n).forEach((m,e) -> component.connect(n,m,e));
        return component;
    }

    public static int componentCount(ReadableGraph<?,?> graph) {
        if(graph instanceof Forest)
            return graph.size() - graph.edgeCount();
        return components(graph).size();
    }

    public static <N,E> @NotNull T2<Map<N,Integer>, Tree<N,E>> topologicalDistance(@NotNull ReadableGraph<N,E> graph, @NotNull N source) {

        Arguments.checkNull(graph,  "graph");
        Arguments.checkNull(source, "source");

        if(!graph.contains(source))
            return t(Map.of(), Tree.empty());

        Map<N,Integer> dist = new HashMap<>();
        dist.put(source, 0);
        Tree<N,E> tree = new HashTree<>(source);

        Queue<N> queue = new ArrayDeque<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            N n = queue.remove();
            graph.adj(n).forEach((m,e) -> {
                if(tree.contains(m)) return;
                tree.add(m, n, e);
                dist.put(m, dist.get(n) + 1);
            });
        }

        for(N n : graph)
            if(!tree.contains(n))
                dist.put(n,-1);
        return t(dist, tree);
    }


    // ------------------------------------
    // Single-Source Single-Destination
    // ------------------------------------

    public static <N,E> Path<N,E> shortestTopologicalPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target) {
        return Algorithm.AnyPath.BREATH_FIRST.compute(graph, source, target);
    }

    public static <N, E> Path<N,E> anyPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target) {
        return anyPath(graph, source, target, Algorithm.AnyPath.BREATH_FIRST);
    }

    public static <N,E> Path<N,E> anyPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                          @NotNull Algorithm.AnyPath algorithm) {
        return Arguments.checkNull(algorithm, "algorithm")
                .compute(graph, source, target);
    }

    public static <N extends intN<N,?>, E extends Number> DistancePath<N,E> shortPath(@NotNull ReadableGraph<N,E> graph,
                                          @NotNull N source, @NotNull N target) {
        return shortestPath(graph, source, target, Number::doubleValue, intN::sqrDist);
    }

    public static <N extends floatN<N,?>, E extends Number> DistancePath<N,E> shortPath(@NotNull ReadableGraph<N,E> graph,
                                                                                        @NotNull N source, @NotNull N target) {
        return shortestPath(graph, source, target, Number::doubleValue, (a,b) -> a.sqrDist(b));
    }

    public static <N extends intN<N,?>, E extends Number> DistancePath<N,E> shortestPath(@NotNull ReadableGraph<N,E> graph,
                                                                                         @NotNull N source, @NotNull N target) {
        return shortestPath(graph, source, target, Number::doubleValue, intN::dist);
    }

    public static <N extends floatN<N,?>, E extends Number> DistancePath<N,E> shortestPath(@NotNull ReadableGraph<N,E> graph,
                                                                                           @NotNull N source, @NotNull N target) {
        return shortestPath(graph, source, target, Number::doubleValue, (a,b) -> a.dist(b));
    }

    public static <N,E> DistancePath<N,E> shortestPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                                       @NotNull ToDoubleFunction<? super E> edgeLength) {
        return shortestPath(graph, source, target, edgeLength, null);
    }

    public static <N,E> DistancePath<N,E> shortestPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                                       @NotNull ToDoubleFunction<? super E> edgeLength, @Nullable ToDoubleBiFunction<? super N, ? super N> heuristic) {
        return shortestPath(graph, source, target, edgeLength, heuristic, Algorithm.ShortestPath.A_STAR);
    }

    public static <N,E> DistancePath<N,E> shortestPath(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N target,
                                                       @NotNull ToDoubleFunction<? super E> edgeLength, @Nullable ToDoubleBiFunction<? super N, ? super N> heuristic,
                                                       @NotNull Algorithm.ShortestPath algorithm) {
        return Arguments.checkNull(algorithm, "algorithm")
                .compute(graph, source, target, edgeLength, heuristic);
    }

    // ------------------------------------
    // Single-Source All-Destinations
    // ------------------------------------

    @NotNull
    public static <N,E extends Number> MapGraph<N,Double,E> shortestPaths(@NotNull ReadableGraph<N,E> graph, @NotNull N source) {
        return shortestPaths(graph, source, Number::doubleValue);
    }

    @NotNull
    public static <N,E> MapGraph<N,Double,E> shortestPaths(@NotNull ReadableGraph<N,E> graph, @NotNull N source,
                                                           @NotNull ToDoubleFunction<? super E> edgeLength) {
        return shortestPaths(graph, source, edgeLength, Algorithm.SingleSourceShortestPath.DIJKSTRA);
    }

    @NotNull
    public static <N,E> MapGraph<N,Double,E> shortestPaths(@NotNull ReadableGraph<N,E> graph, @NotNull N source,
                                                           @NotNull ToDoubleFunction<? super E> edgeLength,
                                                           @NotNull Algorithm.SingleSourceShortestPath algorithm) {
        return Arguments.checkNull(algorithm, "algorithm")
                .compute(graph, source, edgeLength);
    }

    // ------------------------------------
    // Any-Source All-Destinations
    // ------------------------------------

    @NotNull
    public static <N,E extends Number> ShortestPaths<N,E> allShortestPaths(@NotNull ReadableGraph<N,E> graph) {
        return allShortestPaths(graph, Number::doubleValue);
    }

    @NotNull
    public static <N,E> ShortestPaths<N,E> allShortestPaths(@NotNull ReadableGraph<N,E> graph,
                                                            @NotNull ToDoubleFunction<? super E> edgeLength) {
        return allShortestPaths(graph, edgeLength, Algorithm.AllPairsShortestPath.FLOYD_WARSHALL);
    }

    @NotNull
    public static <N,E> ShortestPaths<N,E> allShortestPaths(@NotNull ReadableGraph<N,E> graph,
                                                            @NotNull ToDoubleFunction<? super E> edgeLength,
                                                            @NotNull Algorithm.AllPairsShortestPath algorithm) {
        return Arguments.checkNull(algorithm, "algorithm")
                .compute(graph, edgeLength);
    }



    @NotNull
    public static <N,E extends Number> ReadableGraph<N,Double> transitiveClosure(@NotNull ReadableGraph<N,E> graph) {
        return allShortestPaths(graph).distances();
    }

    @NotNull
    public static <N,E> ReadableGraph<N,Double> transitiveClosure(@NotNull ReadableGraph<N,E> graph,
                                                                  @NotNull ToDoubleFunction<? super E> edgeLength) {
        return allShortestPaths(graph, edgeLength).distances();
    }

    @NotNull
    public static <N,E> ReadableGraph<N,Double> transitiveClosure(@NotNull ReadableGraph<N,E> graph,
                                                                  @NotNull ToDoubleFunction<? super E> edgeLength,
                                                                  @NotNull Algorithm.AllPairsShortestPath algorithm) {
        return allShortestPaths(graph, edgeLength, algorithm).distances();
    }



    // ------------------------------------
    // Max Flow
    // ------------------------------------



    @NotNull
    public static <N,E extends Number> Flow<N> maxFlow(@NotNull ReadableGraph<N,E> graph,
                                                       @NotNull N source, @NotNull N drain) {
        return maxFlow(graph, source, drain, Number::doubleValue);
    }

    @NotNull
    public static <N,E> Flow<N> maxFlow(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N drain,
                                        @NotNull ToDoubleFunction<? super E> edgeCapacity) {
        return maxFlow(graph, source, drain, edgeCapacity, Algorithm.MaxFlowAlg.DINIC); // TODO: Change algorithm
    }

    @NotNull
    public static <N,E> Flow<N> maxFlow(@NotNull ReadableGraph<N,E> graph, @NotNull N source, @NotNull N drain,
                                        @NotNull ToDoubleFunction<? super E> edgeCapacity,
                                        @NotNull Algorithm.MaxFlowAlg algorithm) {
        return Arguments.checkNull(algorithm, "algorithm")
                .compute(graph, source, drain, edgeCapacity);
    }



    // ------------------------------------
    // Graph generation
    // ------------------------------------


    @NotNull
    @Contract(value = "_,_->new", pure = true)
    public static Graph<int2,Integer> grid2d(@Range(from = 0, to = Integer.MAX_VALUE) int w, @Range(from = 0, to = Integer.MAX_VALUE) int h) {
        return grid2d(w, h, int2::new, 1, false);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Contract(value = "_,_,_,_,_->new", pure = true)
    public static <N,E> Graph<N,E> grid2d(@Range(from = 0, to = Integer.MAX_VALUE) int w, @Range(from = 0, to = Integer.MAX_VALUE) int h,
                                          @NotNull BiFunction<? super Integer, ? super Integer, ? extends N> nodeGenerator, E weight, boolean directed) {

        Arguments.checkNull(nodeGenerator, "nodeGenerator");
        Arguments.checkRange(w, 0, null);
        Arguments.checkRange(h, 0, null);

        Graph<N,E> graph = new HashGraph<>(directed);
        Object[][] nodes = new Object[w][h];

        for(int i=0; i<w; i++) for(int j=0; j<h; j++) {
            N n = nodeGenerator.apply(i,j);
            nodes[i][j] = n;
            if(!graph.add(n)) throw new IllegalArgumentException("Duplicate node received from generator: " + n);
            if(i != 0) {
                graph.connect((N) nodes[i-1][j], n, weight);
                if(directed)
                    graph.connect(n, (N) nodes[i-1][j], weight);
            }
            if(j != 0) {
                graph.connect((N) nodes[i][j-1], n, weight);
                if(directed)
                    graph.connect(n, (N) nodes[i][j-1], weight);
            }
        }

        return graph;
    }


    public static void main(String[] args) {
//        Graph<String,Integer> g = new HashGraph<>();
//        g.connect("q", "a", 7);
//        g.connect("q", "b", 5);
//        g.connect("a", "b", 1);
//        g.connect("a", "c", 5);
//        g.connect("b", "s", 8);
//        g.connect("c", "b", 3);
//        g.connect("c", "s", 2);

//        g.connect("q", "a", 7);
//        g.connect("q", "b", 5);
//        g.connect("a", "b", 7);
//        g.connect("a", "c", 5);
//        g.connect("b", "c", 8);
//        g.connect("b", "d", 5);
//        g.connect("c", "s", 8);
//        g.connect("d", "c", 7);
//        g.connect("d", "s", 3);

        Stopwatch overall = new Stopwatch().start();

        Console.log("Generating graph...");
        Graph<int2,Integer> g = grid2d(1000, 100);
        Random r = new Random();
        g.forEach((n,m,e) -> g.connect(n,m, r.nextInt(1000)));
//        log(g);

        Console.log("Edmonds-Karp...");
        Stopwatch watch = new Stopwatch().start();
        Flow<int2> maxFlow = maxFlow(g, new int2(50,50), new int2(940,50), Integer::doubleValue, Algorithm.MaxFlowAlg.EDMONDS_KARP);
        Console.map("Time", watch);
        Console.map("Max flow", maxFlow.flow);
//        log(maxFlow);

        Console.log("Dinic...");
        watch.restart();
        maxFlow = maxFlow(g, new int2(0,50), new int2(99,50), Integer::doubleValue, Algorithm.MaxFlowAlg.DINIC);
        Console.map("Time", watch);
        Console.map("Max flow", maxFlow.flow);
//        log(maxFlow);

//
//        Console.log("All shortest paths...");
//        Console.log(Stopwatch.timed(() -> shortestPaths(g, new int2(0,0))));
//
//        Console.log("Exact shortest path...");
//        Console.log(Stopwatch.timed(() -> shortestPath(g, new int2(500,500), new int2(500, 999))));
//
//        Console.log("Fast short path...");
//        Console.log(Stopwatch.timed(() -> shortPath(g, new int2(500,500), new int2(500, 999))));

        Console.map("Overall time", overall);
//        Console.map("Shortest path", aStar(g, new int2(0,0), new int2(999,999)));
    }

    private static void log(ReadableGraph<?,?> g) {
        Console.log(g+":");
        Console.log(g.nodes());
        Console.log(g.edges());
    }

    private static void log(MapGraph<?,?,?> g) {
        Console.log(g+":");
        Console.log(g.entrySet());
        Console.log(g.edges());
    }
}
