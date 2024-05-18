package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

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
    @NotNull
    Level getBlockWorld();

    /**
     * Gets the graph world that this graph entity exists within.
     *
     * @return the graph world that this graph entity exists within.
     */
    @NotNull
    GraphView getGraphWorld();

    /**
     * Gets the graph that this graph entity is associated with.
     *
     * @return the graph that this graph entity is associated with.
     */
    @NotNull
    BlockGraph getGraph();

    /**
     * Gets a collection of all the players tracking this graph.
     * <p>
     * Note: this returns an empty collection on the client side.
     *
     * @return a collection of all the players tracking this graph.
     */
    default @NotNull Collection<ServerPlayer> getTrackingPlayers() {
        if (getBlockWorld() instanceof ServerLevel world) {
            Set<ServerPlayer> players = new ObjectLinkedOpenHashSet<>();
            for (Iterator<SectionPos> iter = getGraph().getChunks().iterator(); iter.hasNext(); ) {
                players.addAll(
                    world.getChunkSource().chunkMap.getPlayers(iter.next().chunk(),
                        false));
            }
            return players;
        } else {
            return List.of();
        }
    }
}
