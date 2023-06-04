package com.kneelawk.graphlib.api.util;

import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Acts as both key and predicate for graphs' filtered node caches.
 * <p>
 * Note: cache categories should be kept around and re-used. Caching does not work if a new cache category is created
 * every time a lookup is needed.
 *
 * @param <T> the type of node this category operates on.
 */
public class CacheCategory<T extends BlockNode> {
    private final Class<T> nodeClass;
    private final Predicate<NodeHolder<T>> predicate;

    private CacheCategory(Class<T> nodeClass, Predicate<NodeHolder<T>> predicate) {
        this.nodeClass = nodeClass;
        this.predicate = predicate;
    }

    /**
     * Gets the class of the nodes this filters for.
     *
     * @return the class of the nodes this filters for.
     */
    public @NotNull Class<T> getNodeClass() {
        return nodeClass;
    }

    /**
     * Checks whether a given node holder matches this cache category.
     *
     * @param holder the node holder to check.
     * @return <code>true</code> if the given node holder should be included in this category's cache.
     */
    public boolean matches(@NotNull NodeHolder<?> holder) {
        return holder.canCast(nodeClass) && predicate.test(holder.cast(nodeClass));
    }

    /**
     * Creates a new cache category.
     * <p>
     * Note: cache categories should be kept around and re-used. Caching does not work if a new cache category is created
     * every time a lookup is needed.
     *
     * @param nodeClass the class of node the associated caches store.
     * @param predicate the filter that all nodes in the associated caches match.
     * @param <T>       the type of node in this cache category.
     * @return a new cache category.
     */
    public static <T extends BlockNode> CacheCategory<T> of(Class<T> nodeClass, Predicate<NodeHolder<T>> predicate) {
        return new CacheCategory<>(nodeClass, predicate);
    }

    /**
     * Creates a new cache category, with only a type as its filter.
     * <p>
     * Note: cache categories should be kept around and re-used. Caching does not work if a new cache category is created
     * every time a lookup is needed.
     *
     * @param nodeClass the class of node to filter by.
     * @param <T>       the type of node in this cache category.
     * @return a new cache category.
     */
    public static <T extends BlockNode> CacheCategory<T> of(Class<T> nodeClass) {
        return new CacheCategory<>(nodeClass, holder -> true);
    }

    /**
     * Creates a new cache category, with only a predicate as its filter.
     * <p>
     * Note: cache categories should be kept around and re-used. Caching does not work if a new cache category is created
     * every time a lookup is needed.
     *
     * @param predicate the filter that all nodes in the associated caches match.
     * @return a new cache category.
     */
    public static CacheCategory<BlockNode> of(Predicate<NodeHolder<BlockNode>> predicate) {
        return new CacheCategory<>(BlockNode.class, predicate);
    }
}
