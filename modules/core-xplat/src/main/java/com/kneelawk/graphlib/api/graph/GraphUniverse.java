package com.kneelawk.graphlib.api.graph;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDiscoverer;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.world.SaveMode;
import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphUniverseBuilder;

/**
 * Represents one {@link GraphWorld} per {@link ServerWorld}. Provides access to each world's associated {@link GraphWorld}.
 * <p>
 * <b>Note: GraphUniverses must be registered with the {@link #register()} method in order to work properly.</b>
 */
@ApiStatus.NonExtendable
public interface GraphUniverse {

    /**
     * Gets the {@link GraphWorld} for the given {@link ServerWorld}.
     *
     * @param world the world whose graph world is to be obtained.
     * @return the GraphWorld of the given world.
     */
    @NotNull GraphWorld getServerGraphWorld(@NotNull ServerWorld world);

    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    @NotNull Identifier getId();

    /**
     * Gets the save-mode this universe was built with.
     *
     * @return the save-mode this universe was built with.
     */
    @NotNull SaveMode getSaveMode();

    /**
     * Adds a {@link BlockNodeDiscoverer} to this graph universe.
     *
     * @param discoverer the {@link BlockNodeDiscoverer} to be added.
     */
    void addDiscoverer(@NotNull BlockNodeDiscoverer discoverer);

    /**
     * Adds {@link BlockNodeDiscoverer}s to this graph universe.
     *
     * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
     */
    void addDiscoverers(@NotNull BlockNodeDiscoverer... discoverers);

    /**
     * Adds {@link BlockNodeDiscoverer}s to this graph universe.
     *
     * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
     */
    void addDiscoverers(@NotNull Iterable<BlockNodeDiscoverer> discoverers);

    /**
     * Adds {@link BlockNodeDiscoverer}s to this graph universe.
     *
     * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
     */
    void addDiscoverers(@NotNull Collection<BlockNodeDiscoverer> discoverers);

    /**
     * Registers a {@link BlockNodeDecoder} for the given block node type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated block
     * node's {@link BlockNode#getType()}.
     *
     * @param type the block node type to register.
     */
    void addNodeType(@NotNull BlockNodeType type);

    /**
     * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated block
     * node's {@link BlockNode#getType()}.
     *
     * @param types the set of block node types to be registered.
     */
    default void addNodeTypes(@NotNull BlockNodeType... types) {
        for (BlockNodeType type : types) {
            addNodeType(type);
        }
    }

    /**
     * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated block
     * node's {@link BlockNode#getType()}.
     *
     * @param types the set of block node types to be registered.
     */
    default void addNodeTypes(@NotNull Iterable<BlockNodeType> types) {
        for (BlockNodeType type : types) {
            addNodeType(type);
        }
    }

    /**
     * Gets the block node decoder for the given type id.
     *
     * @param typeId the type id of the block node decoder.
     * @return the block node decoder for the given type id.
     */
    @Nullable BlockNodeType getNodeType(@NotNull Identifier typeId);

    /**
     * Registers a {@link NodeEntityDecoder} for the given node entity type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getType()}.
     *
     * @param type the node entity type to register.
     */
    void addNodeEntityType(@NotNull NodeEntityType type);

    /**
     * Registers a set of {@link NodeEntityDecoder} with associated node entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getType()}.
     *
     * @param types the set of node entity types to be registered.
     */
    default void addNodeEntityTypes(@NotNull NodeEntityType... types) {
        for (NodeEntityType type : types) {
            addNodeEntityType(type);
        }
    }

    /**
     * Registers a set of {@link NodeEntityDecoder} with associated node entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getType()}.
     *
     * @param types the set of node entity types to be registered.
     */
    default void addNodeEntityTypes(@NotNull Iterable<NodeEntityType> types) {
        for (NodeEntityType type : types) {
            addNodeEntityType(type);
        }
    }

    /**
     * Gets the node entity decoder for the given type id.
     *
     * @param typeId the type id of the node entity decoder.
     * @return the node entity decoder for the given type id.
     */
    @Nullable NodeEntityType getNodeEntityType(@NotNull Identifier typeId);

    /**
     * Registers a {@link LinkKeyDecoder} for the given link type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getType()}.
     *
     * @param type the link key type to register.
     */
    void addLinkKeyType(@NotNull LinkKeyType type);

    /**
     * Registers a set of {@link LinkKeyDecoder}s with associated link key type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getType()}.
     *
     * @param types the set of link key types to be registered.
     */
    default void addLinkKeyTypes(@NotNull LinkKeyType... types) {
        for (LinkKeyType type : types) {
            addLinkKeyType(type);
        }
    }

    /**
     * Registers a set of {@link LinkKeyDecoder}s with associated link key type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getType()}.
     *
     * @param types the set of link key types to be registered.
     */
    default void addLinkKeyTypes(@NotNull Iterable<LinkKeyType> types) {
        for (LinkKeyType type : types) {
            addLinkKeyType(type);
        }
    }

    /**
     * Gets the link key decoder for the given type id.
     *
     * @param typeId the type id of the link key decoder.
     * @return the link key decoder for the given type id.
     */
    @Nullable LinkKeyType getLinkKeyType(@NotNull Identifier typeId);

    /**
     * Registers a {@link LinkEntityDecoder} for the given link type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getType()}.
     *
     * @param type the type of link entity to register.
     */
    void addLinkEntityType(@NotNull LinkEntityType type);

    /**
     * Registers a set of {@link LinkEntityDecoder}s with associated link entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getType()}.
     *
     * @param types the set of link entity types to be registered.
     */
    default void addLinkEntityTypes(@NotNull LinkEntityType... types) {
        for (LinkEntityType type : types) {
            addLinkEntityType(type);
        }
    }

    /**
     * Registers a set of {@link LinkEntityDecoder}s with associated link entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getType()}.
     *
     * @param types the set of link entity types to be registered.
     */
    default void addLinkEntityTypes(@NotNull Iterable<LinkEntityType> types) {
        for (LinkEntityType type : types) {
            addLinkEntityType(type);
        }
    }

    /**
     * Gets the link entity decoder for the given type id.
     *
     * @param typeId the type id of the link entity decoder.
     * @return the link entity decoder for the given type id.
     */
    @Nullable LinkEntityType getLinkEntityType(@NotNull Identifier typeId);

    /**
     * Registers a {@link GraphEntityType}.
     *
     * @param type the graph entity type to be registered.
     */
    void addGraphEntityType(@NotNull GraphEntityType<?> type);

    /**
     * Registers a set of {@link GraphEntityType}s.
     *
     * @param types the graph entity types to be registered.
     */
    default void addGraphEntityTypes(@NotNull GraphEntityType<?>... types) {
        for (GraphEntityType<?> type : types) {
            addGraphEntityType(type);
        }
    }

    /**
     * Registers a set of {@link GraphEntityType}s.
     *
     * @param types the graph entity types to be registered.
     */
    default void addGraphEntityTypes(@NotNull Iterable<GraphEntityType<?>> types) {
        for (GraphEntityType<?> type : types) {
            addGraphEntityType(type);
        }
    }

    /**
     * Gets the graph entity type for the given type id.
     *
     * @param typeId the type id of the graph entity type.
     * @return the graph entity type for the given type id.
     */
    @Nullable GraphEntityType<?> getGraphEntityType(@NotNull Identifier typeId);

    /**
     * Gets all the graph entity types registered.
     *
     * @return all the graph entity types currently registered.
     */
    @NotNull Collection<GraphEntityType<?>> getAllGraphEntityTypes();

    /**
     * Registers a cache category to be auto-initialized on all graphs.
     *
     * @param category the category to be registered.
     */
    void addCacheCategory(@NotNull CacheCategory<?> category);

    /**
     * Registers a set of cache categories to be auto-initialized on all graphs.
     *
     * @param categories the categories to be registered.
     */
    default void addCacheCategories(@NotNull CacheCategory<?>... categories) {
        for (CacheCategory<?> category : categories) {
            addCacheCategory(category);
        }
    }

    /**
     * Registers a set of cache categories to be auto-initialized on all graphs.
     *
     * @param categories the categories to be registered.
     */
    default void addCacheCategories(@NotNull Iterable<CacheCategory<?>> categories) {
        for (CacheCategory<?> category : categories) {
            addCacheCategory(category);
        }
    }

    /**
     * Gets all the cache categories currently registered.
     *
     * @return all cache categories currently registered.
     */
    @NotNull Iterable<CacheCategory<?>> getCacheCatetories();

    /**
     * Registers this graph universe so that it can be found by its id.
     * <p>
     * If a graph universe is not registered, it will not work.
     */
    void register();

    /**
     * Gets the registration index of the given block node type id.
     * <p>
     * This is currently used in the debug renderer for determining a node's color.
     * <p>
     * Note: this should not be used for synchronization, as this <b>is</b> mod load order dependant, which may not be
     * the same on the client as on the server.
     *
     * @param typeId the type id of the block node to get the registration index of.
     * @return the registration index of the given block node type id.
     */
    int getNodeTypeIndex(@NotNull Identifier typeId);

    /**
     * Gets the number of block node type ids currently registered.
     *
     * @return the number of block node type ids currently registered.
     */
    int getNodeTypeCount();

    /**
     * Creates a new GraphUniverse builder.
     *
     * @return a new builder for building a GraphUniverse.
     */
    @Contract(value = "-> new", pure = true)
    static @NotNull Builder builder() {
        return new SimpleGraphUniverseBuilder();
    }

    /**
     * A builder for {@link GraphUniverse}s.
     */
    interface Builder {
        /**
         * Builds the {@link GraphUniverse} described by this builder. Universes must be registered separately.
         * <p>
         * <b>Note: This does not register universes. Registration should be performed with the
         * {@link GraphUniverse#register()} method.</b>
         *
         * @param universeId the unique id of the universe to be built.
         * @return the newly created {@link GraphUniverse}.
         */
        @NotNull GraphUniverse build(@NotNull Identifier universeId);

        /**
         * Determines how often graphs and chunks should be saved.
         * <p>
         * By default, this is {@link SaveMode#UNLOAD}.
         *
         * @param saveMode the save mode for this universe.
         * @return this builder for call chaining.
         */
        @NotNull Builder saveMode(@NotNull SaveMode saveMode);
    }
}
