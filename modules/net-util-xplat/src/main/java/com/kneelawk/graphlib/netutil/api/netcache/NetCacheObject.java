package com.kneelawk.graphlib.netutil.api.netcache;

import net.minecraft.network.PacketByteBuf;

/**
 * Describes an object that can be cached between client and server.
 */
public interface NetCacheObject {
    /**
     * Encodes this object to a packet so that it can be cached on the other side of the network.
     *
     * @param buf the buffer to write this object to.
     */
    void toPacket(PacketByteBuf buf);
}
