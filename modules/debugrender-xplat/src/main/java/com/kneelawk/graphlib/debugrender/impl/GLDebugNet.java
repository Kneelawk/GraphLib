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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import io.netty.buffer.Unpooled;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.ColorUtils;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.debugrender.api.graph.BlockNodeDebugPacketEncoder;
import com.kneelawk.graphlib.debugrender.impl.payload.DebuggingStopPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphDestroyPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdateBulkPayload;
import com.kneelawk.graphlib.debugrender.impl.payload.GraphUpdatePayload;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadGraph;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadHeader;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadLink;
import com.kneelawk.graphlib.debugrender.impl.payload.PayloadNode;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.util.ClassUtils;

public final class GLDebugNet {
    private GLDebugNet() {
    }

    public static final BlockNodeDebugPacketEncoder DEFAULT_ENCODER = (node, self, buf) -> {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(node.hashCode());

        // Get the default color for our node type
        GraphUniverse universe = self.getGraphWorld().getUniverse();
        int color = ColorUtils.hsba2Argb(
            (float) universe.getNodeTypeIndex(node.getType().getId()) / (float) universe.getNodeTypeCount(),
            1f, 1f, 1f);
        buf.writeInt(color);

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

    public static void onServerStart() {
        debuggingPlayers.clear();
    }

    public static void onServerStop() {
        debuggingPlayers.clear();
    }

    public static void onDisconnect(UUID playerId) {
        debuggingPlayers.removeAll(playerId);
    }

    public static void onGraphCreated(ServerWorld serverWorld, GraphWorld graphWorld, BlockGraph blockGraph) {
        sendBlockGraph(serverWorld, graphWorld, blockGraph);
    }

    public static void onGraphUpdated(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        sendBlockGraph(world, graphWorld, graph);
    }

    public static void onGraphDestroyed(ServerWorld world, GraphWorld graphWorld, long id) {
        Identifier universeId = graphWorld.getUniverse().getId();
        sendToDebuggingPlayers(world, universeId, new GraphDestroyPayload(universeId, id));
    }

    public static void startDebuggingPlayer(ServerPlayerEntity player, GraphUniverse universe) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            GLLog.warn("Tried to start debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.getWorld()));
            return;
        }

        debuggingPlayers.put(player.getUuid(), universe.getId());

        PayloadHeader header = new PayloadHeader(universe.getId(), new Int2ObjectLinkedOpenHashMap<>(),
            new PacketByteBuf(Unpooled.buffer()));
        Object2IntMap<Identifier> paletteLookup = new Object2IntOpenHashMap<>();

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

        List<PayloadGraph> payloadGraphs = new ObjectArrayList<>();
        for (BlockGraph graph : graphs) {
            PayloadGraph payloadGraph = encodeBlockGraph(header, paletteLookup, graph);
            payloadGraphs.add(payloadGraph);
        }

        GraphUpdateBulkPayload payload = new GraphUpdateBulkPayload(header, payloadGraphs);

        GLDRPlatform.INSTANCE.sendPlayPayload(player, payload);
    }

    public static void stopDebuggingPlayer(ServerPlayerEntity player, Identifier universe) {
        if (!(player.getWorld() instanceof ServerWorld world)) {
            GLLog.warn("Tried to stop debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.getWorld()));
            return;
        }

        GLDRPlatform.INSTANCE.sendPlayPayload(player, new DebuggingStopPayload(universe));

        debuggingPlayers.remove(player.getUuid(), universe);
    }

    private static void sendBlockGraph(ServerWorld world, GraphWorld graphWorld, BlockGraph graph) {
        if (debuggingPlayers.isEmpty()) {
            return;
        }

        PayloadHeader header = new PayloadHeader(graphWorld.getUniverse().getId(), new Int2ObjectLinkedOpenHashMap<>(),
            new PacketByteBuf(Unpooled.buffer()));
        Object2IntMap<Identifier> paletteLookup = new Object2IntOpenHashMap<>();

        PayloadGraph payloadGraph = encodeBlockGraph(header, paletteLookup, graph);

        GraphUpdatePayload payload = new GraphUpdatePayload(header, payloadGraph);

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        graph.getChunks().forEachOrdered(section -> {
            for (ServerPlayerEntity player : world.getChunkManager().delegate.getPlayersWatchingChunk(
                section.toChunkPos(), false)) {
                if (debuggingPlayers.containsEntry(player.getUuid(), graphWorld.getUniverse().getId())) {
                    sendTo.add(player);
                }
            }
        });

        for (ServerPlayerEntity player : sendTo) {
            GLDRPlatform.INSTANCE.sendPlayPayload(player, payload);
        }
    }

    private static PayloadGraph encodeBlockGraph(PayloadHeader header, Object2IntMap<Identifier> paletteLookup,
                                                 BlockGraph graph) {
        AtomicInteger index = new AtomicInteger();
        Object2IntMap<NodePos> indexMap = new Object2IntOpenHashMap<>();
        Set<LinkPos> distinct = new LinkedHashSet<>();

        List<PayloadNode> nodes = new ObjectArrayList<>();
        graph.getNodes().forEachOrdered(node -> {
            Identifier typeId = node.getNode().getType().getId();
            int typeIdInt;
            if (paletteLookup.containsKey(typeId)) {
                typeIdInt = paletteLookup.getInt(typeId);
            } else {
                typeIdInt = header.palette().size();
                paletteLookup.put(typeId, typeIdInt);
                header.palette().put(typeIdInt, typeId);
            }

            BlockNodeDebugPacketEncoder encoder = GraphLibDebugRenderImpl.getDebugEncoder(header.universeId(), typeId);
            if (encoder == null) {
                encoder = DEFAULT_ENCODER;
            }

            encoder.encode(node.getNode(), node, header.nodeData());

            indexMap.put(node.getPos(), index.getAndIncrement());
            node.getConnections().stream().map(LinkHolder::getPos).forEach(distinct::add);
            nodes.add(new PayloadNode(typeIdInt, node.getBlockPos()));
        });

        List<PayloadLink> links = new ObjectArrayList<>();
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
            links.add(new PayloadLink(indexMap.getInt(link.first()), indexMap.getInt(link.second())));
        }

        return new PayloadGraph(graph.getId(), nodes, links);
    }

    private static void sendToDebuggingPlayers(ServerWorld world, Identifier universe, CustomPayload payload) {
        PlayerManager manager = world.getServer().getPlayerManager();
        for (UUID playerId : debuggingPlayers.keySet()) {
            if (debuggingPlayers.containsEntry(playerId, universe)) {
                ServerPlayerEntity player = manager.getPlayer(playerId);
                if (player != null) {
                    GLDRPlatform.INSTANCE.sendPlayPayload(player, payload);
                }
            }
        }
    }
}
