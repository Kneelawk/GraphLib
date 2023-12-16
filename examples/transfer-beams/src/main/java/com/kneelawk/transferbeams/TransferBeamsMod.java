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

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkEntityPacketEncoder;
import com.kneelawk.graphlib.syncing.api.graph.user.LinkKeyPacketEncoder;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.graph.TransferLinkEntity;
import com.kneelawk.transferbeams.graph.TransferLinkKey;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;
import com.kneelawk.transferbeams.item.InteractionCancellerItem;
import com.kneelawk.transferbeams.item.NodeItem;

public class TransferBeamsMod implements ModInitializer {
    public static final String MOD_ID = "transfer_beams";

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("beams"));
    public static final SyncedUniverse SYNCED = SyncedUniverse.builder().build(UNIVERSE);

    public static final TagKey<Item> NODE_VISUALIZERS = TagKey.of(RegistryKeys.ITEM, id("node_visualizers"));
    public static final TagKey<Block> WORLDGEN_NODE_HOLDERS = TagKey.of(RegistryKeys.BLOCK, id("worldgen_node_holders"));

    public static final Item ITEM_NODE_ITEM = new NodeItem(new FabricItemSettings());

    @Override
    public void onInitialize() {
        registerUniverse();
        registerItems();
        registerEvents();
    }

    private static void registerUniverse() {
        UNIVERSE.register();
        SYNCED.register();

        UNIVERSE.addNodeType(TransferBlockNode.TYPE);
        SYNCED.addNodeSyncing(TransferBlockNode.TYPE, TransferBlockNode::toPacket, TransferBlockNode::new);
        UNIVERSE.addNodeEntityType(ItemTransferNodeEntity.TYPE);
        SYNCED.addNodeEntitySyncing(ItemTransferNodeEntity.TYPE, ItemTransferNodeEntity::toPacket, ItemTransferNodeEntity::new);

        UNIVERSE.addLinkKeyType(TransferLinkKey.TYPE);
        // TODO: make this less clunky, maybe via LinkKeySyncing
        SYNCED.addLinkKeySyncing(TransferLinkKey.TYPE, LinkKeyPacketEncoder.noop(),
            (buf, ctx) -> TransferLinkKey.INSTANCE);
        UNIVERSE.addLinkEntityType(TransferLinkEntity.TYPE);
        SYNCED.addLinkEntitySyncing(TransferLinkEntity.TYPE, LinkEntityPacketEncoder.noop(),
            (buf, msgCtx) -> new TransferLinkEntity());
    }

    private static void registerItems() {
        Registry.register(Registries.ITEM, id("item_node"), ITEM_NODE_ITEM);

        ItemGroup itemGroup = FabricItemGroup.builder().name(tt("itemGroup", "main")).entries((params, collector) -> {
            collector.addItem(ITEM_NODE_ITEM);
        }).build();
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

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix);
    }
}
