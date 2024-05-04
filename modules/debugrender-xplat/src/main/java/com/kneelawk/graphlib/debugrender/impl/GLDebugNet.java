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
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
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
            buf.writeByte(sided.getSide().get3DDataValue());
        } else {
            // A 0 byte to distinguish ourselves from SidedBlockNode, because both implementations use the same decoder
            buf.writeByte(0);
        }
    };

    private static final Multimap<UUID, ResourceLocation> debuggingPlayers = LinkedHashMultimap.create();

    public static void onServerStart() {
        debuggingPlayers.clear();
    }

    public static void onServerStop() {
        debuggingPlayers.clear();
    }

    public static void onDisconnect(UUID playerId) {
        debuggingPlayers.removeAll(playerId);
    }

    public static void onGraphCreated(ServerLevel serverWorld, GraphWorld graphWorld, BlockGraph blockGraph) {
        sendBlockGraph(serverWorld, graphWorld, blockGraph);
    }

    public static void onGraphUpdated(ServerLevel world, GraphWorld graphWorld, BlockGraph graph) {
        sendBlockGraph(world, graphWorld, graph);
    }

    public static void onGraphDestroyed(ServerLevel world, GraphWorld graphWorld, long id) {
        ResourceLocation universeId = graphWorld.getUniverse().getId();
        sendToDebuggingPlayers(world, universeId, new GraphDestroyPayload(universeId, id));
    }

    public static void startDebuggingPlayer(ServerPlayer player, GraphUniverse universe) {
        if (!(player.level() instanceof ServerLevel world)) {
            GLLog.warn("Tried to start debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.level()));
            return;
        }

        debuggingPlayers.put(player.getUUID(), universe.getId());

        PayloadHeader header = new PayloadHeader(universe.getId(), new Int2ObjectLinkedOpenHashMap<>(),
            new FriendlyByteBuf(Unpooled.buffer()));
        Object2IntMap<ResourceLocation> paletteLookup = new Object2IntOpenHashMap<>();

        MinecraftServer server = world.getServer();
        GraphWorld graphWorld = universe.getGraphWorld(world);
        int viewDistance = server.getPlayerList().getViewDistance();

        SectionPos playerPos = player.getLastSectionPos();
        int minX = playerPos.x() - viewDistance - 1;
        int minZ = playerPos.z() - viewDistance - 1;
        int maxX = playerPos.x() + viewDistance + 1;
        int maxZ = playerPos.z() + viewDistance + 1;

        LongSet graphIds = new LongLinkedOpenHashSet();
        for (int z = minZ; z <= maxZ; z++) {
            for (int x = minX; x <= maxX; x++) {
                if (player.getChunkTrackingView().contains(x, z)) {
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

    public static void stopDebuggingPlayer(ServerPlayer player, ResourceLocation universe) {
        if (!(player.level() instanceof ServerLevel world)) {
            GLLog.warn("Tried to stop debugging a player with a world that was neither client nor server, but was {}",
                ClassUtils.classOf(player.level()));
            return;
        }

        GLDRPlatform.INSTANCE.sendPlayPayload(player, new DebuggingStopPayload(universe));

        debuggingPlayers.remove(player.getUUID(), universe);
    }

    private static void sendBlockGraph(ServerLevel world, GraphWorld graphWorld, BlockGraph graph) {
        if (debuggingPlayers.isEmpty()) {
            return;
        }

        PayloadHeader header = new PayloadHeader(graphWorld.getUniverse().getId(), new Int2ObjectLinkedOpenHashMap<>(),
            new FriendlyByteBuf(Unpooled.buffer()));
        Object2IntMap<ResourceLocation> paletteLookup = new Object2IntOpenHashMap<>();

        PayloadGraph payloadGraph = encodeBlockGraph(header, paletteLookup, graph);

        GraphUpdatePayload payload = new GraphUpdatePayload(header, payloadGraph);

        Set<ServerPlayer> sendTo = new LinkedHashSet<>();
        graph.getChunks().forEachOrdered(section -> {
            for (ServerPlayer player : world.getChunkSource().chunkMap.getPlayers(
                section.chunk(), false)) {
                if (debuggingPlayers.containsEntry(player.getUUID(), graphWorld.getUniverse().getId())) {
                    sendTo.add(player);
                }
            }
        });

        for (ServerPlayer player : sendTo) {
            GLDRPlatform.INSTANCE.sendPlayPayload(player, payload);
        }
    }

    private static PayloadGraph encodeBlockGraph(PayloadHeader header, Object2IntMap<ResourceLocation> paletteLookup,
                                                 BlockGraph graph) {
        AtomicInteger index = new AtomicInteger();
        Object2IntMap<NodePos> indexMap = new Object2IntOpenHashMap<>();
        Set<LinkPos> distinct = new LinkedHashSet<>();

        List<PayloadNode> nodes = new ObjectArrayList<>();
        graph.getNodes().forEachOrdered(node -> {
            ResourceLocation typeId = node.getNode().getType().getId();
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

    private static void sendToDebuggingPlayers(ServerLevel world, ResourceLocation universe, CustomPacketPayload payload) {
        PlayerList manager = world.getServer().getPlayerList();
        for (UUID playerId : debuggingPlayers.keySet()) {
            if (debuggingPlayers.containsEntry(playerId, universe)) {
                ServerPlayer player = manager.getPlayer(playerId);
                if (player != null) {
                    GLDRPlatform.INSTANCE.sendPlayPayload(player, payload);
                }
            }
        }
    }
}
