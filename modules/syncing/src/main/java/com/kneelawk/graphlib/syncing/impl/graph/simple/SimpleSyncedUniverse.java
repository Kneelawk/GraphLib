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

package com.kneelawk.graphlib.syncing.impl.graph.simple;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodePacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodePacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.GraphEntityPacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.GraphEntityPacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkEntityPacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkEntityPacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkKeyPacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkKeyPacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.NodeEntityPacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.NodeEntityPacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.api.util.PacketEncodingUtil;
import com.kneelawk.graphlib.syncing.impl.CommonProxy;
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldStorage;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;

public class SimpleSyncedUniverse implements SyncedUniverseImpl {
    private final Identifier id;
    private final GraphUniverse universe;
    private final SyncProfile syncProfile;

    private final Map<BlockNodeType, BlockNodeSyncing> nodeSyncing = new HashMap<>();
    private final Map<NodeEntityType, NodeEntitySyncing> nodeEntitySyncing = new HashMap<>();
    private final Map<LinkKeyType, LinkKeySyncing> linkKeySyncing = new HashMap<>();
    private final Map<LinkEntityType, LinkEntitySyncing> linkEntitySyncing = new HashMap<>();
    private final Map<GraphEntityType<?>, GraphEntitySyncing<?>> graphEntitySyncing = new HashMap<>();

    public SimpleSyncedUniverse(SimpleSyncedUniverseBuilder builder, @NotNull GraphUniverse universe) {
        id = universe.getId();
        this.universe = universe;
        syncProfile = builder.profile;

        addLinkKeySyncing(EmptyLinkKey.TYPE, PacketEncodingUtil.EMPTY_KEY_ENCODER,
            PacketEncodingUtil.EMPTY_KEY_DECODER);

        if (syncProfile.getNodeFilter() != null) {
            universe.addCacheCategory(syncProfile.getNodeFilter());
        }
    }

    @Override
    public @NotNull Identifier getId() {
        return id;
    }

    @Override
    public @NotNull GraphUniverse getUniverse() {
        return universe;
    }

    @Override
    public @Nullable GraphView getSidedGraphView(@NotNull World world) {
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
    public <N extends BlockNode> void addNodeSyncing(@NotNull BlockNodeType type,
                                                     @NotNull BlockNodePacketEncoder<N> encoder,
                                                     @NotNull BlockNodePacketDecoder decoder) {
        nodeSyncing.put(type, new BlockNodeSyncing(encoder, decoder));
    }

    @Override
    public boolean hasNodeSyncing(@NotNull BlockNodeType type) {
        return nodeSyncing.containsKey(type);
    }

    @Override
    public @NotNull BlockNodeSyncing getNodeSyncing(@NotNull BlockNodeType type) {
        BlockNodeSyncing syncing = nodeSyncing.get(type);
        if (syncing == null)
            throw new IllegalStateException("Attempting to sync unregistered node type: " + type.getId());
        return syncing;
    }

    @Override
    public <N extends NodeEntity> void addNodeEntitySyncing(@NotNull NodeEntityType type,
                                                            @NotNull NodeEntityPacketEncoder<N> encoder,
                                                            @NotNull NodeEntityPacketDecoder decoder) {
        nodeEntitySyncing.put(type, new NodeEntitySyncing(encoder, decoder));
    }

    @Override
    public boolean hasNodeEntitySyncing(@NotNull NodeEntityType type) {
        return nodeEntitySyncing.containsKey(type);
    }

    @Override
    public @NotNull NodeEntitySyncing getNodeEntitySyncing(@NotNull NodeEntityType type) {
        NodeEntitySyncing syncing = nodeEntitySyncing.get(type);
        if (syncing == null)
            throw new IllegalStateException("Attempting to sync unregistered node entity type: " + type.getId());
        return syncing;
    }

    @Override
    public <L extends LinkKey> void addLinkKeySyncing(@NotNull LinkKeyType type,
                                                      @NotNull LinkKeyPacketEncoder<L> encoder,
                                                      @NotNull LinkKeyPacketDecoder decoder) {
        linkKeySyncing.put(type, new LinkKeySyncing(encoder, decoder));
    }

    @Override
    public boolean hasLinkKeySyncing(@NotNull LinkKeyType type) {
        return linkKeySyncing.containsKey(type);
    }

    @Override
    public @NotNull LinkKeySyncing getLinkKeySyncing(@NotNull LinkKeyType type) {
        LinkKeySyncing syncing = linkKeySyncing.get(type);
        if (syncing == null)
            throw new IllegalStateException("Attempting to sync unregistered link key type: " + type.getId());
        return syncing;
    }

    @Override
    public <L extends LinkEntity> void addLinkEntitySyncing(@NotNull LinkEntityType type,
                                                            @NotNull LinkEntityPacketEncoder<L> encoder,
                                                            @NotNull LinkEntityPacketDecoder decoder) {
        linkEntitySyncing.put(type, new LinkEntitySyncing(encoder, decoder));
    }

    @Override
    public boolean hasLinkEntitySyncing(@NotNull LinkEntityType type) {
        return linkEntitySyncing.containsKey(type);
    }

    @Override
    public @NotNull LinkEntitySyncing getLinkEntitySyncing(@NotNull LinkEntityType type) {
        LinkEntitySyncing syncing = linkEntitySyncing.get(type);
        if (syncing == null)
            throw new IllegalStateException("Attempting to sync unregistered link entity type: " + type.getId());
        return syncing;
    }

    @Override
    public <G extends GraphEntity<G>> void addGraphEntitySyncing(@NotNull GraphEntityType<G> type,
                                                                 @NotNull GraphEntityPacketEncoder<G> encoder,
                                                                 @NotNull GraphEntityPacketDecoder decoder) {
        graphEntitySyncing.put(type, new GraphEntitySyncing<>(encoder, decoder));
    }

    @Override
    public boolean hasGraphEntitySyncing(@NotNull GraphEntityType<?> type) {
        return graphEntitySyncing.containsKey(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <G extends GraphEntity<G>> GraphEntitySyncing<G> getGraphEntitySyncing(
        @NotNull GraphEntityType<G> type) {
        GraphEntitySyncing<G> syncing = (GraphEntitySyncing<G>) graphEntitySyncing.get(type);
        if (syncing == null)
            throw new IllegalStateException("Attempting to sync unregistered graph entity type: " + type.getId());
        return syncing;
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
        return new SimpleWorldEncoder(this, world);
    }

    @Override
    public ClientGraphWorldImpl createClientGraphWorld(World world, int loadDistance) {
        return new SimpleClientGraphWorld(this, world, loadDistance);
    }
}
