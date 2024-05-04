package com.kneelawk.graphlib.api.util.codec;

import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

/**
 * Version of {@link com.mojang.serialization.codecs.KeyDispatchCodec} that stores dispatched content in a sub-map with a custom name.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class CustomKeyDispatchCodec<K, V> extends MapCodec<V> {
    private final String typeKey;
    private final String valueKey;
    private final Codec<K> keyCodec;
    private final Function<? super V, ? extends DataResult<? extends K>> type;
    private final Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder;
    private final Function<? super V, ? extends DataResult<? extends Encoder<V>>> encoder;

    /**
     * Creates a new {@link CustomKeyDispatchCodec}.
     *
     * @param typeKey  the key name where the key type will be stored.
     * @param valueKey the key name where the value type will be stored.
     * @param keyCodec the codec of the key type.
     * @param type     a function for retrieving the key when given a value.
     * @param decoder  a function for retrieving the value decoder when given a key.
     * @param encoder  a function for retrieving the value encoder when given a value.
     */
    public CustomKeyDispatchCodec(String typeKey, String valueKey, Codec<K> keyCodec,
                                  Function<? super V, ? extends DataResult<? extends K>> type,
                                  Function<? super K, ? extends DataResult<? extends Decoder<? extends V>>> decoder,
                                  Function<? super V, ? extends DataResult<? extends Encoder<V>>> encoder) {
        this.typeKey = typeKey;
        this.valueKey = valueKey;
        this.keyCodec = keyCodec;
        this.type = type;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    /**
     * Creates a new {@link CustomKeyDispatchCodec}.
     *
     * @param typeKey  the key name where the key type will be stored.
     * @param valueKey the key name where the value type will be store.
     * @param keyCodec the codec of the key type.
     * @param type     a function for retrieving the key when given a value.
     * @param codec    a function for retrieving the value codec when given a key.
     */
    public CustomKeyDispatchCodec(String typeKey, String valueKey, Codec<K> keyCodec,
                                  Function<? super V, ? extends DataResult<? extends K>> type,
                                  Function<? super K, ? extends DataResult<? extends Codec<? extends V>>> codec) {
        this(typeKey, valueKey, keyCodec, type, codec, v -> getCodec(type, codec, v));
    }

    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.of(typeKey, valueKey).map(ops::createString);
    }

    @Override
    public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
        final T elementName = input.get(typeKey);
        if (elementName == null) {
            return DataResult.error(() -> "Input does not contain a key[" + typeKey + "]: " + input);
        }

        return keyCodec.decode(ops, elementName)
            .flatMap(type -> decoder.apply(type.getFirst()).flatMap(elementDecoder -> {
                final T value = input.get(valueKey);
                if (value == null) {
                    return DataResult.error(() -> "Input does not contain a key [" + valueKey + "]: " + input);
                }
                return elementDecoder.parse(ops, value).map(Function.identity());
            }));
    }

    @Override
    public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
        final DataResult<? extends Encoder<V>> encoderResult = encoder.apply(input);
        if (encoderResult.isError()) {
            return prefix.withErrorsFrom(encoderResult);
        }

        final Encoder<V> elementEncoder = encoderResult.result().get();
        return prefix.add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)))
            .add(valueKey, elementEncoder.encodeStart(ops, input));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends Encoder<V>> getCodec(
        Function<? super V, ? extends DataResult<? extends K>> type,
        Function<? super K, ? extends DataResult<? extends Encoder<? extends V>>> encoder, V input) {
        return type.apply(input).flatMap(key -> encoder.apply(key).map(Function.identity()))
            .map(c -> ((Encoder<V>) c));
    }

    @Override
    public String toString() {
        return "CustomKeyDispatchCodec[" + keyCodec + " " + type + " " + decoder + "]";
    }
}
