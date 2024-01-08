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
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.block.Block;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.graph.TransferGraphEntity;
import com.kneelawk.transferbeams.graph.TransferLinkEntity;
import com.kneelawk.transferbeams.graph.TransferLinkKey;
import com.kneelawk.transferbeams.item.ConfigToolItem;
import com.kneelawk.transferbeams.item.InteractionCancellerItem;
import com.kneelawk.transferbeams.item.LinkToolItem;
import com.kneelawk.transferbeams.item.NodeItem;
import com.kneelawk.transferbeams.net.TBNet;
import com.kneelawk.transferbeams.screen.ItemNodeScreenHandler;

public class TransferBeamsMod implements ModInitializer {
    public static final String MOD_ID = "transfer_beams";

    public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("beams"));
    public static final SyncedUniverse SYNCED = SyncedUniverse.builder().build(UNIVERSE);

    public static final TagKey<Item> NODE_VISUALIZERS = TagKey.of(RegistryKeys.ITEM, id("node_visualizers"));
    public static final TagKey<Item> NODE_SELECTORS = TagKey.of(RegistryKeys.ITEM, id("node_selectors"));
    public static final TagKey<Block> WORLDGEN_NODE_HOLDERS =
        TagKey.of(RegistryKeys.BLOCK, id("worldgen_node_holders"));

    public static final Item[] ITEM_NODE_ITEMS = new Item[DyeColor.values().length];

    static {
        for (DyeColor color : DyeColor.values()) {
            ITEM_NODE_ITEMS[color.getId()] = new NodeItem(color, new FabricItemSettings());
        }
    }

    public static final Item CONFIG_TOOL_ITEM = new ConfigToolItem(new FabricItemSettings());
    public static final Item LINK_TOOL_ITEM = new LinkToolItem(new FabricItemSettings());

    @Override
    public void onInitialize() {
        LOG.info("Transfer Beams initializing...");

        TBNet.init();
        registerUniverse();
        registerItems();
        registerEvents();
        registerScreens();

        LOG.info("Transfer Beams initialized.");
    }

    private static void registerUniverse() {
        UNIVERSE.register();
        SYNCED.register();

        UNIVERSE.addNodeType(TransferBlockNode.TYPE);
        SYNCED.addNodeSyncing(TransferBlockNode.TYPE, TransferBlockNode.SYNCING);
        UNIVERSE.addNodeEntityType(ItemTransferNodeEntity.TYPE);
        SYNCED.addNodeEntitySyncing(ItemTransferNodeEntity.TYPE, ItemTransferNodeEntity.SYNCING);

        UNIVERSE.addLinkKeyType(TransferLinkKey.TYPE);
        SYNCED.addLinkKeySyncing(TransferLinkKey.TYPE, TransferLinkKey.SYNCING);
        UNIVERSE.addLinkEntityType(TransferLinkEntity.TYPE);
        SYNCED.addLinkEntitySyncing(TransferLinkEntity.TYPE, TransferLinkEntity.SYNCING);

        UNIVERSE.addGraphEntityType(TransferGraphEntity.TYPE);
        SYNCED.addGraphEntitySyncing(TransferGraphEntity.TYPE, TransferGraphEntity.SYNCING);
    }

    private static void registerItems() {
        for (DyeColor color : DyeColor.values()) {
            Registry.register(Registries.ITEM, id(color.getName() + "_item_transfer_node"),
                ITEM_NODE_ITEMS[color.getId()]);
        }

        Registry.register(Registries.ITEM, id("config_tool"), CONFIG_TOOL_ITEM);
        Registry.register(Registries.ITEM, id("link_tool"), LINK_TOOL_ITEM);

        ItemGroup itemGroup = FabricItemGroup.builder().name(tt("itemGroup", "main")).entries((params, collector) -> {
            collector.addItem(CONFIG_TOOL_ITEM);
            collector.addItem(LINK_TOOL_ITEM);
            for (DyeColor color : DyeColor.values()) {
                collector.addItem(ITEM_NODE_ITEMS[color.getId()]);
            }
        }).icon(() -> new ItemStack(ITEM_NODE_ITEMS[DyeColor.GRAY.getId()])).build();
        Registry.register(Registries.ITEM_GROUP, id("main"), itemGroup);
    }

    private static void registerEvents() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) return ActionResult.PASS;
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof InteractionCancellerItem canceller) {
                return canceller.interceptBlockUse(stack, player, world, hand, hitResult);
            }
            return ActionResult.PASS;
        });
    }

    private static void registerScreens() {
        Registry.register(Registries.SCREEN_HANDLER_TYPE, id("item_node"), ItemNodeScreenHandler.TYPE);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix, Object... args) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }

    public static MutableText gui(String suffix, Object... args) {
        return tt("gui", suffix, args);
    }

    public static String str(String path) {
        return MOD_ID + ":" + path;
    }
}
