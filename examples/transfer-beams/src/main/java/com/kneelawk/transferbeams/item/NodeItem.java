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

package com.kneelawk.transferbeams.item;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public class NodeItem extends Item implements InteractionCancellerItem {
    private final DyeColor color;

    public NodeItem(DyeColor color, Properties settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public InteractionResult interceptBlockUse(ItemStack stack, Player player, Level world, InteractionHand hand,
                                          BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        if (!InventoryUtil.hasInventory(world, blockPos)) return InteractionResult.PASS;

        GraphView syncedView = TransferBeamsMod.SYNCED.getSidedGraphView(world);
        // getSidedGraphView may return null if world is not a ClientWorld or a ServerWorld, like with Create.
        if (syncedView == null) return InteractionResult.FAIL;

        NodePos nodePos = new NodePos(blockPos, new TransferBlockNode(color));

        if (world.isClientSide()) {
            // The fact that nodes are synced means we can tell client-side if the node of our color already exists.
            if (syncedView.nodeExistsAt(nodePos)) {
                return InteractionResult.FAIL;
            } else {
                // send event to the server
                return InteractionResult.SUCCESS;
            }
        } else if (world instanceof ServerLevel serverWorld) {
            // the synced view exists on both client and server
            if (syncedView.nodeExistsAt(nodePos)) {
                return InteractionResult.FAIL;
            } else {
                // the editable graph world only exists on the server
                GraphWorld graphWorld = TransferBeamsMod.UNIVERSE.getGraphWorld(serverWorld);

                graphWorld.addBlockNode(nodePos, new ItemTransferNodeEntity());

                return InteractionResult.CONSUME;
            }
        } else {
            // handle weirdness
            return InteractionResult.FAIL;
        }
    }
}
