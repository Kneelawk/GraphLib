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

package com.kneelawk.transferbeams.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;

import alexiil.mc.lib.net.IMsgReadCtx;
import alexiil.mc.lib.net.IMsgWriteCtx;
import alexiil.mc.lib.net.NetByteBuf;

import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.user.AbstractNodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.transferbeams.util.InventoryUtil;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class ItemTransferNodeEntity extends AbstractNodeEntity implements TransferNodeEntity {
    public static final NodeEntityType TYPE = NodeEntityType.of(id("transfer_node"), ItemTransferNodeEntity::new);

    private @Nullable BlockApiCache<Storage<ItemVariant>, Direction> apiCache;

    public ItemTransferNodeEntity() {}

    public ItemTransferNodeEntity(NetByteBuf buf, IMsgReadCtx ctx) {this();}

    public void toPacket(NetByteBuf buf, IMsgWriteCtx ctx) {}

    @Override
    public void onInit(@NotNull NodeEntityContext ctx) {
        super.onInit(ctx);
        if (ctx.getBlockWorld() instanceof ServerWorld serverWorld) {
            apiCache = BlockApiCache.create(ItemStorage.SIDED, serverWorld, ctx.getBlockPos());
        }
    }

    @Override
    public @NotNull NodeEntityType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public boolean hasInventory(BlockState cachedState) {
        if (apiCache == null) return false;

        return InventoryUtil.hasInventory(apiCache, cachedState);
    }

    @Override
    public void dropItems() {

    }
}
