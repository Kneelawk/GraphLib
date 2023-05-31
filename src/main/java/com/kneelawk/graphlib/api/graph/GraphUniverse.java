package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.Pair;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDecoder;
import com.kneelawk.graphlib.api.graph.user.BlockNodeDiscoverer;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityDecoder;
import com.kneelawk.graphlib.api.world.SaveMode;
import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphUniverseBuilder;

/**
 * Represents one {@link GraphWorld} per {@link ServerWorld}. Provides access to each world's associated {@link GraphWorld}.
 * <p>
 * <b>Note: GraphUniverses must be registered with the {@link #register()} method in order to work properly.</b>
 */
public interface GraphUniverse {
    /**
     * Gets the {@link GraphWorld} for the given {@link ServerWorld}.
     *
     * @param world the world whose BlockGraphController is to be obtained.
     * @return the GraphWorld of the given world.
     */
    @NotNull GraphWorld getGraphWorld(@NotNull ServerWorld world);

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
     * node's {@link BlockNode#getTypeId()}.
     *
     * @param typeId  the type id of the block node the decoder is being registered for.
     * @param decoder the block node decoder responsible for decoding the associated type of block node.
     */
    void addNodeDecoder(@NotNull Identifier typeId, @NotNull BlockNodeDecoder decoder);

    /**
     * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated block
     * node's {@link BlockNode#getTypeId()}.
     *
     * @param decoders the set of block node decoders to be registered.
     */
    void addNodeDecoders(@NotNull Pair<Identifier, ? extends BlockNodeDecoder>... decoders);

    /**
     * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated block
     * node's {@link BlockNode#getTypeId()}.
     *
     * @param decoders the set of block node decoders to be registered.
     */
    void addNodeDecoders(@NotNull Map<Identifier, ? extends BlockNodeDecoder> decoders);

    /**
     * Registers a {@link NodeEntityDecoder} for the given node entity type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getTypeId()}.
     *
     * @param typeId  the type id of the node entity the decoder is being registered for.
     * @param decoder the node entity decoder responsible for decoding the associated type of node entity.
     */
    void addNodeEntityDecoder(@NotNull Identifier typeId, @NotNull NodeEntityDecoder decoder);

    /**
     * Registers a set of {@link NodeEntityDecoder} with associated node entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getTypeId()}.
     *
     * @param decoders the set of node entity decoders to be registered.
     */
    void addNodeEntityDecoders(@NotNull Pair<Identifier, ? extends NodeEntityDecoder>... decoders);

    /**
     * Registers a set of {@link NodeEntityDecoder} with associated node entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated node
     * entity's {@link NodeEntity#getTypeId()}.
     *
     * @param decoders the set of node entity decoders to be registered.
     */
    void addNodeEntityDecoders(@NotNull Map<Identifier, ? extends NodeEntityDecoder> decoders);

    /**
     * Registers a {@link LinkKeyDecoder} for the given link type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getTypeId()}.
     *
     * @param typeId  the type id of the link key the decoder is being registered for.
     * @param decoder the link key decoder responsible for decoding the associated type of link key.
     */
    void addLinkKeyDecoder(@NotNull Identifier typeId, @NotNull LinkKeyDecoder decoder);

    /**
     * Registers a set of {@link LinkKeyDecoder}s with associated link key type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getTypeId()}.
     *
     * @param decoders the set of link key decoders to be registered.
     */
    void addLinkKeyDecoders(@NotNull Pair<Identifier, ? extends LinkKeyDecoder>... decoders);

    /**
     * Registers a set of {@link LinkKeyDecoder}s with associated link key type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link key's
     * {@link LinkKey#getTypeId()}.
     *
     * @param decoders the set of link key decoders to be registered.
     */
    void addLinkKeyDecoders(@NotNull Map<Identifier, ? extends LinkKeyDecoder> decoders);

    /**
     * Registers a {@link LinkEntityDecoder} for the given link type id.
     * <p>
     * The identifier under which the decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getTypeId()}.
     *
     * @param typeId  the type id of the link entity the decoder is being registered for.
     * @param decoder the link entity decoder responsible for decoding the associated type of link entity.
     */
    void addLinkEntityDecoder(@NotNull Identifier typeId, @NotNull LinkEntityDecoder decoder);

    /**
     * Registers a set of {@link LinkEntityDecoder}s with associated link entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getTypeId()}.
     *
     * @param decoders the set of link entity decoders to be registered.
     */
    void addLinkEntityDecoders(@NotNull Pair<Identifier, ? extends LinkEntityDecoder>... decoders);

    /**
     * Registers a set of {@link LinkEntityDecoder}s with associated link entity type ids.
     * <p>
     * The identifier under which a decoder is registered corresponds to the one returned by the associated link
     * entity's {@link LinkEntity#getTypeId()}.
     *
     * @param decoders the set of link entity decoders to be registered.
     */
    void addLinkEntityDecoders(@NotNull Map<Identifier, ? extends LinkEntityDecoder> decoders);

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
    void addGraphEntityTypes(@NotNull GraphEntityType<?>... types);

    /**
     * Registers a set of {@link GraphEntityType}s.
     *
     * @param types the graph entity types to be registered.
     */
    void addGraphEntityTypes(@NotNull Iterable<GraphEntityType<?>> types);

    /**
     * Registers this graph universe so that it can be found by its id.
     * <p>
     * If a graph universe is not registered, it will not work.
     */
    void register();

    /**
     * Gets the default debug color for use when encoding packets to clients.
     *
     * @param typeId the type id of the block node for which a color is being chosen.
     * @return the ARGB default debug color.
     */
    int getDefaultDebugColor(@NotNull Identifier typeId);

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
