package com.kneelawk.graphlib.netutil.impl.netcache;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.netcache.NetCache;

public class NetCacheImpl {
    private static final Map<Identifier, NetCache<?>> registeredCaches = new Object2ObjectLinkedOpenHashMap<>();
    private static final ReadWriteLock cachesLock = new ReentrantReadWriteLock();
    private static final Map<PacketListener, NetCacheHolder> holders = new Object2ObjectLinkedOpenHashMap<>();
    private static final ReadWriteLock holdersLock = new ReentrantReadWriteLock();

    public static void register(NetCache<?> cache) {
        Lock lock = cachesLock.writeLock();
        lock.lock();
        try {
            registeredCaches.put(cache.getId(), cache);
        } finally {
            lock.unlock();
        }
    }

    public static Map<Identifier, SyncedObjectCache<?>> createCaches() {
        Lock lock = cachesLock.readLock();
        lock.lock();
        try {
            ImmutableMap.Builder<Identifier, SyncedObjectCache<?>> builder = ImmutableMap.builder();
            for (Identifier id : registeredCaches.keySet()) {
                builder.put(id, new SyncedObjectCache<>());
            }
            return builder.build();
        } finally {
            lock.unlock();
        }
    }

    public NetCacheHolder getHolder(PacketListener connection) {
        NetCacheHolder holder;
        holdersLock.readLock().lock();
        try {
            holder = holders.get(connection);
        } finally {
            holdersLock.readLock().unlock();
        }

        if (holder != null) return holder;

        holdersLock.writeLock().lock();
        try {
            return holders.computeIfAbsent(connection, _connection -> new NetCacheHolder(createCaches()));
        } finally {
            holdersLock.writeLock().unlock();
        }
    }

    public void removeClosedConnections() {
        holdersLock.readLock().lock();
         try {
             Iterator<PacketListener> keyIter =
         } finally {
             holdersLock.readLock().unlock();
         }
    }
}
