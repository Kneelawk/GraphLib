/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

import java.util.function.Supplier;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.multiblocklamps.MLPlatform;

import static com.kneelawk.multiblocklamps.MultiblockLamps.id;

public class MLPlatformImpl implements MLPlatform {
    @Override
    public <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                               MapCodec<? extends Block> codec) {
        Identifier id = id(path);
        T block = creator.get();
        MultiblockLampsFabric.BLOCKS.add(new Pair<>(id, block));
        MultiblockLampsFabric.ITEMS.add(new Pair<>(id, new BlockItem(block, new FabricItemSettings())));
        MultiblockLampsFabric.BLOCK_TYPES.add(new Pair<>(id, codec));
        return () -> block;
    }

    @Override
    public Supplier<GraphUniverse> registerUniverse(String path, Supplier<GraphUniverse> creator) {
        GraphUniverse universe = creator.get();
        MultiblockLampsFabric.UNIVERSES.add(new Pair<>(id(path), universe));
        return () -> universe;
    }
}
