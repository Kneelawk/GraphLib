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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;

public class ItemNodeScreenHandler extends ScreenHandler {
    public ItemNodeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(ItemTransferNodeEntity.FILTER_INVENTORY_SIZE),
            new SimpleInventory(ItemTransferNodeEntity.FILTER_INVENTORY_SIZE),
            new SimpleInventory(ItemTransferNodeEntity.SIGNAL_INVENTORY_SIZE),
            new ArrayPropertyDelegate(ItemTransferNodeEntity.PROPERTY_COUNT));
    }

    public ItemNodeScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inputFilter,
                                 Inventory outputFilter, Inventory signalInventory, PropertyDelegate properties) {
        super(TransferBeamsMod.ITEM_SCREEN_HANDLER, syncId);
        addProperties(properties);

        // add player inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(
                    new Slot(playerInventory, j + i * 9 + 9, 6 + j * 18, 26 + 5 + 9 + 18 * 2 + 9 + 1 + i * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 6 + i * 18, 26 + 5 + 9 + 18 * 2 + 9 + 18 * 3 + 4 + 1));
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
}
