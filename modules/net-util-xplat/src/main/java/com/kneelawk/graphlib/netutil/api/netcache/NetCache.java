package com.kneelawk.graphlib.netutil.api.netcache;

import net.minecraft.util.Identifier;

/**
 * Object for caching objects between client and server so that they can be referred to by integer ids.
 *
 * @param <T> the type of object being cached.
 */
public class NetCache<T> {
    private final Identifier id;
    private final NetCacheObjectEncoder<T> encoder;
    private final NetCacheObjectDecoder<T> decoder;

    private NetCache(Identifier id, NetCacheObjectEncoder<T> encoder, NetCacheObjectDecoder<T> decoder) {
        this.id = id;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Gets the id of this net cache.
     *
     * @return the id of this net cache.
     */
    public Identifier getId() {
        return id;
    }

    /**
     * Gets the encoder associated with this cache.
     *
     * @return the encoder associated with this cache.
     */
    public NetCacheObjectEncoder<T> getEncoder() {
        return encoder;
    }

    /**
     * Gets the decoder associated with this cache.
     *
     * @return the decoder associated with this cache.
     */
    public NetCacheObjectDecoder<T> getDecoder() {
        return decoder;
    }

    /**
     * Creates a new Net Cache.
     *
     * @param id      the id of this cache, used for identifying caches.
     * @param encoder the encoder for the objects being cached.
     * @param decoder the decoder for the objects being cached.
     * @param <T>     the type of object being cached.
     * @return a new Net Cache.
     */
    public static <T> NetCache<T> of(Identifier id, NetCacheObjectEncoder<T> encoder,
                                     NetCacheObjectDecoder<T> decoder) {
        return new NetCache<>(id, encoder, decoder);
    }
}
