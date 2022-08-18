package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.github.rccookie.util.ImmutabilityException;
import com.github.rccookie.util.IterableIterator;
import com.github.rccookie.util.IterableMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Forest<N,E> extends ReadableGraph<N,E> {

    Forest<Object,Object> EMPTY = new Forest<>() {
        @Override
        public void add(@NotNull Object node, @Nullable Object parent, Object edge) {
            throw new ImmutabilityException();
        }

        @Override
        public void addRoot(@NotNull Object node) {
            throw new ImmutabilityException();
        }

        @Override
        public void addTree(@NotNull Tree<?, ?> tree) {
            throw new ImmutabilityException();
        }

        @Override
        public void addTree(@NotNull Tree<?, ?> tree, @Nullable Object parent, Object edge) {
            throw new ImmutabilityException();
        }

        @Override
        public void set(@NotNull Object old, @NotNull Object now) {
            throw new ImmutabilityException();
        }

        @Override
        public boolean disconnect(@NotNull Object node) {
            return false;
        }

        @Override
        public @NotNull Forest<Object, Object> clone() {
            return this;
        }

        @Override
        public @NotNull Forest<Object, Object> newInstance() {
            return this;
        }

        @Override
        public Set<Object> roots() {
            return Set.of();
        }

        @Override
        public IterableMap<Object, Object> adj(Object node) {
            return null;
        }

        @Override
        public Map<Object, ? extends Map<Object, Object>> adjacencyList() {
            return Map.of();
        }

        @Override
        public Object edge(@NotNull Object a, @NotNull Object b) {
            return null;
        }

        @Override
        public boolean connected(@NotNull Object a, @NotNull Object b) {
            return false;
        }

        @Override
        public Set<Object> nodes() {
            return Set.of();
        }

        @Override
        public @NotNull Set<? extends Edge<Object, Object>> edges() {
            return Set.of();
        }

        @Override
        public @NotNull Collection<Object> weights() {
            return Set.of();
        }

        @Override
        public boolean contains(Object node) {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int edgeCount() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public void forEach(EdgeConsumer<? super Object,? super Object> action) {
        }

        @Override
        public void clear() {
            throw new ImmutabilityException();
        }

        @NotNull
        @Override
        public Iterator<Object> iterator() {
            return IterableIterator.empty();
        }
    };

    void add(@NotNull N node, @Nullable N parent, E edge);

    void addRoot(@NotNull N node);

    void addTree(@NotNull Tree<? extends N, ? extends E> tree);

    void addTree(@NotNull Tree<? extends N, ? extends E> tree, @Nullable N parent, E edge);

    void set(@NotNull Object old, @NotNull N now);

    boolean disconnect(@NotNull Object node);

    void clear();


    default Set<Tree<N,E>> getTrees() {
        return Graphs.trees(this);
    }

    default int componentCount() {
        return Graphs.componentCount(this);
    }


    Set<N> roots();


    @Override
    default boolean isDirected() {
        return true;
    }

    @NotNull Forest<N,E> clone();

    @NotNull Forest<N,E> newInstance();


    @SuppressWarnings("unchecked")
    static <N,E> Forest<N,E> empty() {
        return (Forest<N,E>) EMPTY;
    }
}
