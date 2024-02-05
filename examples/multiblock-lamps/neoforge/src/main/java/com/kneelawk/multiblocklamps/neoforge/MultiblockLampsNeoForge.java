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

package com.kneelawk.multiblocklamps.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;

import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.multiblocklamps.MultiblockLamps;

import static com.kneelawk.multiblocklamps.MultiblockLamps.CONNECTED_LAMP_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.LAMP_CONNECTOR_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.id;

@Mod(MultiblockLamps.MOD_ID)
@SuppressWarnings("unused")
public class MultiblockLampsNeoForge {
    public MultiblockLampsNeoForge(IEventBus modBus) {
        modBus.addListener(this::onRegister);
        modBus.addListener(this::onCreativeTab);
    }

    public void onRegister(RegisterEvent event) {
        event.register(GraphLibImpl.UNIVERSE_KEY, _helper -> MultiblockLamps.registerUniverse());
        event.register(RegistryKeys.BLOCK, helper -> {
            helper.register(id("connected_lamp"), CONNECTED_LAMP_BLOCK);
            helper.register(id("lamp_connector"), LAMP_CONNECTOR_BLOCK);
        });
        event.register(RegistryKeys.ITEM, helper -> {
            helper.register(id("connected_lamp"), new BlockItem(CONNECTED_LAMP_BLOCK, new Item.Settings()));
            helper.register(id("lamp_connector"), new BlockItem(LAMP_CONNECTOR_BLOCK, new Item.Settings()));
        });
    }

    public void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ItemGroups.REDSTONE_BLOCKS) {
            event.addStack(new ItemStack(CONNECTED_LAMP_BLOCK.asItem()));
            event.addStack(new ItemStack(LAMP_CONNECTOR_BLOCK.asItem()));
        }
    }
}
