package com.kneelawk.graphlib.impl.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReadOnlyMappingMap<K, V, R> implements Map<K, R> {
    private final Map<K, V> delegate;
    private final Function<V, R> mapper;
    private final Function<R, @Nullable V> reverseMapper;

    public ReadOnlyMappingMap(Map<K, V> delegate, Function<V, R> mapper, Function<R, @Nullable V> reverseMapper) {
        this.delegate = delegate;
        this.mapper = mapper;
        this.reverseMapper = reverseMapper;
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
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean containsValue(Object value) {
        return delegate.containsValue(reverseMapper.apply((R) value));
    }

    @Override
    public R get(Object key) {
        return mapper.apply(delegate.get(key));
    }

    @Nullable
    @Override
    public R put(K key, R value) {
        throw new UnsupportedOperationException("This map is read-only");
    }

    @Override
    public R remove(Object key) {
        throw new UnsupportedOperationException("This map is read-only");
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends R> m) {
        throw new UnsupportedOperationException("This map is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This map is read-only");
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @NotNull
    @Override
    public Collection<R> values() {
        return new ReadOnlyMappingCollection<>(delegate.values(), mapper);
    }

    @NotNull
    @Override
    public Set<Entry<K, R>> entrySet() {
        return new ReadOnlyMappingSet<>(delegate.entrySet(), entry -> Map.entry(entry.getKey(), mapper.apply(entry.getValue())));
    }
}
