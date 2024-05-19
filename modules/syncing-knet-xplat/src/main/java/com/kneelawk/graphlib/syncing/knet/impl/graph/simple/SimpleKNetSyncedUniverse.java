/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.knet.impl.graph.simple;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.impl.CommonProxy;
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldStorage;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;
import com.kneelawk.graphlib.syncing.impl.graph.simple.SimpleClientGraphWorld;
import com.kneelawk.graphlib.syncing.knet.api.SyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.impl.KNetEncoding;
import com.kneelawk.graphlib.syncing.knet.impl.graph.KNetWorldListener;

public class SimpleKNetSyncedUniverse implements KNetSyncedUniverse, SyncedUniverseImpl {
    private final ResourceLocation id;
    private final GraphUniverse universe;
    private final SyncProfile syncProfile;

    private final Map<BlockNodeType, BlockNodeSyncing> nodeSyncing = new HashMap<>();
    private final Map<NodeEntityType, NodeEntitySyncing> nodeEntitySyncing = new HashMap<>();
    private final Map<LinkKeyType, LinkKeySyncing> linkKeySyncing = new HashMap<>();
    private final Map<LinkEntityType, LinkEntitySyncing> linkEntitySyncing = new HashMap<>();
    private final Map<GraphEntityType<?>, GraphEntitySyncing<?>> graphEntitySyncing = new HashMap<>();

    public SimpleKNetSyncedUniverse(SimpleKNetSyncedUniverseBuilder builder, @NotNull GraphUniverse universe) {
        id = universe.getId();
        this.universe = universe;
        syncProfile = builder.profile;

        addLinkKeySyncing(SyncingKNet.EMPTY_KEY_SYNCING);

        if (syncProfile.getNodeFilter() != null) {
            universe.addCacheCategory(syncProfile.getNodeFilter());
        }
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public @NotNull GraphUniverse getUniverse() {
        return universe;
    }

    @Override
    public @Nullable GraphView getSidedGraphView(@NotNull Level world) {
        GraphWorldStorage storage = CommonProxy.INSTANCE.getSidedStorage(world);
        if (storage == null) return null;

        return storage.get(id);
    }

    @Override
    public @Nullable ClientGraphWorldImpl getClientGraphView() {
        ClientGraphWorldStorage storage = CommonProxy.INSTANCE.getClientStorage();
        if (storage == null) {
            return null;
        }

        return storage.get(id);
    }

    @Override
    public void addNodeSyncing(@NotNull BlockNodeSyncing syncing) {
        nodeSyncing.put(syncing.getType(), syncing);
    }

    @Override
    public @Nullable BlockNodeSyncing getNodeSyncing(@NotNull BlockNodeType type) {
        return nodeSyncing.get(type);
    }

    @Override
    public void addNodeEntitySyncing(@NotNull NodeEntitySyncing syncing) {
        nodeEntitySyncing.put(syncing.getType(), syncing);
    }

    @Override
    public @Nullable NodeEntitySyncing getNodeEntitySyncing(@NotNull NodeEntityType type) {
        return nodeEntitySyncing.get(type);
    }

    @Override
    public void addLinkKeySyncing(@NotNull LinkKeySyncing syncing) {
        linkKeySyncing.put(syncing.getType(), syncing);
    }

    @Override
    public @Nullable LinkKeySyncing getLinkKeySyncing(@NotNull LinkKeyType type) {
        return linkKeySyncing.get(type);
    }

    @Override
    public void addLinkEntitySyncing(@NotNull LinkEntitySyncing syncing) {
        linkEntitySyncing.put(syncing.getType(), syncing);
    }

    @Override
    public @Nullable LinkEntitySyncing getLinkEntitySyncing(@NotNull LinkEntityType type) {
        return linkEntitySyncing.get(type);
    }

    @Override
    public <G extends GraphEntity<G>> void addGraphEntitySyncing(@NotNull GraphEntitySyncing<G> syncing) {
        graphEntitySyncing.put(syncing.getType(), syncing);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @Nullable <G extends GraphEntity<G>> GraphEntitySyncing<G> getGraphEntitySyncing(
        @NotNull GraphEntityType<G> type) {
        return (GraphEntitySyncing<G>) graphEntitySyncing.get(type);
    }

    @Override
    public void register() {
        GraphLibSyncingImpl.register(this);
    }

    @Override
    public @NotNull SyncProfile getSyncProfile() {
        return syncProfile;
    }

    @Override
    public @NotNull WorldListener createWorldListener(ServerGraphWorldImpl world) {
        return KNetWorldListener.INSTANCE;
    }

    @Override
    public ClientGraphWorldImpl createClientGraphWorld(Level world, int loadDistance) {
        return new SimpleClientGraphWorld(this, world, loadDistance);
    }

    @Override
    public void sendChunkDataPacket(ServerGraphWorldImpl world, ServerPlayer player, ChunkPos pos) {
        KNetEncoding.sendChunkData(world, player, pos);
    }
}
