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

package com.kneelawk.multiblocklamps;

import java.util.List;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
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

public class MultiblockLampsMod implements ModInitializer {
    public static final String MOD_ID = "multiblock_lamps";

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("graph_universe"));

    // Cache to make node lookups faster
    public static final CacheCategory<LampInputNode> LAMP_INPUT_CACHE = CacheCategory.of(LampInputNode.class);
    public static final CacheCategory<LampNode> LAMP_CACHE = CacheCategory.of(LampNode.class);

    public static final Block CONNECTED_LAMP_BLOCK = new ConnectedLampBlock(
        FabricBlockSettings.create().luminance(state -> state.get(ConnectedLampBlock.LIT) ? 15 : 0).strength(0.3f)
            .sounds(BlockSoundGroup.GLASS).allowsSpawning((_state, _view, _pos, _type) -> true));
    public static final Block LAMP_CONNECTOR_BLOCK =
        new LampConnectorBlock(FabricBlockSettings.create().mapColor(MapColor.STONE).strength(1.5f, 6.0f));

    @Override
    public void onInitialize() {
        UNIVERSE.register();
        UNIVERSE.addNodeTypes(ConnectedLampNode.TYPE);
        UNIVERSE.addNodeTypes(LampConnectorNode.TYPE);
        UNIVERSE.addDiscoverer(
            (world, pos) -> world.getBlockState(pos).getBlock() instanceof ConnectableBlock connectable ?
                connectable.createNodes() : List.of());

        Registry.register(Registries.BLOCK, id("connected_lamp"), CONNECTED_LAMP_BLOCK);
        Registry.register(Registries.ITEM, id("connected_lamp"),
            new BlockItem(CONNECTED_LAMP_BLOCK, new FabricItemSettings()));
        Registry.register(Registries.BLOCK_TYPE, id("connected_lamp"), ConnectedLampBlock.CODEC);
        Registry.register(Registries.BLOCK, id("lamp_connector"), LAMP_CONNECTOR_BLOCK);
        Registry.register(Registries.ITEM, id("lamp_connector"),
            new BlockItem(LAMP_CONNECTOR_BLOCK, new FabricItemSettings()));
        Registry.register(Registries.BLOCK_TYPE, id("lamp_connector"), LampConnectorBlock.CODEC);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE_BLOCKS).register(entries -> {
            entries.addItem(CONNECTED_LAMP_BLOCK);
            entries.addItem(LAMP_CONNECTOR_BLOCK);
        });
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
