package com.kneelawk.graphlib.impl;

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
import net.minecraft.server.world.ThreadedChunkManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.event.GraphLibEvents;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeLink;
import com.kneelawk.graphlib.api.node.PosLinkKey;
import com.kneelawk.graphlib.api.node.PosNodeKey;

public final class GraphLibCommonNetworking {
    private GraphLibCommonNetworking() {
    }

    public static final Identifier ID_MAP_BULK_ID = Constants.id("id_map_bulk");
    public static final Identifier ID_MAP_PUT_ID = Constants.id("id_map_put");
    public static final Identifier GRAPH_UPDATE_ID = Constants.id("graph_update");
    public static final Identifier GRAPH_UPDATE_BULK_ID = Constants.id("graph_update_bulk");
    public static final Identifier GRAPH_DESTROY_ID = Constants.id("graph_destroy");
    public static final Identifier DEBUGGING_STOP_ID = Constants.id("debugging_stop");

    private static final Multimap<UUID, Identifier> debuggingPlayers = LinkedHashMultimap.create();
    private static final Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> debuggingPlayers.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> debuggingPlayers.clear());
        ServerPlayConnectionEvents.DISCONNECT.register(
            (handler, server) -> debuggingPlayers.removeAll(handler.player.getUuid()));

        GraphLibEvents.GRAPH_CREATED.register(GraphLibCommonNetworking::sendBlockGraph);
        GraphLibEvents.GRAPH_UPDATED.register(GraphLibCommonNetworking::sendBlockGraph);
        GraphLibEvents.GRAPH_DESTROYED.register((world, graphWorld, id) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeVarInt(getIdentifierInt(world, graphWorld.getUniverse()));
            buf.writeLong(id);

            sendToDebuggingPlayers(world, graphWorld.getUniverse(), GRAPH_DESTROY_ID, buf);
        });
    }

    public static void startDebuggingPlayer(ServerPlayerEntity player, GraphUniverse universe) {
        sendIdMap(player);
        debuggingPlayers.put(player.getUuid(), universe.getId());
        ServerWorld world = (ServerWorld) player.getWorld();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(getIdentifierInt(world, universe.getId()));

        MinecraftServer server = world.getServer();
        GraphWorld graphWorld = universe.getGraphWorld(world);
        int viewDistance = server.getPlayerManager().getViewDistance();

        ChunkSectionPos playerPos = player.getWatchedSection();
        int minX = playerPos.getSectionX() - viewDistance - 1;
        int minZ = playerPos.getSectionZ() - viewDistance - 1;
        int maxX = playerPos.getSectionX() + viewDistance + 1;
        int maxZ = playerPos.getSectionZ() + viewDistance + 1;

        LongSet graphIds = new LongLinkedOpenHashSet();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (ThreadedChunkManager.isWithinDistance(x, z, playerPos.getSectionX(), playerPos.getSectionZ(),
                    viewDistance)) {
                    ChunkPos pos = new ChunkPos(x, z);

                    graphWorld.getGraphsInChunk(pos).forEach(graphIds::add);
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
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(getIdentifierInt((ServerWorld) player.getWorld(), universe));

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
        buf.writeVarInt(getIdentifierInt(world, graphWorld.getUniverse()));

        encodeBlockGraph(world, graphWorld, graph, buf);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        graph.getChunks().forEachOrdered(section -> {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, section.toChunkPos())) {
                if (debuggingPlayers.containsEntry(player.getUuid(), graphWorld.getUniverse())) {
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
        Map<PosNodeKey, Integer> indexMap = new HashMap<>();
        Set<PosLinkKey> distinct = new LinkedHashSet<>();

        buf.writeVarInt(graph.size());
        graph.getNodes().forEachOrdered(node -> {
            buf.writeVarInt(getIdentifierInt(world, node.getNode().getTypeId()));
            buf.writeBlockPos(node.getPos());
            node.getNode().toPacket(node, world, graphWorld, buf);
            indexMap.put(node.toNodeKey(), index.getAndIncrement());

            for (NodeLink link : node.getConnections().values()) {
                distinct.add(PosLinkKey.from(link));
            }
        });

        PacketByteBuf linkBuf = PacketByteBufs.create();
        int written = 0;
        for (PosLinkKey link : distinct) {
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
