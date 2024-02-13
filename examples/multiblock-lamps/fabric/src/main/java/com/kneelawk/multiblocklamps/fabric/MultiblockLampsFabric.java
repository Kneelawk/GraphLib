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

package com.kneelawk.multiblocklamps.fabric;

import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.fabric.api.GraphLibFabric;

import static com.kneelawk.multiblocklamps.MultiblockLamps.CONNECTED_LAMP_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.LAMP_CONNECTOR_BLOCK;

public class MultiblockLampsFabric implements ModInitializer {
    public static final List<Pair<Identifier, GraphUniverse>> UNIVERSES = new ObjectArrayList<>();
    public static final List<Pair<Identifier, Block>> BLOCKS = new ObjectArrayList<>();
    public static final List<Pair<Identifier, Item>> ITEMS = new ObjectArrayList<>();
    public static final List<Pair<Identifier, MapCodec<? extends Block>>> BLOCK_TYPES = new ObjectArrayList<>();

    @Override
    public void onInitialize() {
        register(UNIVERSES, GraphLibFabric.UNIVERSE);
        register(BLOCKS, Registries.BLOCK);
        register(ITEMS, Registries.ITEM);
        register(BLOCK_TYPES, Registries.BLOCK_TYPE);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE_BLOCKS).register(entries -> {
            entries.addItem(CONNECTED_LAMP_BLOCK.get());
            entries.addItem(LAMP_CONNECTOR_BLOCK.get());
        });
    }

    private static <T> void register(List<Pair<Identifier, T>> list, Registry<T> registry) {
        for (var pair : list) {
            Registry.register(registry, pair.getLeft(), pair.getRight());
        }
    }
}
