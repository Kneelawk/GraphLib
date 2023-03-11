package com.kneelawk.graphlib.api.v1.graph;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.Pair;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.v1.node.BlockNodeDiscoverer;
import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphUniverseBuilder;

/**
 * Represents one {@link GraphWorld} per {@link ServerWorld}. Provides access to each world's associated {@link GraphWorld}.
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
         * Builds and registers the {@link GraphUniverse} described by this builder.
         * <p>
         * Right before each GraphUniverse is built, GraphLib invokes all the {@link UniverseModifierRegistry.Modify}
         * registered for the given universe id as well as each registered {@link UniverseModifierRegistry.ModifyAll}.
         * This allows mods to register decoders and detectors for other mods' universes.
         *
         * @param universeId the unique id of the GraphUniverse to be built.
         * @return the newly created {@link GraphUniverse}.
         */
        @NotNull GraphUniverse buildAndRegister(@NotNull Identifier universeId);

        /**
         * Adds a {@link BlockNodeDiscoverer} to this graph universe.
         *
         * @param discoverer the {@link BlockNodeDiscoverer} to be added.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder discoverer(@NotNull BlockNodeDiscoverer discoverer);

        /**
         * Adds {@link BlockNodeDiscoverer}s to this graph universe.
         *
         * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder discoverers(@NotNull BlockNodeDiscoverer... discoverers);

        /**
         * Adds {@link BlockNodeDiscoverer}s to this graph universe.
         *
         * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder discoverers(@NotNull Iterable<BlockNodeDiscoverer> discoverers);

        /**
         * Adds {@link BlockNodeDiscoverer}s to this graph universe.
         *
         * @param discoverers the {@link BlockNodeDiscoverer}s to be added.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder discoverers(@NotNull Collection<BlockNodeDiscoverer> discoverers);

        /**
         * Registers a {@link BlockNodeDecoder} for the given block node type id.
         * <p>
         * The identifier under which the decoder is registered corresponds to the one returned by the associated block
         * node's {@link BlockNode#getTypeId()}.
         *
         * @param typeId  the type id of the block node the decoder is being registered for.
         * @param decoder the block node decoder responsible for decoding the associated type of block node.
         * @return this builder for call chaining.
         */
        @Contract("_, _ -> this")
        @NotNull Builder decoder(@NotNull Identifier typeId, @NotNull BlockNodeDecoder decoder);

        /**
         * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
         * <p>
         * The identifier under which a decoder is registered corresponds to the one returned by the associated block
         * node's {@link BlockNode#getTypeId()}.
         *
         * @param decoders the set of block node decoders to be registered.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder decoders(@NotNull Pair<Identifier, BlockNodeDecoder>... decoders);

        /**
         * Registers a set of {@link BlockNodeDecoder} with associated block node type ids.
         * <p>
         * The identifier under which a decoder is registered corresponds to the one returned by the associated block
         * node's {@link BlockNode#getTypeId()}.
         *
         * @param decoders the set of block node decoders to be registered.
         * @return this builder for call chaining.
         */
        @Contract("_ -> this")
        @NotNull Builder decoders(@NotNull Map<Identifier, BlockNodeDecoder> decoders);
    }
}
