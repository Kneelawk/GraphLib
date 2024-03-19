/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.lns.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import alexiil.mc.lib.net.ActiveConnection;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.InvalidInputDataException;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.NetIdData;
import alexiil.mc.lib.net.NetObjectCache;
import alexiil.mc.lib.net.ParentNetId;
import alexiil.mc.lib.net.ParentNetIdSingle;
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil;
import alexiil.mc.lib.net.impl.McNetworkStack;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.graph.BlockGraphImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.util.ClassUtils;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;
import com.kneelawk.graphlib.syncing.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;
import com.kneelawk.graphlib.syncing.lns.api.graph.LNSSyncedUniverse;
import com.kneelawk.graphlib.syncing.lns.api.util.PacketEncodingUtil;

public final class LNSNetworking {
    private LNSNetworking() {}

    public static void init() {
        // statically initializes everything here
    }

    public static final ParentNetId GRAPH_LIB_ID = McNetworkStack.ROOT.child(Constants.MOD_ID);

    public static final NetObjectCache<LNSSyncedUniverse> UNIVERSE_CACHE =
        NetObjectCache.createMappedIdentifier(GRAPH_LIB_ID.child("universe_cache"),
            SyncedUniverse::getId, id -> {
                SyncedUniverseImpl universe = GraphLibSyncingImpl.SYNCED_UNIVERSE.get(id);
                if (universe == null) {
                    GLLog.warn("Unable to decode unknown universe {}", id);
                }
                if (!(universe instanceof LNSSyncedUniverse lnsSynced)) {
                    GLLog.warn("Decoded universe of wrong type! Expected LNSSyncedUniverse but found " + universe.getClass());
                    return null;
                }
                return lnsSynced;
            });
    public static final NetObjectCache<Identifier> ID_CACHE =
        NetObjectCache.createMappedIdentifier(GRAPH_LIB_ID.child("id_cache"), Function.identity(), Function.identity());

    public static final ParentNetIdSingle<NodeEntity> NODE_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, NodeEntity.class, "node_entity", -1) {
            @Override
            protected NodeEntity readContext(NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                LNSSyncedUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    throw new InvalidInputDataException(
                        "Unable to decode universe from unknown universe id int " + universeIdInt);
                }

                NodePos pos = PacketEncodingUtil.decodeNodePos(buffer, ctx, universe);

                GraphView view = universe.getSidedGraphView(world);
                if (view == null) {
                    throw new IllegalStateException(
                        "Player's world was neither client nor server, but was " + ClassUtils.classOf(world) +
                            ". Unable to decode node entity packet.");
                }

                NodeEntity entity = view.getNodeEntity(pos);
                if (entity == null) {
                    GLLog.warn("Failed to find node entity @ {} in world {} and universe {}", pos, world,
                        universe.getId());
                }

                return entity;
            }

            @Override
            protected void writeContext(NetByteBuf buffer, IMsgWriteCtx ctx, NodeEntity value) {
                NodeEntityContext entityCtx = value.getContext();
                LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(entityCtx.getGraphWorld());

                buffer.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));

                PacketEncodingUtil.encodeNodePos(entityCtx.getPos(), buffer, ctx, universe);
            }
        };

    public static final ParentNetIdSingle<LinkEntity> LINK_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, LinkEntity.class, "link_entity", -1) {
            @Override
            protected LinkEntity readContext(NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                LNSSyncedUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    throw new InvalidInputDataException(
                        "Unable to decode universe from unknown universe id int " + universeIdInt);
                }

                LinkPos pos = PacketEncodingUtil.decodeLinkPos(buffer, ctx, universe);

                GraphView view = universe.getSidedGraphView(world);
                if (view == null) {
                    throw new IllegalStateException(
                        "Player's world was neither client nor server, but was " + ClassUtils.classOf(world) +
                            ". Unable to decode link entity packet.");
                }

                LinkEntity entity = view.getLinkEntity(pos);
                if (entity == null) {
                    GLLog.warn("Failed to find link entity @ {} in world {} and universe {}", pos, world,
                        universe.getId());
                }

                return entity;
            }

            @Override
            protected void writeContext(NetByteBuf buffer, IMsgWriteCtx ctx, LinkEntity value) {
                LinkEntityContext entityCtx = value.getContext();
                LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(entityCtx.getGraphWorld());

                buffer.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));

                PacketEncodingUtil.encodeLinkPos(entityCtx.getPos(), buffer, ctx, universe);
            }
        };

    @SuppressWarnings("rawtypes")
    public static final ParentNetIdSingle<GraphEntity> GRAPH_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, GraphEntity.class, "graph_entity", -1) {
            @Override
            protected GraphEntity<?> readContext(NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                SyncedUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    throw new InvalidInputDataException(
                        "Unable to decode universe from unknown universe id int " + universeIdInt);
                }

                GraphView view = universe.getSidedGraphView(world);
                if (view == null) {
                    throw new IllegalStateException(
                        "Player's world was neither client nor server, but was " + ClassUtils.classOf(world) +
                            ". Unable to decode graph entity packet.");
                }

                long graphId = buffer.readVarUnsignedLong();
                BlockGraph graph = view.getGraph(graphId);
                if (graph == null) {
                    GLLog.warn("Unable to find graph for id {} in world {} in universe {}", graphId, world,
                        universe.getId());
                    return null;
                }

                int typeIdInt = buffer.readVarUnsignedInt();
                Identifier typeId = ID_CACHE.getObj(ctx.getConnection(), typeIdInt);
                if (typeId == null) {
                    GLLog.warn("Unable to decode graph entity type id from int {}", typeIdInt);
                    throw new InvalidInputDataException("Unable to decode graph entity type id from int " + typeIdInt);
                }

                GraphEntityType<?> type = universe.getUniverse().getGraphEntityType(typeId);
                if (type == null) {
                    GLLog.warn("Encountered unknown graph entity type {}", typeId);
                    throw new InvalidInputDataException("Encountered unknown graph entity type " + typeId);
                }

                return graph.getGraphEntity(type);
            }

            @Override
            protected void writeContext(NetByteBuf buffer, IMsgWriteCtx ctx, GraphEntity value) {
                GraphEntityContext entityCtx = value.getContext();
                LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(entityCtx.getGraphWorld());

                buffer.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));

                buffer.writeVarUnsignedLong(entityCtx.getGraph().getId());

                buffer.writeVarUnsignedInt(ID_CACHE.getId(ctx.getConnection(), value.getType().getId()));
            }
        };

    public static <T> @NotNull T readType(@NotNull NetByteBuf buf, ActiveConnection conn,
                                          @NotNull Function<@NotNull Identifier, @Nullable T> typeGetter,
                                          @NotNull String typeName, BlockPos blockPos)
        throws InvalidInputDataException {
        int typeIdInt = buf.readVarUnsignedInt();
        Identifier typeId = ID_CACHE.getObj(conn, typeIdInt);
        if (typeId == null) {
            GLLog.warn("Unable to decode unknown {} id int: {} @ {}", typeName, typeIdInt, blockPos);
            throw new InvalidInputDataException(
                "Unable to decode unknown " + typeName + " id int: " + typeIdInt + " @ " + blockPos);
        }

        T type = typeGetter.apply(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown {} id: {} @ {}", typeName, typeId, blockPos);
            throw new InvalidInputDataException(
                "Unable to decode unknown " + typeName + " id: " + typeId + " @ " + blockPos);
        }

        return type;
    }

    public static void writeType(@NotNull NetByteBuf buf, @NotNull ActiveConnection conn, Identifier typeId) {
        buf.writeVarUnsignedInt(ID_CACHE.getId(conn, typeId));
    }

    public static final NetIdData CHUNK_DATA =
        new NetIdData(GRAPH_LIB_ID, "chunk_data", -1).toClientOnly().setReceiver(LNSNetworking::receiveChunkDataPacket);

    public static void sendChunkDataPacket(ServerGraphWorldImpl world, ServerPlayerEntity player, ChunkPos pos) {
        ActiveConnection connection = CoreMinecraftNetUtil.getConnection(player);
        CHUNK_DATA.send(connection, (buffer, ctx) -> {
            buffer.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), GraphLibSyncingLNSImpl.getUniverse(world)));
            buffer.writeVarInt(pos.x);
            buffer.writeVarInt(pos.z);
            LNSEncoding.writeChunkPillar(pos, world, buffer, ctx);
        });
    }

    private static void receiveChunkDataPacket(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "chunk data");
        if (world == null) return;

        int chunkX = buf.readVarInt();
        int chunkZ = buf.readVarInt();

        LNSDecoding.readChunkPillar(world, chunkX, chunkZ, buf, ctx);
    }

    public static final NetIdData NODE_ADD =
        new NetIdData(GRAPH_LIB_ID, "node_add", -1).toClientOnly().setReceiver(LNSNetworking::receiveNodeAdd);

    public static void sendNodeAdd(BlockGraphImpl graph, NodeHolder<BlockNode> node) {
        if (!(graph.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendNodeAdd should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(node)) return;

        Collection<ServerPlayerEntity> watching = PlayerLookup.tracking(world.getWorld(), node.getBlockPos());

        for (ServerPlayerEntity player : watching) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                NODE_ADD.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeNodeAdd(graph, node, buf, ctx);
                });
            }
        }
    }

    private static void receiveNodeAdd(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "node add");
        if (world == null) return;

        LNSDecoding.readNodeAdd(world, buf, ctx);
    }

    @Nullable
    private static ClientGraphWorldImpl readClientGraphWorld(NetByteBuf buf, IMsgReadCtx ctx, String packetName) {
        int universeIdInt = buf.readVarUnsignedInt();
        SyncedUniverseImpl universe = (SyncedUniverseImpl) UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
        if (universe == null) {
            GLLog.warn("Received {} packet for unknown universe id int: {}", packetName, universeIdInt);
            ctx.drop("Received " + packetName + " for unknown universe");
            return null;
        }

        ClientGraphWorldImpl world = universe.getClientGraphView();
        if (world == null) {
            GLLog.warn("Received {} packet but the client GraphWorld was null", packetName);
            ctx.drop("Received " + packetName + " but client GraphWorld was null");
            return null;
        }
        return world;
    }

    public static final NetIdData GRAPH_MERGE =
        new NetIdData(GRAPH_LIB_ID, "graph_merge", -1).toClientOnly().setReceiver(LNSNetworking::receiveMerge);

    public static void sendMerge(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(into.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendMerge should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        for (var iter = into.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(PlayerLookup.tracking(world.getWorld(), iter.next().toChunkPos()));
        }
        for (var iter = from.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(PlayerLookup.tracking(world.getWorld(), iter.next().toChunkPos()));
        }

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                GRAPH_MERGE.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeMerge(from, into, buf, ctx);
                });
            }
        }
    }

    private static void receiveMerge(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "graph merge");
        if (world == null) return;

        LNSDecoding.readMerge(world, buf, ctx);
    }

    public static final NetIdData NODE_LINK =
        new NetIdData(GRAPH_LIB_ID, "node_link", -1).toClientOnly().setReceiver(LNSNetworking::receiveLink);

    public static void sendLink(BlockGraphImpl graph, LinkHolder<LinkKey> link) {
        if (!(graph.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendLink should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(link.getFirst()) && nodeFilter.matches(link.getSecond())))
            return;

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        sendTo.addAll(PlayerLookup.tracking(world.getWorld(), link.getFirstBlockPos()));
        sendTo.addAll(PlayerLookup.tracking(world.getWorld(), link.getSecondBlockPos()));

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                NODE_LINK.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeLink(graph, link, buf, ctx);
                });
            }
        }
    }

    private static void receiveLink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "node link");
        if (world == null) return;

        LNSDecoding.readLink(world, buf, ctx);
    }

    public static final NetIdData NODE_UNLINK =
        new NetIdData(GRAPH_LIB_ID, "node_unlink", -1).toClientOnly().setReceiver(LNSNetworking::receiveUnlink);

    public static void sendUnlink(BlockGraphImpl graph, NodeHolder<BlockNode> a, NodeHolder<BlockNode> b, LinkKey key) {
        if (!(graph.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendUnlink should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        CacheCategory<?> nodeFilter = sp.getNodeFilter();
        if (nodeFilter != null && !(nodeFilter.matches(a) && nodeFilter.matches(b))) return;

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        sendTo.addAll(PlayerLookup.tracking(world.getWorld(), a.getBlockPos()));
        sendTo.addAll(PlayerLookup.tracking(world.getWorld(), b.getBlockPos()));

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                NODE_UNLINK.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeUnlink(graph, a, b, key, buf, ctx);
                });
            }
        }
    }

    private static void receiveUnlink(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "node unlink");
        if (world == null) return;

        LNSDecoding.readUnlink(world, buf, ctx);
    }

    public static final NetIdData GRAPH_SPLIT =
        new NetIdData(GRAPH_LIB_ID, "graph_split", -1).toClientOnly().setReceiver(LNSNetworking::receiveSplitInto);

    public static void sendSplitInto(BlockGraphImpl from, BlockGraphImpl into) {
        if (!(into.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendSplitInto should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        Set<ServerPlayerEntity> sendTo = new LinkedHashSet<>();
        for (var iter = into.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(PlayerLookup.tracking(world.getWorld(), iter.next().toChunkPos()));
        }
        for (var iter = from.getChunks().iterator(); iter.hasNext(); ) {
            sendTo.addAll(PlayerLookup.tracking(world.getWorld(), iter.next().toChunkPos()));
        }

        for (ServerPlayerEntity player : sendTo) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                GRAPH_SPLIT.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeSplitInto(from, into, buf, ctx);
                });
            }
        }
    }

    private static void receiveSplitInto(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "graph split");
        if (world == null) return;

        LNSDecoding.readSplitInto(world, buf, ctx);
    }

    public static final NetIdData NODE_REMOVE =
        new NetIdData(GRAPH_LIB_ID, "node_remove", -1).toClientOnly().setReceiver(LNSNetworking::receiveNodeRemove);

    public static void sendNodeRemove(BlockGraphImpl graph, NodeHolder<BlockNode> holder) {
        if (!(graph.getGraphView() instanceof ServerGraphWorldImpl world))
            throw new IllegalArgumentException("sendNodeRemove should only be called on the logical server");

        LNSSyncedUniverse universe = GraphLibSyncingLNSImpl.getUniverse(world);
        SyncProfile sp = universe.getSyncProfile();
        if (!sp.isEnabled()) return;

        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(holder)) return;

        Collection<ServerPlayerEntity> watching = PlayerLookup.tracking(world.getWorld(), holder.getBlockPos());

        for (ServerPlayerEntity player : watching) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                NODE_REMOVE.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    LNSEncoding.writeNodeRemove(graph, holder, buf, ctx);
                });
            }
        }
    }

    private static void receiveNodeRemove(NetByteBuf buf, IMsgReadCtx ctx) throws InvalidInputDataException {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "node remove");
        if (world == null) return;

        LNSDecoding.readNodeRemove(world, buf, ctx);
    }
}