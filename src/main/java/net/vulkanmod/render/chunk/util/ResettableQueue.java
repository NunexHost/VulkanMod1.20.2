package net.vulkanmod.render.chunk.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.function.Consumer;

public class ResettableQueue<T> implements Iterable<T> {
    T[] queue;
    int position = 0;
    int limit = 0;
    int capacity;

    public ResettableQueue() {
        this(1024);
    }

    @SuppressWarnings("unchecked")
    public ResettableQueue(int initialCapacity) {
        this.capacity = initialCapacity;

        this.queue = (T[])(new Object[capacity]);
    }

    public boolean hasNext() {
        return position < limit;
    }

    public T poll() {
        T t = this.queue[position];
        this.position++;

        return t;
    }

    public void add(T t) {
        if(t == null)
            return;

        if(limit == capacity) resize();
        this.queue[limit++] = t;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        this.capacity *= 2;

        T[] oldQueue = this.queue;
        this.queue = (T[])(new Object[capacity]);

        System.arraycopy(oldQueue, 0, this.queue, 0, position);
    }

    public int size() {
        return limit;
    }

    public void clear() {
        this.position = 0;
        this.limit = 0;
    }

    public Iterator<T> iterator(boolean reverseOrder) {
        return reverseOrder ?
                () -> {
                    int pos = limit - 1;

                    while (pos >= 0) {
                        yield queue[pos--];
                    }
                }
                :
                () -> {
                    int pos = 0;

                    while (pos < limit) {
                        yield queue[pos++];
                    }
                };
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return iterator(false);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < limit; i++) {
            action.accept(queue[i]);
        }
    }
}
