package com.kneelawk.graphlib.netutil.api.netcache;

import net.minecraft.network.PacketByteBuf;

/**
 * Used for decoding an object being cached.
 *
 * @param <T> the type of object being cached.
 */
public interface NetCacheObjectDecoder<T extends NetCacheObject> {
    /**
     * Decodes an object from a packet so that it can be cached.
     *
     * @param buf the buffer to read the object from.
     * @return the newly decoded object.
     */
    T decode(PacketByteBuf buf);
}
