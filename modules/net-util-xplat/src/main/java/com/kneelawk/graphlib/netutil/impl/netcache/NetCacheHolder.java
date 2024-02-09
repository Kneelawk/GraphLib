package com.kneelawk.graphlib.netutil.impl.netcache;

import java.util.Map;

import net.minecraft.util.Identifier;

public class NetCacheHolder {
    private final Map<Identifier, SyncedObjectCache<?>> caches;

    public NetCacheHolder(Map<Identifier, SyncedObjectCache<?>> caches) {
        this.caches = caches;
    }

    @SuppressWarnings("unchecked")
    public <T> SyncedObjectCache<T> getCache(Identifier cacheId) {
        return (SyncedObjectCache<T>) caches.get(cacheId);
    }
}
