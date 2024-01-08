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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.user.AbstractNodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.syncing.api.graph.user.NodeEntitySyncing;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;
import com.kneelawk.transferbeams.util.DropHandler;
import com.kneelawk.transferbeams.util.InventoryUtil;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;
import static com.kneelawk.transferbeams.TransferBeamsMod.tt;

public class ItemTransferNodeEntity extends AbstractNodeEntity
    implements TransferNodeEntity, NamedScreenHandlerFactory {
    private static final Box BOUNDING_BOX =
        new Box(4.0 / 16.0 / 4.0, 4.0 / 16.0 / 4.0, 4.0 / 16.0 / 4.0, 12.0 / 16.0 / 4.0, 12.0 / 16.0 / 4.0,
            12.0 / 16.0 / 4.0);

    public static final NodeEntityType TYPE = NodeEntityType.of(id("transfer_node"), nbt -> {
        if (!(nbt instanceof NbtCompound root)) return null;

        ItemTransferNodeEntity entity = new ItemTransferNodeEntity();
        entity.inputFilter.readNbtList(root.getList("inputFilter", NbtElement.COMPOUND_TYPE));
        entity.outputFilter.readNbtList(root.getList("outputFilter", NbtElement.COMPOUND_TYPE));
        entity.signalInventory.readNbtList(root.getList("signalInventory", NbtElement.COMPOUND_TYPE));

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

    private final SimpleInventory inputFilter = new SimpleInventory(FILTER_INVENTORY_SIZE) {
        @Override
        public void markDirty() {
            super.markDirty();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final SimpleInventory outputFilter = new SimpleInventory(FILTER_INVENTORY_SIZE) {
        @Override
        public void markDirty() {
            super.markDirty();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final SimpleInventory signalInventory = new SimpleInventory(SIGNAL_INVENTORY_SIZE) {
        @Override
        public void markDirty() {
            super.markDirty();
            if (ctx != null) ctx.markDirty();
        }
    };
    private final PropertyDelegate properties = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case INPUT_ALLOW_PROPERTY -> inputAllow ? 1 : 0;
                case INPUT_SIDE_PROPERTY -> {
                    if (inputSide != null) yield inputSide.getId();
                    else yield 6;
                }
                case OUTPUT_ALLOW_PROPERTY -> outputAllow ? 1 : 0;
                case OUTPUT_SIDE_PROPERTY -> {
                    if (outputSide != null) yield outputSide.getId();
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
                    if (0 <= value && value < 6) inputSide = Direction.byId(value);
                    else inputSide = null;
                }
                case OUTPUT_ALLOW_PROPERTY -> outputAllow = value != 0;
                case OUTPUT_SIDE_PROPERTY -> {
                    if (0 <= value && value < 6) outputSide = Direction.byId(value);
                    else outputSide = null;
                }
                default -> throw new IllegalStateException("Unexpected value: " + index);
            }
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

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
        NbtCompound root = new NbtCompound();
        root.putBoolean("inputAllow", inputAllow);
        root.putByte("inputSide", (byte) (inputSide != null ? inputSide.getId() : 6));
        root.putBoolean("outputAllow", outputAllow);
        root.putByte("outputSide", (byte) (outputSide != null ? outputSide.getId() : 6));
        root.put("inputFilter", inputFilter.toNbtList());
        root.put("outputFilter", outputFilter.toNbtList());
        root.put("signalInventory", signalInventory.toNbtList());
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
    public Box getBoundingBox() {
        return BOUNDING_BOX;
    }

    @Override
    public void onActivate(ServerPlayerEntity player) {
        player.openHandledScreen(this);
    }

    @Override
    public Text getDisplayName() {
        if (!(getContext().getNode() instanceof TransferBlockNode node)) return tt("title", "item_transfer_node");

        return TransferBeamsMod.ITEM_NODE_ITEMS[node.color().getId()].getName();
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ItemNodeScreenHandler(syncId, playerInventory, inputFilter, outputFilter, signalInventory,
            properties);
    }
}
