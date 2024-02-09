/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.netutil.impl.payload;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.netcache.NetCache;
import com.kneelawk.graphlib.netutil.impl.NetUtilImpl;
import com.kneelawk.graphlib.netutil.impl.netcache.NetCacheImpl;

public record AssociateIdPayload(Identifier cacheId, int key, Object obj) implements CustomPayload {
    public static final Identifier ID = NetUtilImpl.id("associate_id");

    @SuppressWarnings("unchecked")
    public static AssociateIdPayload decode(PacketByteBuf buf) {
        Identifier cacheId = buf.readIdentifier();
        int key = buf.readVarInt();

        NetCache<Object> cache = (NetCache<Object>) NetCacheImpl.getCache(cacheId);
        if (cache == null) throw new IllegalArgumentException(
            "Attempted to decode AssociateByPayload with missing cache id: " + cacheId +
                ". This is likely an invalid packet.");
        Object obj = cache.getDecoder().decode(buf);

        return new AssociateIdPayload(cacheId, key, obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(PacketByteBuf buf) {
        buf.writeIdentifier(cacheId);
        buf.writeVarInt(key);

        NetCache<Object> cache = (NetCache<Object>) NetCacheImpl.getCache(cacheId);
        if (cache == null) throw new IllegalStateException(
            "Attempted to encode AssociateIdPayload with missing cache id: " + cacheId +
                ". Maybe it was not registered?");

        cache.getEncoder().encode(obj, buf);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
