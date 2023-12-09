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

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodePacketDecoder;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodePacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;

public class SimpleSyncedUniverse implements SyncedUniverseImpl {
    private final GraphUniverse universe;
    private final SyncProfile syncProfile;

    private final Map<BlockNodeType, BlockNodeSyncing> nodeSyncing = new HashMap<>();

    public SimpleSyncedUniverse(SimpleSyncedUniverseBuilder builder, @NotNull GraphUniverse universe) {
        this.universe = universe;
        syncProfile = builder.profile;
    }

    @Override
    public @NotNull Identifier getId() {
        return universe.getId();
    }

    @Override
    public @NotNull GraphUniverse getUniverse() {
        return universe;
    }

    @Override
    public void addNodeSyncing(@NotNull BlockNodeType type, @Nullable BlockNodePacketEncoder<?> encoder,
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
    public void register() {
        GraphLibSyncingImpl.register(this);
    }

    @Override
    public @NotNull SyncProfile getSyncProfile() {
        return syncProfile;
    }
}
