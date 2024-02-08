package com.kneelawk.graphlib.netutil.impl.netcache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import com.kneelawk.graphlib.netutil.api.netcache.NetCacheObject;

public class SyncedObjectCache<T extends NetCacheObject> {
    private final Int2ObjectMap<T> incoming = new Int2ObjectOpenHashMap<>();
    private final ReadWriteLock incomingLock = new ReentrantReadWriteLock();
    private final Object2IntMap<T> outgoing = new Object2IntOpenHashMap<>();
    private final ReadWriteLock outgoingLock = new ReentrantReadWriteLock();

    public void putIncoming(int id, T obj) {
        Lock lock = incomingLock.writeLock();
        lock.lock();
        try {
            incoming.put(id, obj);
        } finally {
            lock.unlock();
        }
    }

    public T getIncoming(int id) {
        Lock lock = incomingLock.readLock();
        lock.lock();
        try {
            return incoming.get(id);
        } finally {
            lock.unlock();
        }
    }

    public void putOutgoing(T obj, int id) {
        Lock lock = outgoingLock.writeLock();
        lock.lock();
        try {
            outgoing.put(obj, id);
        } finally {
            lock.unlock();
        }
    }

    public int getOutgoing(T obj) {
        Lock lock = outgoingLock.readLock();
        lock.lock();
        try {
            return outgoing.getInt(obj);
        } finally {
            lock.unlock();
        }
    }
}
