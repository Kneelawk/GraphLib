package com.kneelawk.graphlib.netutil.impl.netcache;

import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.ImmutableMap;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.netcache.NetCache;

public class NetCacheImpl {
    private static final Map<Identifier, NetCache<?>> registeredCaches = new Object2ObjectLinkedOpenHashMap<>();
    private static final ReadWriteLock cachesLock = new ReentrantReadWriteLock();

    public static NetCache<?> getCache(Identifier id) {
        Lock lock = cachesLock.readLock();
        lock.lock();
        try {
            return registeredCaches.get(id);
        } finally {
            lock.unlock();
        }
    }

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
}
