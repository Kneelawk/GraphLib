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

package com.kneelawk.transferbeams.datagen;

import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import com.kneelawk.transferbeams.TransferBeamsMod;

public class TransferBeamsItemTagGen extends FabricTagProvider.ItemTagProvider {
    public TransferBeamsItemTagGen(FabricDataOutput output,
                                   CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        TagAppender<Item> nodeVisualizers = tag(TransferBeamsMod.NODE_VISUALIZERS);
        add(nodeVisualizers, TransferBeamsMod.ITEM_NODE_ITEMS);
        add(nodeVisualizers, TransferBeamsMod.CONFIG_TOOL_ITEM, TransferBeamsMod.LINK_TOOL_ITEM);
        TagAppender<Item> nodeSelectors = tag(TransferBeamsMod.NODE_SELECTORS);
        add(nodeSelectors, TransferBeamsMod.CONFIG_TOOL_ITEM, TransferBeamsMod.LINK_TOOL_ITEM);
    }

    private void add(TagAppender<Item> appender, Item... items) {
        for (Item item : items) {
            appender.add(item.builtInRegistryHolder().key());
        }
    }
}
