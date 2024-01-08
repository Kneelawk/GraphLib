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

package com.kneelawk.transferbeams.screen;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import alexiil.mc.lib.net.NetIdDataK;
import alexiil.mc.lib.net.ParentNetIdCast;
import alexiil.mc.lib.net.impl.CoreMinecraftNetUtil;
import alexiil.mc.lib.net.impl.McNetworkStack;

import static com.kneelawk.transferbeams.TransferBeamsMod.str;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.FILTER_INVENTORY_SIZE;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.INPUT_ALLOW_PROPERTY;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.INPUT_SIDE_PROPERTY;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.OUTPUT_ALLOW_PROPERTY;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.OUTPUT_SIDE_PROPERTY;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.PROPERTY_COUNT;
import static com.kneelawk.transferbeams.graph.ItemTransferNodeEntity.SIGNAL_INVENTORY_SIZE;

public class ItemNodeScreenHandler extends ScreenHandler {
    public static final ScreenHandlerType<ItemNodeScreenHandler> TYPE =
        new ScreenHandlerType<>(ItemNodeScreenHandler::new, FeatureFlags.DEFAULT_SET);

    private static final ParentNetIdCast<ScreenHandler, ItemNodeScreenHandler> NET_PARENT =
        McNetworkStack.SCREEN_HANDLER.subType(ItemNodeScreenHandler.class, str("item_node"));
    private static final NetIdDataK<ItemNodeScreenHandler> INPUT_ALLOW_ID = NET_PARENT.idData("input_allow", 1)
        .setReceiver((handler, buf, ctx) -> handler.setInputAllow(buf.readByte() != 0)).toServerOnly();
    private static final NetIdDataK<ItemNodeScreenHandler> INPUT_SIDE_ID =
        NET_PARENT.idData("input_side", 1).setReceiver((handler, buf, ctx) -> {
            byte value = buf.readByte();
            handler.setInputSide(0 <= value && value < 6 ? Direction.byId(value) : null);
        }).toServerOnly();
    private static final NetIdDataK<ItemNodeScreenHandler> OUTPUT_ALLOW_ID = NET_PARENT.idData("output_allow", 1)
        .setReceiver((handler, buf, ctx) -> handler.setOutputAllow(buf.readByte() != 0)).toServerOnly();
    private static final NetIdDataK<ItemNodeScreenHandler> OUTPUT_SIDE_ID =
        NET_PARENT.idData("output_side", 1).setReceiver((handler, buf, ctx) -> {
            byte value = buf.readByte();
            handler.setOutputSide(0 <= value && value < 6 ? Direction.byId(value) : null);
        }).toServerOnly();

    public final World world;
    public final Inventory outputFilter;
    public final Inventory inputFilter;
    public final Inventory signalInventory;
    public final PropertyDelegate properties;

    public final List<TabSlot> tabSlots = new ObjectArrayList<>();
    public final List<FilterSlot> outputSlots = new ObjectArrayList<>();
    public final List<FilterSlot> inputSlots = new ObjectArrayList<>();
    public final List<SignalSlot> signalSlots = new ObjectArrayList<>();

    public ItemNodeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(FILTER_INVENTORY_SIZE),
            new SimpleInventory(FILTER_INVENTORY_SIZE),
            new SimpleInventory(SIGNAL_INVENTORY_SIZE),
            new ArrayPropertyDelegate(PROPERTY_COUNT));
    }

    public ItemNodeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inputFilter,
                                 Inventory outputFilter, Inventory signalInventory, PropertyDelegate properties) {
        super(TYPE, syncId);
        this.world = playerInventory.player.getWorld();
        this.outputFilter = outputFilter;
        this.inputFilter = inputFilter;
        this.signalInventory = signalInventory;
        this.properties = properties;
        addProperties(properties);

        // add player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, 6 + j * 18, 26 + 5 + 9 + 18 * 2 + 9 + 1 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, 6 + i * 18, 26 + 5 + 9 + 18 * 2 + 9 + 18 * 3 + 4 + 1));
        }

        // add output filter
        for (int i = 0; i < FILTER_INVENTORY_SIZE; i++) {
            FilterSlot slot = new FilterSlot(outputFilter, i, 5 + 27 + 1 + (i % 6) * 18, 26 + 5 + 9 + 1 + i / 6 * 18);
            addSlot(slot);
            tabSlots.add(slot);
            outputSlots.add(slot);
        }

        // add input filter
        for (int i = 0; i < FILTER_INVENTORY_SIZE; i++) {
            FilterSlot slot = new FilterSlot(inputFilter, i, 5 + 27 + 1 + (i % 6) * 18, 26 + 5 + 9 + 1 + i / 6 * 18);
            addSlot(slot);
            tabSlots.add(slot);
            inputSlots.add(slot);
        }

        // add signal slots
        for (int i = 0; i < SIGNAL_INVENTORY_SIZE; i++) {
            SignalSlot slot = new SignalSlot(signalInventory, i, 5 + 54 + 1 + i * 18, 26 + 5 + 9 + 9 + 1);
            addSlot(slot);
            tabSlots.add(slot);
            signalSlots.add(slot);
        }
    }

    @Override
    public ItemStack quickTransfer(PlayerEntity player, int fromIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public boolean getInputAllow() {
        return properties.get(INPUT_ALLOW_PROPERTY) != 0;
    }

    public void setInputAllow(boolean allow) {
        if (world.isClient) {
            INPUT_ALLOW_ID.send(CoreMinecraftNetUtil.getClientConnection(), this,
                (obj, buf, ctx) -> buf.writeByte(allow ? 1 : 0));
        } else {
            properties.set(INPUT_ALLOW_PROPERTY, allow ? 1 : 0);
        }
    }

    public @Nullable Direction getInputSide() {
        int value = properties.get(INPUT_SIDE_PROPERTY);
        return 0 <= value && value < 6 ? Direction.byId(value) : null;
    }

    public void setInputSide(@Nullable Direction side) {
        if (world.isClient) {
            INPUT_SIDE_ID.send(CoreMinecraftNetUtil.getClientConnection(), this,
                (obj, buf, ctx) -> buf.writeByte(side != null ? side.getId() : 6));
        } else {
            properties.set(INPUT_SIDE_PROPERTY, side != null ? side.getId() : 6);
        }
    }

    public boolean getOutputAllow() {
        return properties.get(OUTPUT_ALLOW_PROPERTY) != 0;
    }

    public void setOutputAllow(boolean allow) {
        if (world.isClient) {
            OUTPUT_ALLOW_ID.send(CoreMinecraftNetUtil.getClientConnection(), this,
                (obj, buf, ctx) -> buf.writeByte(allow ? 1 : 0));
        } else {
            properties.set(OUTPUT_ALLOW_PROPERTY, allow ? 1 : 0);
        }
    }

    public @Nullable Direction getOutputSide() {
        int value = properties.get(OUTPUT_SIDE_PROPERTY);
        return 0 <= value && value < 6 ? Direction.byId(value) : null;
    }

    public void setOutputSide(@Nullable Direction side) {
        if (world.isClient) {
            OUTPUT_SIDE_ID.send(CoreMinecraftNetUtil.getClientConnection(), this,
                (obj, buf, ctx) -> buf.writeByte(side != null ? side.getId() : 6));
        } else {
            properties.set(OUTPUT_SIDE_PROPERTY, side != null ? side.getId() : 6);
        }
    }

    public interface TabSlot {
        void setEnabled(boolean enabled);
    }

    public static class FilterSlot extends Slot implements TabSlot {
        private boolean enabled = false;

        public FilterSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }

    public static class SignalSlot extends Slot implements TabSlot {
        private boolean enabled = false;

        public SignalSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }
    }
}
