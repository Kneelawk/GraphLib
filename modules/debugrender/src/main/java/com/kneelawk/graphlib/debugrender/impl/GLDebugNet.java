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

package com.kneelawk.graphlib.debugrender.impl;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.event.GraphLibEvents;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.util.ClassUtils;

public final class GLDebugNet {
    private GLDebugNet() {
    }

    public static final Identifier ID_MAP_BULK_ID = Constants.id("id_map_bulk");
    public static final Identifier ID_MAP_PUT_ID = Constants.id("id_map_put");
    public static final Identifier GRAPH_UPDATE_ID = Constants.id("graph_update");
    public static final Identifier GRAPH_UPDATE_BULK_ID = Constants.id("graph_update_bulk");
    public static final Identifier GRAPH_DESTROY_ID = Constants.id("graph_destroy");
    public static final Identifier DEBUGGING_STOP_ID = Constants.id("debugging_stop");

    public static final BlockNodeDebugPacketEncoder DEFAULT_ENCODER = (node, self, buf) -> {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(node.hashCode());

        // Get the default color for our node type
        buf.writeInt(self.getGraphWorld().getUniverse().getDefaultDebugColor(node.getType().getId()));

        if (node instanceof SidedBlockNode sided) {
            // A 1 byte to distinguish ourselves from BlockNode, because both implementations use the same decoder
            buf.writeByte(1);

            // Our side
            buf.writeByte(sided.getSide().getId());
        } else {
            // A 0 byte to distinguish ourselves from SidedBlockNode, because both implementations use the same decoder
            buf.writeByte(0);
        }
    };

    private static final Multimap<UUID, Identifier> debuggingPlayers = LinkedHashMultimap.create();
    private static final Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> debuggingPlayers.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> debuggingPlayers.clear());
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> debuggingPlayers.removeAll(handler.player.getUuid()));

        GraphLibEvents.GRAPH_CREATED.register(GLDebugNet::sendBlockGraph);
        GraphLibEvents.GRAPH_UPDATED.register(GLDebugNet::sendBlockGraph);
        GraphLibEvents.GRAPH_DESTROYED.register((world, graphWorld, id) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(getIdentifierInt(world, graphWorld.getUniverse().getId()));
            buf.writeLong(id);

            sendToDebuggingPlayers(world, graphWorld.getUniverse().getId(), GRAPH_DESTROY_ID, buf);
        });
    }

    public static void startDebuggingPlayer(ServerPlayerEntity player, GraphUniverse universe) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            GLLog.warn("Tried to start debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.getWorld()));
            return;
        }

        sendIdMap(player);
        debuggingPlayers.put(player.getUuid(), universe.getId());

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(getIdentifierInt(world, universe.getId()));

        MinecraftServer server = world.getServer();
        GraphWorld graphWorld = universe.getServerGraphWorld(world);
        int viewDistance = server.getPlayerManager().getViewDistance();

        ChunkSectionPos playerPos = player.getWatchedSection();
        int minX = playerPos.getSectionX() - viewDistance - 1;
        int minZ = playerPos.getSectionZ() - viewDistance - 1;
        int maxX = playerPos.getSectionX() + viewDistance + 1;
        int maxZ = playerPos.getSectionZ() + viewDistance + 1;

        LongSet graphIds = new LongLinkedOpenHashSet();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (player.method_52372().method_52356(x, z)) {
                    ChunkPos pos = new ChunkPos(x, z);

                    graphWorld.getAllGraphIdsInChunk(pos).forEach(graphIds::add);
                }
            }
        }

        // collect the actual set of graphs we intend to send
        List<BlockGraph> graphs =
            graphIds.longStream().mapToObj(graphWorld::getGraph).filter(Objects::nonNull).toList();

        buf.writeVarInt(graphs.size());

        for (BlockGraph graph : graphs) {
            encodeBlockGraph(world, graphWorld, graph, buf);
        }

        ServerPlayNetworking.send(player, GRAPH_UPDATE_BULK_ID, buf);
    }

    public static void stopDebuggingPlayer(ServerPlayerEntity player, Identifier universe) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            GLLog.warn("Tried to stop debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.getWorld()));
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(getIdentifierInt(world, universe));

        ServerPlayNetworking.send(player, DEBUGGING_STOP_ID, buf);

        debuggingPlayers.remove(player.getUuid(), universe);
    }

    private static void sendIdMap(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(idMap.size());
        for (var entry : idMap.object2IntEntrySet()) {
            buf.writeIdentifier(entry.getKey());
            buf.writeVarInt(entry.getIntValue());
        }
        ServerPlayNetworking.send(player, ID_MAP_BULK_ID, buf);
    }

    private static int getIdentifierInt(ServerWorld world, Identifier id) {
        if (idMap.containsKey(id)) {
            return idMap.getInt(id);
        } else {
            int index = idMap.size();
            idMap.put(id, index);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeIdentifier(id);
            buf.writeVarInt(index);
            sendToDebuggingPlayers(world, ID_MAP_PUT_ID, buf);

            return index;
        }
    }

    private static void sendBlockGraph(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        if (debuggingPlayers.isEmpty()) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(getIdentifierInt(world, graphWorld.getUniverse().getId()));

        encodeBlockGraph(world, graphWorld, graph, buf);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        graph.getChunks().forEachOrdered(section -> {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, section.toChunkPos())) {
                if (debuggingPlayers.containsEntry(player.getUuid(), graphWorld.getUniverse().getId())) {
                    sendTo.add(player);
                }
            }
        });

        for (ServerPlayerEntity player : sendTo) {
            ServerPlayNetworking.send(player, GRAPH_UPDATE_ID, buf);
        }
    }

    private static void encodeBlockGraph(ServerWorld world, GraphWorld graphWorld, BlockGraph graph,
                                         PacketByteBuf buf) {
        buf.writeLong(graph.getId());

        AtomicInteger index = new AtomicInteger();
        Map<NodePos, Integer> indexMap = new HashMap<>();
        Set<LinkPos> distinct = new LinkedHashSet<>();

        buf.writeVarInt(graph.size());
        graph.getNodes().forEachOrdered(node -> {
            Identifier typeId = node.getNode().getType().getId();
            buf.writeVarInt(getIdentifierInt(world, typeId));
            buf.writeBlockPos(node.getBlockPos());

            BlockNodeDebugPacketEncoder encoder =
                GraphLibDebugRenderImpl.getDebugEncoder(graphWorld.getUniverse().getId(), typeId);
            if (encoder == null) {
                encoder = DEFAULT_ENCODER;
            }

            encoder.encode(node.getNode(), node, buf);
            indexMap.put(node.getPos(), index.getAndIncrement());
            node.getConnections().stream().map(LinkHolder::getPos).forEach(distinct::add);
        });

        PacketByteBuf linkBuf = PacketByteBufs.create();
        int written = 0;
        for (LinkPos link : distinct) {
            if (!indexMap.containsKey(link.first())) {
                GLLog.warn(
                    "Attempted to save link with non-existent node. Graph Id: {}, offending node: {}, missing node: {}",
                    graph.getId(), link.second(), link.first());
                continue;
            }
            if (!indexMap.containsKey(link.second())) {
                GLLog.warn(
                    "Attempted to save link with non-existent node. Graph Id: {}, offending node: {}, missing node: {}",
                    graph.getId(), link.first(), link.second());
                continue;
            }
            linkBuf.writeVarInt(indexMap.get(link.first()));
            linkBuf.writeVarInt(indexMap.get(link.second()));
            written++;
        }
        buf.writeVarInt(written);
        buf.writeBytes(linkBuf);
    }

    private static void sendToDebuggingPlayers(ServerWorld world, Identifier packetId, PacketByteBuf buf) {
        PlayerManager manager = world.getServer().getPlayerManager();
        for (UUID playerId : debuggingPlayers.keySet()) {
            ServerPlayerEntity player = manager.getPlayer(playerId);
            if (player != null) {
                ServerPlayNetworking.send(player, packetId, buf);
            }
        }
    }

    private static void sendToDebuggingPlayers(ServerWorld world, Identifier universe, Identifier packetId,
                                               PacketByteBuf buf) {
        PlayerManager manager = world.getServer().getPlayerManager();
        for (UUID playerId : debuggingPlayers.keySet()) {
            if (debuggingPlayers.containsEntry(playerId, universe)) {
                ServerPlayerEntity player = manager.getPlayer(playerId);
                if (player != null) {
                    ServerPlayNetworking.send(player, packetId, buf);
                }
            }
        }
    }
}
