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

package com.kneelawk.transferbeams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.fabric.api.KNetRegistrarFabric;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.graph.TransferGraphEntity;
import com.kneelawk.transferbeams.graph.TransferLinkEntity;
import com.kneelawk.transferbeams.graph.TransferLinkKey;
import com.kneelawk.transferbeams.item.ConfigToolItem;
import com.kneelawk.transferbeams.item.InteractionCancellerItem;
import com.kneelawk.transferbeams.item.LinkToolItem;
import com.kneelawk.transferbeams.item.NodeItem;
import com.kneelawk.transferbeams.item.NodePosComponent;
import com.kneelawk.transferbeams.net.TBNet;
import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;

public class TransferBeamsMod implements ModInitializer {
    public static final String MOD_ID = "transfer_beams";

    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("beams"));
    public static final KNetSyncedUniverse SYNCED = KNetSyncedUniverse.builder().build(UNIVERSE);

    public static final TagKey<Item> NODE_VISUALIZERS = TagKey.create(Registries.ITEM, id("node_visualizers"));
    public static final TagKey<Item> NODE_SELECTORS = TagKey.create(Registries.ITEM, id("node_selectors"));
    public static final TagKey<Block> WORLDGEN_NODE_HOLDERS =
        TagKey.create(Registries.BLOCK, id("worldgen_node_holders"));

    public static final Item[] ITEM_NODE_ITEMS = new Item[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            ITEM_NODE_ITEMS[color.getId()] = new NodeItem(color, new Item.Properties());
        }
    }

    public static final Item CONFIG_TOOL_ITEM = new ConfigToolItem(new Item.Properties());
    public static final Item LINK_TOOL_ITEM = new LinkToolItem(new Item.Properties());

    @Override
    public void onInitialize() {
        LOG.info("Transfer Beams initializing...");

        registerUniverse();
        registerItems();
        registerEvents();
        registerScreens();
        registerNetworking(new KNetRegistrarFabric());

        LOG.info("Transfer Beams initialized.");
    }

    private static void registerUniverse() {
        UNIVERSE.register();
        SYNCED.register();

        UNIVERSE.addNodeType(TransferBlockNode.TYPE);
        SYNCED.addNodeSyncing(TransferBlockNode.SYNCING);
        UNIVERSE.addNodeEntityType(ItemTransferNodeEntity.TYPE);
        SYNCED.addNodeEntitySyncing(ItemTransferNodeEntity.SYNCING);

        UNIVERSE.addLinkKeyType(TransferLinkKey.TYPE);
        SYNCED.addLinkKeySyncing(TransferLinkKey.SYNCING);
        UNIVERSE.addLinkEntityType(TransferLinkEntity.TYPE);
        SYNCED.addLinkEntitySyncing(TransferLinkEntity.SYNCING);

        UNIVERSE.addGraphEntityType(TransferGraphEntity.TYPE);
        SYNCED.addGraphEntitySyncing(TransferGraphEntity.SYNCING);
    }

    private static void registerItems() {
        for (DyeColor color : DyeColor.values()) {
            Registry.register(BuiltInRegistries.ITEM, id(color.getName() + "_item_transfer_node"),
                ITEM_NODE_ITEMS[color.getId()]);
        }

        Registry.register(BuiltInRegistries.ITEM, id("config_tool"), CONFIG_TOOL_ITEM);
        Registry.register(BuiltInRegistries.ITEM, id("link_tool"), LINK_TOOL_ITEM);

        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("node_pos"), NodePosComponent.TYPE);

        CreativeModeTab itemGroup =
            FabricItemGroup.builder().title(tt("itemGroup", "main")).displayItems((params, collector) -> {
                collector.accept(CONFIG_TOOL_ITEM);
                collector.accept(LINK_TOOL_ITEM);
                for (DyeColor color : DyeColor.values()) {
                    collector.accept(ITEM_NODE_ITEMS[color.getId()]);
                }
            }).icon(() -> new ItemStack(ITEM_NODE_ITEMS[DyeColor.GRAY.getId()])).build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), itemGroup);
    }

    private static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) return InteractionResult.PASS;
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof InteractionCancellerItem canceller) {
                return canceller.interceptBlockUse(stack, player, world, hand, hitResult);
            }
            return InteractionResult.PASS;
        });
    }

    private static void registerScreens() {
        Registry.register(BuiltInRegistries.MENU, id("item_node"), ItemNodeScreenHandler.TYPE);
    }

    private static void registerNetworking(KNetRegistrar registrar) {
        TBNet.init(registrar);
        ItemNodeScreenHandler.init(registrar);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static MutableComponent tt(String prefix, String suffix, Object... args) {
        return Component.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }

    public static MutableComponent gui(String suffix, Object... args) {
        return tt("gui", suffix, args);
    }

    public static String str(String path) {
        return MOD_ID + ":" + path;
    }
}
