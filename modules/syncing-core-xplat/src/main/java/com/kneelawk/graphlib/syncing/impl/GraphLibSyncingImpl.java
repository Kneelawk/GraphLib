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

package com.kneelawk.graphlib.syncing.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;

public class GraphLibSyncingImpl {
    public static final Map<Identifier, SyncedUniverseImpl> SYNCED_UNIVERSE = new LinkedHashMap<>();

    public static void register(SyncedUniverseImpl universe) {
        if (!(universe.getUniverse() instanceof GraphUniverseImpl universeImpl)) throw new IllegalArgumentException(
            "Attempted to register a SyncedUniverse with a non-GraphLib GraphUniverse implementation: " +
                universe.getClass());

        if (SYNCED_UNIVERSE.containsKey(universe.getId())) throw new IllegalArgumentException(
            "A synced universe is already registered with the id: " + universe.getId());

        SYNCED_UNIVERSE.put(universe.getId(), universe);
        universeImpl.addListener(SyncedConstants.LISTENER_KEY, universe);
    }

    public static void sendChunkDataPackets(ServerWorld serverWorld, ServerPlayerEntity player, ChunkPos pos) {
        ServerGraphWorldStorage storage = StorageHelper.getStorage(serverWorld);

        for (SyncedUniverseImpl universe : SYNCED_UNIVERSE.values()) {
            ServerGraphWorldImpl world = storage.get(universe.getId());
            if (universe.getSyncProfile().isEnabled() &&
                universe.getSyncProfile().getPlayerFilter().shouldSync(player)) {
                try {
                    universe.sendChunkDataPacket(world, player, pos);
                } catch (Exception e) {
                    GLLog.error("Error sending GraphWorld chunk packets. World: '{}'/{}, Chunk: {}", serverWorld,
                        serverWorld.getRegistryKey().getValue(), pos, e);
                }
            }
        }
    }
}
