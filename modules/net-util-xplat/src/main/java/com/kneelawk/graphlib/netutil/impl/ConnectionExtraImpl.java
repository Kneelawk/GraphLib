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

package com.kneelawk.graphlib.netutil.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.payload.CustomPayload;

import com.kneelawk.graphlib.netutil.api.ConnectionExtra;
import com.kneelawk.graphlib.netutil.api.netcache.NetCache;
import com.kneelawk.graphlib.netutil.impl.mixin.api.MinecraftNetworkHandler;
import com.kneelawk.graphlib.netutil.impl.netcache.NetCacheHolder;
import com.kneelawk.graphlib.netutil.impl.netcache.NetCacheImpl;

public class ConnectionExtraImpl implements ConnectionExtra {
    private final PacketListener connection;
    private final NetCacheHolder cacheHolder;

    public ConnectionExtraImpl(PacketListener connection) {
        this.connection = connection;
        this.cacheHolder = new NetCacheHolder(NetCacheImpl.createCaches());
    }

    @Override
    public @NotNull PacketListener getConnection() {
        return connection;
    }

    @Override
    public void sendPayload(CustomPayload payload) {
        if (connection instanceof MinecraftNetworkHandler handler) {
            handler.gl_sendPayload(payload);
        } else {
            throw new IllegalStateException("Connection extra created with invalid packet listener: " + connection);
        }
    }

    @Override
    public <T> int obj2Int(@NotNull NetCache<T> cache, @NotNull T obj) {
        return 0;
    }

    @Override
    public <T> @Nullable T tryInt2Obj(@NotNull NetCache<T> cache, int id) {
        return null;
    }

    public NetCacheHolder getCacheHolder() {
        return cacheHolder;
    }
}
