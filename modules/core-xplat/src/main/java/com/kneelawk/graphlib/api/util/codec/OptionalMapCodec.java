package com.kneelawk.graphlib.api.util.codec;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

/**
 * Map codec that requires a given set of keys in order to even consider decoding.
 *
 * @param <O> the type that this map codec wraps.
 */
public class OptionalMapCodec<O> extends MapCodec<Optional<O>> {
    private final Collection<String> requiredKeys;
    private final MapCodec<O> wrapped;

    /**
     * Creates an {@link OptionalMapCodec}.
     *
     * @param requiredKeys the keys that must be present in order to decode the wrapped type.
     * @param wrapped      the map codec for the wrapped type.
     */
    public OptionalMapCodec(Collection<String> requiredKeys, MapCodec<O> wrapped) {
        this.requiredKeys = requiredKeys;
        this.wrapped = wrapped;
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return requiredKeys.stream().map(ops::createString);
    }

    @Override
    public <T> DataResult<Optional<O>> decode(DynamicOps<T> ops, MapLike<T> input) {
        for (String key : requiredKeys) {
            if (input.get(key) == null) {
                return DataResult.success(Optional.empty());
            }
        }

        return wrapped.decode(ops, input).map(Optional::of);
    }

    @Override
    public <T> RecordBuilder<T> encode(Optional<O> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            prefix = wrapped.encode(input.get(), ops, prefix);
        }

        return prefix;
    }
}
