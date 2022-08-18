package com.github.rccookie.graph;

import java.util.Objects;

import com.github.rccookie.geometry.performance.floatN;
import com.github.rccookie.util.Arguments;

public class LocatedValue<L extends floatN<L,?>,T> {

    public final L location;
    public final T value;

    public LocatedValue(L location, T value) {
        this.location = Arguments.checkNull(location, "location").clone();
        this.value = value;
    }

    @Override
    public String toString() {
        return value + " at " + location;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LocatedValue && Objects.equals(value, ((LocatedValue<?,?>) o).value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
