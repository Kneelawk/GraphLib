package com.kneelawk.graphlib;

import com.kneelawk.graphlib.graph.BlockGraphController;
import com.kneelawk.graphlib.graph.BlockNodeDecoder;
import com.kneelawk.graphlib.mixin.api.StorageHelper;
import com.mojang.serialization.Lifecycle;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Graph Lib public API. This class contains static methods and fields for interacting with Graph Lib, obtaining a
 * {@link BlockGraphController}, or registering {@link BlockNodeDecoder}s and BlockNodeFinders.
 */
public final class GraphLib {
    private static final Identifier BLOCK_NODE_DECODER_IDENTIFIER = Constants.id("block_node_decoder");
    private static final RegistryKey<Registry<BlockNodeDecoder>> BLOCK_NODE_DECODER_KEY =
            RegistryKey.ofRegistry(BLOCK_NODE_DECODER_IDENTIFIER);

    /**
     * Registry of {@link BlockNodeDecoder}s for block-node type ids.
     */
    public static final Registry<BlockNodeDecoder> BLOCK_NODE_DECODER =
            new SimpleRegistry<>(BLOCK_NODE_DECODER_KEY, Lifecycle.experimental(), null);

    /**
     * Gets the {@link BlockGraphController} for the given {@link ServerWorld}.
     *
     * @param world the world whose BlockGraphController is to be obtained.
     * @return the BlockGraphController of the given world.
     */
    public static BlockGraphController getController(ServerWorld world) {
        return StorageHelper.getController(world);
    }

    // ---- Internal Stuff ---- //

    public static Logger log = LoggerFactory.getLogger(Constants.MOD_ID);

    private GraphLib() {
    }

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registry.REGISTRIES, BLOCK_NODE_DECODER_IDENTIFIER,
                BLOCK_NODE_DECODER);
    }
}
