package com.github.rccookie.graph;

import com.github.rccookie.util.Arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HashTree<N,E> extends HashForest<N,E> implements Tree<N,E> {

    private N root;


    public HashTree() {
    }

    public HashTree(@Nullable N root) {
        if(root != null)
            graph.add(this.root = root);
    }


    @Override
    public void add(@NotNull N node, @Nullable N parent, E edge) {
        if(Arguments.checkNull(node, "node") == parent || parent == null)
            addRoot(node);
        else super.add(node, parent, edge);
    }

    @Override
    public void addRoot(@NotNull N node) {
        if(root != null) throw new IllegalStateException("Tree already has a root");
        graph.add(root = node);
    }

    @Override
    public void addTree(@NotNull Tree<? extends N, ? extends E> tree, @Nullable N parent, E edge) {
        if(parent == null && root != null)
            throw new IllegalStateException("Tree already has a root");
        super.addTree(tree, parent, edge);
    }

    @Override
    public void set(@NotNull Object old, @NotNull N now) {
        Arguments.checkNull(old, "old");
        Arguments.checkNull(now, "now");
        if(root == old) root = now;
        super.set(old, now);
    }

    @Override
    public boolean disconnect(@NotNull Object node) {
        if(!contains(node)) return false;
        if(node == root)
            clear();
        else super.disconnect(node);
        return true;
    }

    @Override
    public N root() {
        return root;
    }

    @Override
    public void clear() {
        super.clear();
        root = null;
    }

    @Override
    public @NotNull HashTree<N,E> clone() {
        HashTree<N,E> clone = new HashTree<>();
        if(root != null) {
            clone.addRoot(root);
            for(Edge<N,E> e : edges())
                add(e.b, e.a, e.value);
        }
        return clone;
    }

    @Override
    public @NotNull HashTree<N,E> newInstance() {
        return new HashTree<>();
    }

    @Override
    public String toString() {
        return "Tree with " + size() + " nodes " + nodes();
    }
}
