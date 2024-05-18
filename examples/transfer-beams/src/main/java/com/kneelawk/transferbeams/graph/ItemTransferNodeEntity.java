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
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.user.AbstractNodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.syncing.lns.api.graph.user.NodeEntitySyncing;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;
import com.kneelawk.transferbeams.util.DropHandler;
import com.kneelawk.transferbeams.util.InventoryUtil;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;
import static com.kneelawk.transferbeams.TransferBeamsMod.tt;

public class ItemTransferNodeEntity extends AbstractNodeEntity
    implements TransferNodeEntity, MenuProvider {
    private static final AABB BOUNDING_BOX =
        new AABB(4.0 / 16.0 / 4.0, 4.0 / 16.0 / 4.0, 4.0 / 16.0 / 4.0, 12.0 / 16.0 / 4.0, 12.0 / 16.0 / 4.0,
            12.0 / 16.0 / 4.0);

    public static final NodeEntityType TYPE = NodeEntityType.of(id("transfer_node"), nbt -> {
        if (!(nbt instanceof CompoundTag root)) return null;

        ItemTransferNodeEntity entity = new ItemTransferNodeEntity();
        entity.inputFilter.fromTag(root.getList("inputFilter", Tag.TAG_COMPOUND));
        entity.outputFilter.fromTag(root.getList("outputFilter", Tag.TAG_COMPOUND));
        entity.signalInventory.fromTag(root.getList("signalInventory", Tag.TAG_COMPOUND));

        return entity;
    });
    public static final NodeEntitySyncing SYNCING = NodeEntitySyncing.ofNoOp(ItemTransferNodeEntity::new);

    public static final int FILTER_INVENTORY_SIZE = 6 * 2;
    public static final int SIGNAL_INVENTORY_SIZE = 3;
    public static final int PROPERTY_COUNT = 4;
    public static final int INPUT_ALLOW_PROPERTY = 0;
    public static final int INPUT_SIDE_PROPERTY = 1;
    public static final int OUTPUT_ALLOW_PROPERTY = 2;
    public static final int OUTPUT_SIDE_PROPERTY = 3;

    private @Nullable BlockApiCache<Storage<ItemVariant>, Direction> apiCache;

    // input filter is an allow-list
    private boolean inputAllow = true;
    private @Nullable Direction inputSide = null;
    private boolean outputAllow = true;
    private @Nullable Direction outputSide = null;

    private final SimpleContainer inputFilter = new SimpleContainer(FILTER_INVENTORY_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final SimpleContainer outputFilter = new SimpleContainer(FILTER_INVENTORY_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final SimpleContainer signalInventory = new SimpleContainer(SIGNAL_INVENTORY_SIZE) {
        @Override
        public void setChanged() {
            super.setChanged();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final ContainerData properties = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case INPUT_ALLOW_PROPERTY -> inputAllow ? 1 : 0;
                case INPUT_SIDE_PROPERTY -> {
                    if (inputSide != null) yield inputSide.get3DDataValue();
                    else yield 6;
                }
                case OUTPUT_ALLOW_PROPERTY -> outputAllow ? 1 : 0;
                case OUTPUT_SIDE_PROPERTY -> {
                    if (outputSide != null) yield outputSide.get3DDataValue();
                    else yield 6;
                }
                default -> throw new IllegalStateException("Unexpected value: " + index);
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case INPUT_ALLOW_PROPERTY -> inputAllow = value != 0;
                case INPUT_SIDE_PROPERTY -> {
                    if (0 <= value && value < 6) inputSide = Direction.from3DDataValue(value);
                    else inputSide = null;
                }
                case OUTPUT_ALLOW_PROPERTY -> outputAllow = value != 0;
                case OUTPUT_SIDE_PROPERTY -> {
                    if (0 <= value && value < 6) outputSide = Direction.from3DDataValue(value);
                    else outputSide = null;
                }
                default -> throw new IllegalStateException("Unexpected value: " + index);
            }
        }

        @Override
        public int getCount() {
            return PROPERTY_COUNT;
        }
    };

    @Override
    public void onInit(@NotNull NodeEntityContext ctx) {
        super.onInit(ctx);
        if (ctx.getBlockWorld() instanceof ServerLevel serverWorld) {
            apiCache = BlockApiCache.create(ItemStorage.SIDED, serverWorld, ctx.getBlockPos());
        }
    }

    @Override
    public @NotNull NodeEntityType getType() {
        return TYPE;
    }

    @Override
    public @Nullable Tag toTag() {
        CompoundTag root = new CompoundTag();
        root.putBoolean("inputAllow", inputAllow);
        root.putByte("inputSide", (byte) (inputSide != null ? inputSide.get3DDataValue() : 6));
        root.putBoolean("outputAllow", outputAllow);
        root.putByte("outputSide", (byte) (outputSide != null ? outputSide.get3DDataValue() : 6));
        root.put("inputFilter", inputFilter.createTag());
        root.put("outputFilter", outputFilter.createTag());
        root.put("signalInventory", signalInventory.createTag());
        return root;
    }

    @Override
    public boolean hasInventory(BlockState cachedState) {
        if (apiCache == null) return false;

        return InventoryUtil.hasInventory(apiCache, cachedState);
    }

    @Override
    public void dropItems(DropHandler handler) {
        if (!(getContext().getNode() instanceof TransferBlockNode node)) return;

        handler.dropNonCreative(new ItemStack(TransferBeamsMod.ITEM_NODE_ITEMS[node.color().getId()]));
    }

    @Override
    public AABB getBoundingBox() {
        return BOUNDING_BOX;
    }

    @Override
    public void onActivate(ServerPlayer player) {
        player.openMenu(this);
    }

    @Override
    public Component getDisplayName() {
        if (!(getContext().getNode() instanceof TransferBlockNode node)) return tt("title", "item_transfer_node");

        return TransferBeamsMod.ITEM_NODE_ITEMS[node.color().getId()].getDescription();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerEntity) {
        return new ItemNodeScreenHandler(syncId, playerInventory, inputFilter, outputFilter, signalInventory,
            properties);
    }
}
