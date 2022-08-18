package com.github.rccookie.graph;

import java.util.List;
import java.util.function.ToDoubleFunction;

import com.github.rccookie.util.Arguments;

import org.jetbrains.annotations.NotNull;

public class DistancePath<N,E> extends ArrayPath<N,E> {

    private double distance = 0;
    private final ToDoubleFunction<? super E> toDouble;

    public DistancePath(N start, ToDoubleFunction<? super E> edgeLength) {
        super(start);
        this.toDouble = edgeLength;
    }

    DistancePath(List<N> nodes, List<E> edges, ToDoubleFunction<? super E> toDouble, double distance) {
        super(nodes, edges);
        this.toDouble = toDouble;
        this.distance = distance;
    }

    public double distance() {
        return distance;
    }


    @Override
    public boolean append(N node, E edge) {
        if(super.append(node, edge)) {
            distance += toDouble.applyAsDouble(edge);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertBefore(int index, N node, E edge) {
        if(super.insertBefore(index, node, edge)) {
            distance += toDouble.applyAsDouble(edge);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertAfter(int index, N node, E edge) {
        if(super.insertAfter(index, node, edge)) {
            distance += toDouble.applyAsDouble(edge);
            return true;
        }
        return false;
    }

    @Override
    public E setEdgeBefore(int index, E edge) {
        E old = super.setEdgeBefore(index, edge);
        distance += toDouble.applyAsDouble(edge) - toDouble.applyAsDouble(old);
        return old;
    }

    @Override
    public E setEdgeAfter(int index, E edge) {
        E old = super.setEdgeAfter(index, edge);
        distance += toDouble.applyAsDouble(edge) - toDouble.applyAsDouble(old);
        return old;
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean remove(Object node) {
        if(!nodesSet.remove(Arguments.checkNull(node, "node"))) return false;
        int index = nodes.indexOf(node);
        nodes.remove(index);
        distance -= toDouble.applyAsDouble(edges.remove(Math.max(0, index-1)));
        return true;
    }

    @Override
    public N remove(int index) {
        if(edges.isEmpty()) throw new IllegalStateException("Path cannot be empty");
        if(index < 0 || index >= nodes.size())
            throw new IndexOutOfBoundsException(index);
        N node = nodes.remove(index);
        nodesSet.remove(node);
        distance -= toDouble.applyAsDouble(edges.remove(Math.max(0, index-1)));
        return node;
    }

    @Override
    public boolean clear() {
        distance = 0;
        return super.clear();
    }

    @Override
    public @NotNull DistancePath<N,E> clone() {
        return new DistancePath<>(nodes, edges, toDouble, distance);
    }

    @Override
    public String toString() {
        return super.toString() + " (distance: " + distance + ')';
    }
}
