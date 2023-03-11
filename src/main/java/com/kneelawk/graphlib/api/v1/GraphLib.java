package com.kneelawk.graphlib.api.v1;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registry;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, like obtaining a
 * previously-registered {@link GraphUniverse}.
 */
public final class GraphLib {
    private GraphLib() {
    }

    /**
     * Registry of {@link BlockNodePacketEncoderHolder}s for encoding nodes to send to the client for debug rendering.
     */
    public static final Registry<BlockNodePacketEncoderHolder<?>> BLOCK_NODE_PACKET_ENCODER =
        new SimpleRegistry<>(GraphLibImpl.BLOCK_NODE_PACKET_ENCODER_KEY, Lifecycle.experimental());

    /**
     * The unique id of the universe representing the data managed by pre-1.0 versions of GraphLib.
     *
     * @deprecated It is recommended for mods to build and use their own universes with {@link GraphUniverse#builder()}.
     */
    @Deprecated
    public static final Identifier LEGACY_UNIVERSE_ID = Constants.id(Constants.GRAPHDATA_DIRNAME);

    /**
     * The universe representing the data managed by pre-1.0 versions of GraphLib.
     *
     * @deprecated It is recommended for mods to build and use their own universes with {@link GraphUniverse#builder()}.
     */
    @Deprecated
    public static final GraphUniverse LEGACY_UNIVERSE = GraphUniverse.builder().buildAndRegister(LEGACY_UNIVERSE_ID);

    /**
     * Gets a registered {@link GraphUniverse} by its id.
     *
     * @param universeId the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static @NotNull GraphUniverse getUniverse(Identifier universeId) {
        GraphUniverseImpl graphUniverse = GraphLibImpl.UNIVERSE.get(universeId);
        if (graphUniverse == null) {
            throw new IllegalArgumentException("No universe exists with the name " + universeId);
        }

        return graphUniverse;
    }
}
