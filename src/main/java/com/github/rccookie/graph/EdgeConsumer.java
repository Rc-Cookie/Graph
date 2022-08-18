package com.github.rccookie.graph;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface EdgeConsumer<N,E> {

    void accept(@NotNull N a, @NotNull N b, E e);
}
