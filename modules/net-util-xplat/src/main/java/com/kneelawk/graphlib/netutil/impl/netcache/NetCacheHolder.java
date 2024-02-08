package com.kneelawk.graphlib.netutil.impl.netcache;

import java.util.Map;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.netutil.api.netcache.NetCacheObject;

public class NetCacheHolder {
    private final Map<Identifier, SyncedObjectCache<?>> caches;

    public NetCacheHolder(Map<Identifier, SyncedObjectCache<?>> caches) {
        this.caches = caches;
    }

    @SuppressWarnings("unchecked")
    public <T extends NetCacheObject> SyncedObjectCache<T> getCache(Identifier cacheId) {
        return (SyncedObjectCache<T>) caches.get(cacheId);
    }
}
