package com.kneelawk.graphlib.impl;

import com.kneelawk.graphlib.api.v1.GraphLib;
import com.kneelawk.graphlib.api.v1.GraphLibEvents;
import com.kneelawk.graphlib.api.v1.graph.BlockGraph;
import com.kneelawk.graphlib.api.v1.graph.BlockGraphController;
import com.kneelawk.graphlib.api.v1.graph.BlockNodeHolder;
import com.kneelawk.graphlib.api.v1.graph.NodeView;
import com.kneelawk.graphlib.api.v1.graph.struct.Link;
import com.kneelawk.graphlib.api.v1.graph.struct.Node;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoder;
import com.kneelawk.graphlib.api.v1.net.BlockNodePacketEncoderHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.SidedBlockNode;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class GraphLibCommonNetworking {
    private GraphLibCommonNetworking() {
    }

    public static final Identifier ID_MAP_BULK_ID = Constants.id("id_map_bulk");
    public static final Identifier ID_MAP_PUT_ID = Constants.id("id_map_put");
    public static final Identifier GRAPH_UPDATE_ID = Constants.id("graph_update");
    public static final Identifier GRAPH_UPDATE_BULK_ID = Constants.id("graph_update_bulk");
    public static final Identifier GRAPH_DESTROY_ID = Constants.id("graph_destroy");
    public static final Identifier DEBUGGING_STOP_ID = Constants.id("debugging_stop");

    public static final BlockNodePacketEncoder<BlockNode> DEFAULT_ENCODER = (node, holderNode, world, view, buf) -> {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(node.hashCode());
        buf.writeInt(node.getClass().getName().hashCode());

        if (node instanceof SidedBlockNode sided) {
            buf.writeByte(1);
            buf.writeByte(sided.getSide().getId());
        } else {
            buf.writeByte(0);
        }
    };

    private static final Set<UUID> debuggingPlayers = new LinkedHashSet<>();
    private static final Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();

    public static void startDebuggingPlayer(ServerPlayerEntity player) {
        sendIdMap(player);
        debuggingPlayers.add(player.getUuid());

        ServerWorld world = player.getWorld();
        MinecraftServer server = world.getServer();
        BlockGraphController controller = GraphLib.getController(world);
        int viewDistance = server.getPlayerManager().getViewDistance();

        ChunkSectionPos playerPos = player.getWatchedSection();
        int minX = playerPos.getSectionX() - viewDistance - 1;
        int minZ = playerPos.getSectionZ() - viewDistance - 1;
        int maxX = playerPos.getSectionX() + viewDistance + 1;
        int maxZ = playerPos.getSectionZ() + viewDistance + 1;

        LongSet graphs = new LongLinkedOpenHashSet();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (ThreadedAnvilChunkStorage.isWithinDistance(x, z, playerPos.getSectionX(), playerPos.getSectionZ(),
                    viewDistance)) {
                    ChunkPos pos = new ChunkPos(x, z);

                    controller.getGraphsInChunk(pos).forEach(graphs::add);
                }
            }
        }

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(graphs.size());

        for (long graphId : graphs) {
            BlockGraph graph = controller.getGraph(graphId);

            if (graph != null) {
                encodeBlockGraph(world, controller, graph, buf);
            }
        }

        ServerPlayNetworking.send(player, GRAPH_UPDATE_BULK_ID, buf);
    }

    public static void stopDebuggingPlayer(ServerPlayerEntity player) {
        debuggingPlayers.remove(player.getUuid());

        ServerPlayNetworking.send(player, DEBUGGING_STOP_ID, PacketByteBufs.empty());
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> debuggingPlayers.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> debuggingPlayers.clear());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> stopDebuggingPlayer(handler.player));

        GraphLibEvents.GRAPH_CREATED.register(GraphLibCommonNetworking::sendBlockGraph);
        GraphLibEvents.GRAPH_UPDATED.register(GraphLibCommonNetworking::sendBlockGraph);
        GraphLibEvents.GRAPH_DESTROYED.register((world, controller, id) -> {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeLong(id);

            sendToDebuggingPlayers(world, GRAPH_DESTROY_ID, buf);
        });
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

    private static void sendBlockGraph(ServerWorld world, BlockGraphController controller, BlockGraph graph) {
        if (debuggingPlayers.isEmpty()) {
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        encodeBlockGraph(world, controller, graph, buf);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        graph.getChunks().forEachOrdered(section -> {
            for (ServerPlayerEntity player : PlayerLookup.tracking(world, section.toChunkPos())) {
                if (debuggingPlayers.contains(player.getUuid())) {
                    sendTo.add(player);
                }
            }
        });

        for (ServerPlayerEntity player : sendTo) {
            ServerPlayNetworking.send(player, GRAPH_UPDATE_ID, buf);
        }
    }

    private static void encodeBlockGraph(ServerWorld world, BlockGraphController controller, BlockGraph graph,
                                         PacketByteBuf buf) {
        buf.writeLong(graph.getId());

        AtomicInteger index = new AtomicInteger();
        Map<Node<BlockNodeHolder>, Integer> indexMap = new HashMap<>();
        Set<Link<BlockNodeHolder>> distinct = new LinkedHashSet<>();

        buf.writeVarInt(graph.size());
        graph.getNodes().forEachOrdered(node -> {
            buf.writeVarInt(getIdentifierInt(world, node.data().getNode().getTypeId()));
            buf.writeBlockPos(node.data().getPos());
            encodeBlockNode(world, controller, node, buf);
            indexMap.put(node, index.getAndIncrement());
            distinct.addAll(node.connections());
        });

        buf.writeVarInt(distinct.size());
        for (Link<BlockNodeHolder> link : distinct) {
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
            buf.writeVarInt(indexMap.get(link.first()));
            buf.writeVarInt(indexMap.get(link.second()));
        }
    }

    private static void encodeBlockNode(ServerWorld world, NodeView view, Node<BlockNodeHolder> node,
                                        PacketByteBuf buf) {
        BlockNodePacketEncoderHolder<?> holder =
            GraphLib.BLOCK_NODE_PACKET_ENCODER.get(node.data().getNode().getTypeId());

        if (holder != null) {
            holder.toPacket(node.data().getNode(), node, world, view, buf);
        } else {
            DEFAULT_ENCODER.toPacket(node.data().getNode(), node, world, view, buf);
        }
    }

    private static void sendToDebuggingPlayers(ServerWorld world, Identifier packetId, PacketByteBuf buf) {
        PlayerManager manager = world.getServer().getPlayerManager();
        for (UUID playerId : debuggingPlayers) {
            ServerPlayerEntity player = manager.getPlayer(playerId);
            if (player != null) {
                ServerPlayNetworking.send(player, packetId, buf);
            }
        }
    }
}
