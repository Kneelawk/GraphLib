/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.syncing.api.graph;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.impl.graph.simple.SimpleSyncedUniverseBuilder;

/**
 * Manages server to client synchronization of a {@link GraphUniverse}
 */
public interface SyncedUniverse {
    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    @NotNull Identifier getId();

    /**
     * Gets the graph universe that this handler synchronizes.
     *
     * @return the universe associated with this synchronization handler.
     */
    @NotNull GraphUniverse getUniverse();

    /**
     * Gets the {@link GraphView} for the given {@link World}.
     * <p>
     * This works on both the logical server and the logical client, unlike
     * {@link GraphUniverse#getServerGraphWorld(ServerWorld)} which only works on the logical server.
     *
     * @param world the world to get the graph view associated with.
     * @return the graph view associated with the given world.
     */
    @Nullable GraphView getSidedGraphView(@NotNull World world);

    /**
     * Gets the {@link GraphView} of this client, if this is indeed a physical client.
     *
     * @return the graph view of this client, if this is indeed a physical client, or <code>null</code> if this is a
     * physical server.
     */
    @Nullable GraphView getClientGraphView();

    /**
     * Registers an encoder and decoder for the given block node type.
     *
     * @param type    the type of block node to associate the encoder and decoder with.
     * @param syncing the syncing for the block node.
     */
    void addNodeSyncing(@NotNull BlockNodeType type, @NotNull BlockNodeSyncing syncing);

    /**
     * Gets whether the given block node type has had encoders and decoders registered with this universe.
     *
     * @param type the type of block node to check.
     * @return {@code true} if this universe contains syncing for the given block node type.
     */
    boolean hasNodeSyncing(@NotNull BlockNodeType type);

    /**
     * Gets the encoder and decoder for the given block node type.
     *
     * @param type the type of block node to get the syncing for.
     * @return the syncing for the given block node type.
     */
    @NotNull BlockNodeSyncing getNodeSyncing(@NotNull BlockNodeType type);

    /**
     * Registers an encoder and decoder for the given node entity type.
     *
     * @param type    the type of node entity to associate the encoder and decoder with.
     * @param syncing the node entity syncing.
     */
    void addNodeEntitySyncing(@NotNull NodeEntityType type, @NotNull NodeEntitySyncing syncing);

    /**
     * Gets whether the given node entity type has had encoders and decoders registered with this universe.
     *
     * @param type the type of node entity to check.
     * @return {@code true} if this universe contains syncing for the given node entity type.
     */
    boolean hasNodeEntitySyncing(@NotNull NodeEntityType type);

    /**
     * Gets the encoder and decoder for the given node entity type.
     *
     * @param type the type of node entity to get the syncing for.
     * @return the syncing for the given node entity type.
     */
    @NotNull NodeEntitySyncing getNodeEntitySyncing(@NotNull NodeEntityType type);

    /**
     * Registers an encoder and decoder for the given link key type.
     *
     * @param type    the type of link key to associate the encoder and decoder with.
     * @param syncing the link key syncing.
     */
    void addLinkKeySyncing(@NotNull LinkKeyType type, @NotNull LinkKeySyncing syncing);

    /**
     * Gets whether the given link key type has had encoders and decoders registered with this universe.
     *
     * @param type the type of link key to check.
     * @return {@code true} if this universe contains syncing for the given link key type.
     */
    boolean hasLinkKeySyncing(@NotNull LinkKeyType type);

    /**
     * Gets the encoder and decoder for the given link key type.
     *
     * @param type the type of link key to get the syncing for.
     * @return the syncing for the given link key type.
     */
    @NotNull LinkKeySyncing getLinkKeySyncing(@NotNull LinkKeyType type);

    /**
     * Registers an encoder and decoder for the given link entity type.
     *
     * @param type    the type of link entity to associate the encoder and decoder with.
     * @param syncing the link entity syncing.
     */
    void addLinkEntitySyncing(@NotNull LinkEntityType type, @NotNull LinkEntitySyncing syncing);

    /**
     * Gets whether the given link entity type has had encoders and decoders registered with this universe.
     *
     * @param type the type of link entity to check.
     * @return {@code true} if this universe contains syncing for the given link entity type.
     */
    boolean hasLinkEntitySyncing(@NotNull LinkEntityType type);

    /**
     * Gets the encoder and decoder for the given link entity type.
     *
     * @param type the type of link entity to get the syncing for.
     * @return the syncing for the given link entity type.
     */
    @NotNull LinkEntitySyncing getLinkEntitySyncing(@NotNull LinkEntityType type);

    /**
     * Registers an encoder and decoder for the given graph entity type.
     *
     * @param <G>     the type of graph entity to add syncing for.
     * @param type    the type of graph entity to associate the encoder and decoder with.
     * @param syncing the graph entity syncing.
     */
    <G extends GraphEntity<G>> void addGraphEntitySyncing(@NotNull GraphEntityType<G> type,
                                                          @NotNull GraphEntitySyncing<G> syncing);

    /**
     * Gets whether the given graph entity type has had encoders and decoders registered with this universe.
     *
     * @param type the type of graph entity to check.
     * @return {@code true} if this universe contains syncing for the given graph entity type.
     */
    boolean hasGraphEntitySyncing(@NotNull GraphEntityType<?> type);

    /**
     * Gets the encoder and decoder for the given graph entity type.
     *
     * @param type the type of graph entity to get the syncing for.
     * @param <G>  the type of graph entity to get the syncing for.
     * @return the syncing for the given graph entity type.
     */
    <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> getGraphEntitySyncing(@NotNull GraphEntityType<G> type);

    /**
     * Registers this synchronization handler so that it can be found by its universe's id.
     * <p>
     * If this synchronization handler is not registered, it will not work.
     */
    void register();

    /**
     * Gets this universe's synchronization profile.
     *
     * @return this universe's synchronization profile.
     */
    @NotNull SyncProfile getSyncProfile();

    /**
     * Creates a new SyncedUniverse builder.
     *
     * @return a new builder for building a SyncedUniverse.
     */
    @Contract(value = "-> new", pure = true)
    static @NotNull Builder builder() {
        return new SimpleSyncedUniverseBuilder();
    }

    /**
     * A builder for {@link SyncedUniverse}s.
     */
    interface Builder {
        /**
         * Builds a universe synchronization handler for the given universe.
         * <p>
         * <b>Note: This does not register synchronization handlers. Registration should be performed with the
         * {@link SyncedUniverse#register()} method.</b>
         *
         * @param universe the universe that this synchronization handler is to synchronize.
         * @return a new universe synchronization handler.
         */
        @NotNull SyncedUniverse build(@NotNull GraphUniverse universe);

        /**
         * Sets whether this graph universe should be synchronized to the client.
         * <p>
         * This is set to {@link SyncProfile#SYNC_EVERYTHING} by default.
         * <p>
         * The {@link CacheCategory} in the given sync profile will be automatically registered on universe creation.
         *
         * @param profile a profile describing whether and how this graph universe should be synchronized to the client.
         * @return this builder for call chaining.
         */
        @NotNull Builder synchronizeToClient(@NotNull SyncProfile profile);
    }
}
