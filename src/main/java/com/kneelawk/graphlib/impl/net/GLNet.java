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

import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import alexiil.mc.lib.net.ActiveConnection;
import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;
import alexiil.mc.lib.net.NetObjectCache;
import alexiil.mc.lib.net.ParentNetId;
import alexiil.mc.lib.net.ParentNetIdSingle;
import alexiil.mc.lib.net.impl.McNetworkStack;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.ObjectType;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GLLog;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

public class GLNet {
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
            GLLog.warn("Unable to decode unknown BlockNode id: {} @ {}", typeId, blockPos);
        }

        return type;
    }

    public static void writeType(@NotNull NetByteBuf buf, @NotNull ActiveConnection conn, Identifier typeId) {
        buf.writeVarUnsignedInt(ID_CACHE.getId(conn, typeId));
    }
}
