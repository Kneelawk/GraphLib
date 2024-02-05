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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import com.kneelawk.multiblocklamps.MultiblockLamps;
import com.kneelawk.multiblocklamps.block.ConnectedLampBlock;
import com.kneelawk.multiblocklamps.block.LampConnectorBlock;

import static com.kneelawk.multiblocklamps.MultiblockLamps.CONNECTED_LAMP_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.LAMP_CONNECTOR_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.id;

public class MultiblockLampsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MultiblockLamps.registerUniverse();

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
}
