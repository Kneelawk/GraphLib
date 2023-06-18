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

package com.kneelawk.graphlib.impl.net;

import java.util.Collection;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.ClientGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.GraphWorldImpl;

public class GLNet {
    public static void init() {
        // statically initializes everything here
    }

    public static final ParentNetId GRAPH_LIB_ID = McNetworkStack.ROOT.child(Constants.MOD_ID);

    public static final NetObjectCache<GraphUniverse> UNIVERSE_CACHE =
        NetObjectCache.createMappedIdentifier(GRAPH_LIB_ID.child("universe_cache"),
            GraphUniverse::getId, id -> {
                GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(id);
                if (universe == null) {
                    GLLog.warn("Unable to decode unknown universe {}", id);
                }
                return universe;
            });
    public static final NetObjectCache<Identifier> ID_CACHE =
        NetObjectCache.createMappedIdentifier(GRAPH_LIB_ID.child("id_cache"), Function.identity(), Function.identity());

    public static final ParentNetIdSingle<NodeEntity> NODE_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, NodeEntity.class, "node_entity", -1) {
            @Override
            protected NodeEntity readContext(NetByteBuf buffer, IMsgReadCtx ctx) {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                GraphUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    return null;
                }

                NodePos pos = NodePos.fromPacket(buffer, ctx, universe);
                if (pos == null) {
                    GLLog.warn("Unable to decode node pos");
                    return null;
                }

                GraphView view = universe.getGraphView(world);
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

                buffer.writeVarUnsignedInt(
                    UNIVERSE_CACHE.getId(ctx.getConnection(), entityCtx.getGraphWorld().getUniverse()));

                entityCtx.getPos().toPacket(buffer, ctx);
            }
        };

    public static final ParentNetIdSingle<LinkEntity> LINK_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, LinkEntity.class, "link_entity", -1) {
            @Override
            protected LinkEntity readContext(NetByteBuf buffer, IMsgReadCtx ctx) {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                GraphUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    return null;
                }

                LinkPos pos = LinkPos.fromPacket(buffer, ctx, universe);
                if (pos == null) {
                    GLLog.warn("Unable to decode link pos");
                    return null;
                }

                GraphView view = universe.getGraphView(world);
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

                buffer.writeVarUnsignedInt(
                    UNIVERSE_CACHE.getId(ctx.getConnection(), entityCtx.getGraphWorld().getUniverse()));

                entityCtx.getPos().toPacket(buffer, ctx);
            }
        };

    @SuppressWarnings("rawtypes")
    public static final ParentNetIdSingle<GraphEntity> GRAPH_ENTITY_PARENT =
        new ParentNetIdSingle<>(GRAPH_LIB_ID, GraphEntity.class, "graph_entity", -1) {
            @Override
            protected GraphEntity<?> readContext(NetByteBuf buffer, IMsgReadCtx ctx) throws InvalidInputDataException {
                World world = ctx.getConnection().getPlayer().getWorld();

                int universeIdInt = buffer.readVarUnsignedInt();
                GraphUniverse universe = UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
                if (universe == null) {
                    GLLog.warn("Unable to decode universe from unknown universe id int {}", universeIdInt);
                    return null;
                }

                GraphView view = universe.getGraphView(world);

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
                    return null;
                }

                GraphEntityType<?> type = universe.getGraphEntityType(typeId);
                if (type == null) {
                    GLLog.warn("Unable to decode unknown graph entity type {}", typeId);
                    return null;
                }

                return graph.getGraphEntity(type);
            }

            @Override
            protected void writeContext(NetByteBuf buffer, IMsgWriteCtx ctx, GraphEntity value) {
                GraphEntityContext entityCtx = value.getContext();
                buffer.writeVarUnsignedInt(
                    UNIVERSE_CACHE.getId(ctx.getConnection(), entityCtx.getGraphWorld().getUniverse()));

                buffer.writeVarUnsignedLong(entityCtx.getGraph().getId());

                buffer.writeVarUnsignedInt(ID_CACHE.getId(ctx.getConnection(), value.getType().getId()));
            }
        };

    public static <T> @Nullable T readType(@NotNull NetByteBuf buf, ActiveConnection conn,
                                           @NotNull Function<@NotNull Identifier, @Nullable T> typeGetter,
                                           @NotNull String typeName, BlockPos blockPos) {
        int typeIdInt = buf.readVarUnsignedInt();
        Identifier typeId = ID_CACHE.getObj(conn, typeIdInt);
        if (typeId == null) {
            GLLog.warn("Unable to decode unknown {} id int: {} @ {}", typeName, typeIdInt, blockPos);
            return null;
        }

        T type = typeGetter.apply(typeId);
        if (type == null) {
            GLLog.warn("Unable to decode unknown {} id: {} @ {}", typeName, typeId, blockPos);
        }

        return type;
    }

    public static void writeType(@NotNull NetByteBuf buf, @NotNull ActiveConnection conn, Identifier typeId) {
        buf.writeVarUnsignedInt(ID_CACHE.getId(conn, typeId));
    }

    public static final NetIdData CHUNK_DATA =
        new NetIdData(GRAPH_LIB_ID, "chunk_data", -1).toClientOnly().setReceiver(GLNet::receiveChunkDataPacket);

    public static void sendChunkDataPacket(GraphWorldImpl world, ServerPlayerEntity player, ChunkPos pos) {
        ActiveConnection connection = CoreMinecraftNetUtil.getConnection(player);
        CHUNK_DATA.send(connection, (buffer, ctx) -> {
            buffer.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), world.getUniverse()));
            buffer.writeVarInt(pos.x);
            buffer.writeVarInt(pos.z);
            world.writeChunkPillar(pos, buffer, ctx);
        });
    }

    private static void receiveChunkDataPacket(NetByteBuf buf, IMsgReadCtx ctx) {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "chunk data");
        if (world == null) return;

        int chunkX = buf.readVarInt();
        int chunkZ = buf.readVarInt();

        world.readChunkPillar(chunkX, chunkZ, buf, ctx);
    }

    public static final NetIdData NODE_ADD =
        new NetIdData(GRAPH_LIB_ID, "node_add", -1).toClientOnly().setReceiver(GLNet::receiveNodeAdd);

    public static void sendNodeAdd(BlockGraph graph, NodeHolder<BlockNode> node) {
        if (!(node.getBlockWorld() instanceof ServerWorld serverWorld))
            throw new IllegalArgumentException("sendNodeAdd should only be called on the server");

        GraphWorldImpl world = (GraphWorldImpl) node.getGraphWorld();
        GraphUniverse universe = world.getUniverse();
        if (!universe.isSynchronizationEnabled()) return;

        SyncProfile sp = universe.getSyncProfile();
        if (sp.getNodeFilter() != null && !sp.getNodeFilter().matches(node)) return;

        Collection<ServerPlayerEntity> watching = PlayerLookup.tracking(serverWorld, node.getBlockPos());

        for (ServerPlayerEntity player : watching) {
            if (sp.getPlayerFilter().shouldSync(player)) {
                ActiveConnection conn = CoreMinecraftNetUtil.getConnection(player);
                NODE_ADD.send(conn, (buf, ctx) -> {
                    buf.writeVarUnsignedInt(UNIVERSE_CACHE.getId(ctx.getConnection(), universe));
                    world.writeNodeAdd(graph, node, buf, ctx);
                });
            }
        }
    }

    private static void receiveNodeAdd(NetByteBuf buf, IMsgReadCtx ctx) {
        ClientGraphWorldImpl world = readClientGraphWorld(buf, ctx, "node add");
        if (world == null) return;

        world.readNodeAdd(buf, ctx);
    }

    @Nullable
    private static ClientGraphWorldImpl readClientGraphWorld(NetByteBuf buf, IMsgReadCtx ctx, String packetName) {
        int universeIdInt = buf.readVarUnsignedInt();
        GraphUniverseImpl universe = (GraphUniverseImpl) UNIVERSE_CACHE.getObj(ctx.getConnection(), universeIdInt);
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
}
