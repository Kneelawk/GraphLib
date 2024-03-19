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
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;

import com.kneelawk.multiblocklamps.MultiblockLamps;

import static com.kneelawk.multiblocklamps.MultiblockLamps.CONNECTED_LAMP_BLOCK;
import static com.kneelawk.multiblocklamps.MultiblockLamps.LAMP_CONNECTOR_BLOCK;

@Mod(MultiblockLamps.MOD_ID)
@SuppressWarnings("unused")
public class MultiblockLampsNeoForge {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MultiblockLamps.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MultiblockLamps.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
        DeferredRegister.create(RegistryKeys.BLOCK_TYPE, MultiblockLamps.MOD_ID);

    public MultiblockLampsNeoForge(IEventBus modBus) {
        MultiblockLamps.init();

        modBus.addListener(this::onInit);
        modBus.addListener(this::onCreativeTab);

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_TYPES.register(modBus);
    }

    public void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(MultiblockLamps::initUniverse);
    }

    public void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == ItemGroups.REDSTONE_BLOCKS) {
            event.addStack(new ItemStack(CONNECTED_LAMP_BLOCK.get().asItem()));
            event.addStack(new ItemStack(LAMP_CONNECTOR_BLOCK.get().asItem()));
        }
    }
}
