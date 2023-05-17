package com.kneelawk.graphlib.impl.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class ReadOnlyMappingSet<T, R> implements Set<R> {
    private final Set<T> delegate;
    private final Function<T, R> mapper;

    public ReadOnlyMappingSet(Set<T> delegate, Function<T, R> mapper) {
        this.delegate = delegate;
        this.mapper = mapper;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @NotNull
    @Override
    public Iterator<R> iterator() {
        return new MappingIterator(delegate.iterator());
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return delegate.stream().map(mapper).toArray();
    }

    @NotNull
    @Override
    public <E> E @NotNull [] toArray(@NotNull E @NotNull [] a) {
        return delegate.stream().map(mapper).toList().toArray(a);
    }

    @Override
    public boolean add(R r) {
        throw new UnsupportedOperationException("This set is read-only");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("This set is read-only");
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        Set<R> set = delegate.stream().map(mapper).collect(Collectors.toSet());
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends R> c) {
        throw new UnsupportedOperationException("This set is read-only");
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("This set is read-only");
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        throw new UnsupportedOperationException("This set is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This set is read-only");
    }

    private class MappingIterator implements Iterator<R> {
        private final Iterator<T> input;

        private MappingIterator(Iterator<T> input) {
            this.input = input;
        }

        /**
         * Returns {@code true} if the iteration has more elements.
         * (In other words, returns {@code true} if {@link #next} would
         * return an element rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return input.hasNext();
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public R next() {
            return mapper.apply(input.next());
        }
    }
}
