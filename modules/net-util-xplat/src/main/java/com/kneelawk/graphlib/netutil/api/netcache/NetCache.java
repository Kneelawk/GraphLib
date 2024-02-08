package com.kneelawk.graphlib.netutil.api.netcache;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

/**
 * Object for caching objects between client and server so that they can be referred to by integer ids.
 *
 * @param <T> the type of object being cached.
 */
public class NetCache<T extends NetCacheObject> {
    private final Identifier id;
    private final NetCacheObjectDecoder<T> decoder;

    public NetCache(Identifier id, NetCacheObjectDecoder<T> decoder) {
        this.id = id;
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

    public int encode(PacketListener connection, T obj) {

    }
}
