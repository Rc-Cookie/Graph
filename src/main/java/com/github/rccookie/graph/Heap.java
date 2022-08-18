package com.github.rccookie.graph;

import java.util.Collection;
import java.util.Queue;

import com.github.rccookie.util.Cloneable;
import com.github.rccookie.util.IterableIterator;

import org.jetbrains.annotations.NotNull;

public interface Heap<T> extends Iterable<T>, Cloneable<Heap<T>> {

    boolean enqueue(T t);

    boolean enqueueAll(Collection<? extends T> ts);

    T dequeue();

    T peek();

    boolean update(Object o);

    boolean updateIncreased(Object o);

    boolean updateDecreased(Object o);

    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Queue<T> asQueue();

    @NotNull
    @Override
    IterableIterator<T> iterator();

    void clear();
}
