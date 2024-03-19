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

package com.kneelawk.multiblocklamps;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.multiblocklamps.block.ConnectableBlock;
import com.kneelawk.multiblocklamps.block.ConnectedLampBlock;
import com.kneelawk.multiblocklamps.block.LampConnectorBlock;
import com.kneelawk.multiblocklamps.node.ConnectedLampNode;
import com.kneelawk.multiblocklamps.node.LampConnectorNode;
import com.kneelawk.multiblocklamps.node.LampInputNode;
import com.kneelawk.multiblocklamps.node.LampNode;

public class MultiblockLamps {
    public static final String MOD_ID = "multiblock_lamps";

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("graph_universe"));

    // Cache to make node lookups faster
    public static final CacheCategory<LampInputNode> LAMP_INPUT_CACHE = CacheCategory.of(LampInputNode.class);
    public static final CacheCategory<LampNode> LAMP_CACHE = CacheCategory.of(LampNode.class);

    public static final Supplier<Block> CONNECTED_LAMP_BLOCK =
        MLPlatform.INSTANCE.registerBlockWithItem("connected_lamp", () -> new ConnectedLampBlock(
                AbstractBlock.Settings.create().luminance(state -> state.get(ConnectedLampBlock.LIT) ? 15 : 0)
                    .strength(0.3f).sounds(BlockSoundGroup.GLASS).allowsSpawning((_state, _view, _pos, _type) -> true)),
            ConnectedLampBlock.CODEC);
    public static final Supplier<Block> LAMP_CONNECTOR_BLOCK =
        MLPlatform.INSTANCE.registerBlockWithItem("lamp_connector",
            () -> new LampConnectorBlock(AbstractBlock.Settings.create().mapColor(MapColor.STONE).strength(1.5f, 6.0f)),
            LampConnectorBlock.CODEC);
    
    public static void initUniverse() {
        UNIVERSE.register();
        UNIVERSE.addNodeTypes(ConnectedLampNode.TYPE);
        UNIVERSE.addNodeTypes(LampConnectorNode.TYPE);
        UNIVERSE.addDiscoverer(
            (world, pos) -> world.getBlockState(pos).getBlock() instanceof ConnectableBlock connectable ?
                connectable.createNodes() : List.of());
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
