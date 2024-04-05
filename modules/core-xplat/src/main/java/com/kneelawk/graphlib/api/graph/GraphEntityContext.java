package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;

/**
 * Context for a graph entity.
 */
public interface GraphEntityContext {
    /**
     * Marks this entity's graph as dirty and in need of saving.
     */
    void markDirty();

    /**
     * Gets the block world that this graph entity exists within.
     *
     * @return the block world that this graph entity exists within.
     */
    @NotNull World getBlockWorld();

    /**
     * Gets the graph world that this graph entity exists within.
     *
     * @return the graph world that this graph entity exists within.
     */
    @NotNull GraphView getGraphWorld();

    /**
     * Gets the graph that this graph entity is associated with.
     *
     * @return the graph that this graph entity is associated with.
     */
    @NotNull BlockGraph getGraph();

    /**
     * Gets a collection of all the players tracking this graph.
     * <p>
     * Note: this returns an empty collection on the client side.
     *
     * @return a collection of all the players tracking this graph.
     */
    default @NotNull Collection<ServerPlayerEntity> getTrackingPlayers() {
        if (getBlockWorld() instanceof ServerWorld world) {
            Set<ServerPlayerEntity> players = new ObjectLinkedOpenHashSet<>();
            for (Iterator<ChunkSectionPos> iter = getGraph().getChunks().iterator(); iter.hasNext(); ) {
                players.addAll(
                    world.getChunkManager().delegate.getPlayersWatchingChunk(iter.next().toChunkPos(), false));
            }
            return players;
        } else {
            return List.of();
        }
    }
}
