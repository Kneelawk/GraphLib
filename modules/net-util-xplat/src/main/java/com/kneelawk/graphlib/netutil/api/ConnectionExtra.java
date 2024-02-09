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

package com.kneelawk.graphlib.netutil.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.payload.CustomPayload;

import com.kneelawk.graphlib.netutil.api.netcache.NetCache;

/**
 * NetUtil representation of a connection.
 */
public interface ConnectionExtra {
    /**
     * Get the backing packet listener of this connection.
     *
     * @return this connection's backing packet listener.
     */
    @NotNull PacketListener getConnection();

    /**
     * Send a custom payload along this connection.
     *
     * @param payload the payload to send.
     */
    void sendPayload(CustomPayload payload);

    /**
     * Gets the integer id associated with the given object.
     * <p>
     * This potentially sends a packet to associate a new object and id pair if no association exists for the supplied object.
     *
     * @param cache the cache to look up the association in.
     * @param obj   the object to look up the association for.
     * @param <T>   the type of object to look up.
     * @return the integer id associated with the given object.
     */
    <T> int obj2Int(@NotNull NetCache<T> cache, @NotNull T obj);

    /**
     * Gets an object associated with the given cache and integer id.
     * <p>
     * This will return {@code null} if no packet has been received that adds an association for the given id.
     *
     * @param cache the cache to look up the association in.
     * @param id    the id to look up the association for.
     * @param <T>   the type of object to look up.
     * @return the object associated with the given integer id, or {@code null} if no association was received for the given id.
     */
    <T> @Nullable T tryInt2Obj(@NotNull NetCache<T> cache, int id);

    /**
     * Gets an object associated with the given cache and integer id.
     * <p>
     * This throws an {@link IllegalStateException} if no packet has been received that adds an association for the given id.
     *
     * @param cache the cache to look up the association in.
     * @param id    the id to look up the assocaition for.
     * @param <T>   the type of object to look up.
     * @return the object associated with the given integer id.
     * @throws IllegalStateException if no association was received for the given id.
     */
    default <T> @NotNull T int2Obj(@NotNull NetCache<T> cache, int id) {
        T obj = tryInt2Obj(cache, id);
        if (obj == null) throw new IllegalStateException(
            cache.getId() + " attempted to find a missing id: " + id + ", connection side: " +
                getConnection().getSide() + ", connection state:" + getConnection().getState());
        return obj;
    }
}
