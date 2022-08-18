package com.github.rccookie.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;

import com.github.rccookie.util.Arguments;
import com.github.rccookie.util.Console;
import com.github.rccookie.util.EmptyIteratorException;
import com.github.rccookie.util.IterableIterator;

import org.jetbrains.annotations.NotNull;

public class BinaryHeap<T> implements Heap<T> {

    private Object[] data = new Object[8];
    private final HashMap<Object,Integer> positions = new HashMap<>();
    private int size = 0;

    private final Comparator<? super T> comparator;

    private Queue<T> queue = null;

    @SuppressWarnings("unchecked")
    public BinaryHeap() {
        this((Comparator<? super T>) Comparator.naturalOrder());
    }

    public BinaryHeap(@NotNull Comparator<? super T> comparator) {
        this.comparator = Arguments.checkNull(comparator, "comparator");
    }

    @SuppressWarnings("unchecked")
    public BinaryHeap(Collection<? extends T> ts) {
        this(ts, (Comparator<? super T>) Comparator.naturalOrder());
    }

    public BinaryHeap(@NotNull Collection<? extends T> ts, @NotNull Comparator<? super T> comparator) {
        this(comparator);
        Arguments.checkNull(ts, "ts");
        int newSize = data.length;
        while(size + ts.size() > newSize) newSize *= 2;
        if(newSize != data.length)
            data = Arrays.copyOf(data, newSize);
        int i=0;
        for(T t : ts) {
            positions.put(t,i);
            data[i++] = t;
        }
        size = i;
        for(i/=2; i>=0; i--)
            sink(i);
    }


    @Override
    public boolean enqueue(T t) {
        Integer prev = positions.put(t, size);
        if(prev != null) {
            positions.put(t, prev);
            return false;
        }
        if(size == data.length)
            data = Arrays.copyOf(data, size * 2);
        data[size] = t;
        rise(size++);
        return true;
    }

    @Override
    public boolean enqueueAll(Collection<? extends T> ts) {
        int newSize = data.length;
        while(size + ts.size() > newSize) newSize *= 2;
        if(newSize != data.length)
            data = Arrays.copyOf(data, newSize);
        boolean diff = false;
        for(T t : ts) diff |= enqueue(t);
        return diff;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T dequeue() {
        swap(0, --size);
        sink(0);
        positions.remove(data[size]);
        return (T) data[size];
    }

    @SuppressWarnings("unchecked")
    @Override
    public T peek() {
        if(size == 0) throw new NoSuchElementException("Heap is empty");
        return (T) data[0];
    }

    @Override
    public boolean update(Object o) {
        Integer i = positions.get(o);
        if(i == null) return false;
        return heapify(i);
    }

    @Override
    public boolean updateIncreased(Object o) {
        Integer i = positions.get(o);
        if(i == null) return false;
        return sink(i);
    }

    @Override
    public boolean updateDecreased(Object o) {
        Integer i = positions.get(o);
        if(i == null)
            return false;
        return rise(i);
    }

    private boolean heapify(int i) {
        return sink(i) || rise(i);
    }

    @SuppressWarnings("unchecked")
    private boolean rise(int i) {
        boolean diff = false;
        int j;
        while(i != 0 && comparator.compare((T) data[i], (T) data[j = (i-1)/2]) < 0) {
            swap(i, i = j);
            diff = true;
        }
        return diff;
    }

    @SuppressWarnings("unchecked")
    private boolean sink(int i) {
        boolean diff = false;
        while(i < size / 2) {
            int j = 2*i+1;
            if(j < size-1 && comparator.compare((T) data[j+1], (T) data[j]) < 0)
                j++;
            if(comparator.compare((T) data[j], (T) data[i]) < 0)
                swap(i, i = j);
            else break;
        }
        return diff;
    }

    private void swap(int i, int j) {
        Object t = data[i];
        positions.put(data[i] = data[j], i);
        positions.put(data[j] = t,       j);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return positions.containsKey(o);
    }

    @Override
    public Queue<T> asQueue() {
        return queue != null ? queue : (queue = new Queue<>() {
            @Override
            public boolean add(T t) {
                return enqueue(t);
            }

            @Override
            public boolean offer(T t) {
                return add(t);
            }

            @Override
            public T remove() {
                return dequeue();
            }

            @Override
            public T poll() {
                return dequeue();
            }

            @Override
            public T element() {
                return BinaryHeap.this.peek();
            }

            @Override
            public T peek() {
                return BinaryHeap.this.peek();
            }

            @Override
            public int size() {
                return BinaryHeap.this.size();
            }

            @Override
            public boolean isEmpty() {
                return BinaryHeap.this.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return BinaryHeap.this.contains(o);
            }

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return new Iterator<>() {
                    int i = 0;
                    @Override
                    public boolean hasNext() {
                        return i < size;
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public T next() {
                        if(i >= size) throw new EmptyIteratorException();
                        return (T) data[i++];
                    }
                };
            }

            @NotNull
            @Override
            public Object @NotNull [] toArray() {
                return toArray(new Object[size]);
            }

            @NotNull
            @Override
            public <U> U @NotNull [] toArray(@NotNull U @NotNull [] a) {
                if(Arguments.checkNull(a, "a").length < size)
                    a = Arrays.copyOf(a, size);
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(data, 0, a, 0, size);
                return a;
            }

            @Override
            public boolean remove(Object o) {
                Integer i = positions.get(o);
                if(i == null) return false;
                swap(i, --size);
                sink(i);
                positions.remove(data[size]);
                return true;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                for(Object o : c) if(!contains(o)) return false;
                return true;
            }

            @Override
            public boolean addAll(@NotNull Collection<? extends T> c) {
                return enqueueAll(c);
            }

            @Override
            public boolean removeAll(@NotNull Collection<?> c) {
                boolean diff = false;
                for(Object o : c) diff |= remove(o);
                return diff;
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean retainAll(@NotNull Collection<?> c) {
                boolean diff = false;
                Object[] data = BinaryHeap.this.data;
                BinaryHeap.this.clear();
                for(Object o : data) {
                    if(c.contains(o)) {
                        diff = true;
                        enqueue((T) o);
                    }
                }
                return diff;
            }

            @Override
            public void clear() {
                BinaryHeap.this.clear();
            }
        });
    }

    @Override
    public @NotNull BinaryHeap<T> clone() {
        BinaryHeap<T> clone = new BinaryHeap<>(comparator);
        clone.data = data.clone();
        clone.size = size;
        clone.positions.putAll(positions);
        return clone;
    }

    @NotNull
    @Override
    public IterableIterator<T> iterator() {
        return new IterableIterator<>() {
            @Override
            public boolean hasNext() {
                return size != 0;
            }
            @Override
            public T next() {
                return dequeue();
            }
        };
    }

    @Override
    public void clear() {
        data = new Object[8];
        positions.clear();
        size = 0;
    }

    @Override
    public String toString() {
        return Arrays.toString(size == data.length ? data : Arrays.copyOf(data, size));
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof BinaryHeap)) return false;
        BinaryHeap<?> that = (BinaryHeap<?>) o;
        if(size == 0 && size == that.size) return true;
        return Objects.equals(data[0], that.data[0]) &&
                positions.keySet().equals(that.positions.keySet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(positions.keySet());
    }

    public static void main(String[] args) {
        Heap<Object> heap = new BinaryHeap<>();
        heap.enqueue(5);
        heap.enqueue(3);
        heap.enqueue(2);
        heap.enqueue(8);
        heap.enqueue(7);
        heap.enqueue(6);
        Console.log(heap.dequeue());
    }
}
