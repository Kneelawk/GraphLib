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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.ConnectionExtra;
import com.kneelawk.graphlib.netutil.impl.mixin.api.MinecraftNetworkHandler;

public class NetUtilImpl {
    public static final String MOD_ID = "graphlib_net_util";

    private static final Map<PacketListener, ConnectionExtra> holders = new Object2ObjectLinkedOpenHashMap<>();
    private static final ReadWriteLock holdersLock = new ReentrantReadWriteLock();

    public static ConnectionExtra getOrCreateConnection(PacketListener connection) {
        if (!(connection instanceof MinecraftNetworkHandler)) return null;

        ConnectionExtra holder;
        holdersLock.readLock().lock();
        try {
            holder = holders.get(connection);
        } finally {
            holdersLock.readLock().unlock();
        }

        if (holder != null) return holder;

        holdersLock.writeLock().lock();
        try {
            return holders.computeIfAbsent(connection, ConnectionExtraImpl::new);
        } finally {
            holdersLock.writeLock().unlock();
        }
    }

    public static void removeClosedConnections() {
        List<PacketListener> holderKeys;
        holdersLock.readLock().lock();
        try {
            holderKeys = new ArrayList<>(holders.keySet());
        } finally {
            holdersLock.readLock().unlock();
        }

        for (PacketListener key : holderKeys) {
            if (!key.isConnected()) {
                holdersLock.writeLock().lock();
                try {
                    holders.remove(key);
                } finally {
                    holdersLock.writeLock().unlock();
                }
            }
        }
    }

    public static

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
