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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.ItemTransferNodeEntity;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.util.InventoryUtil;

public class NodeItem extends Item implements InteractionCancellerItem {
    private final DyeColor color;

    public NodeItem(DyeColor color, Settings settings) {
        super(settings);
        this.color = color;
    }

    @Override
    public ActionResult interceptBlockUse(ItemStack stack, PlayerEntity player, World world, Hand hand,
                                          BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        if (!InventoryUtil.hasInventory(world, blockPos)) return ActionResult.PASS;

        GraphView syncedView = TransferBeamsMod.SYNCED.getSidedGraphView(world);
        // getSidedGraphView may return null if world is not a ClientWorld or a ServerWorld, like with Create.
        if (syncedView == null) return ActionResult.FAIL;

        NodePos nodePos = new NodePos(blockPos, new TransferBlockNode(color));

        if (world.isClient()) {
            // The fact that nodes are synced means we can tell client-side if the node of our color already exists.
            if (syncedView.nodeExistsAt(nodePos)) {
                return ActionResult.FAIL;
            } else {
                // send event to the server
                return ActionResult.SUCCESS;
            }
        } else if (world instanceof ServerWorld serverWorld) {
            // the synced view exists on both client and server
            if (syncedView.nodeExistsAt(nodePos)) {
                return ActionResult.FAIL;
            } else {
                // the editable graph world only exists on the server
                GraphWorld graphWorld = TransferBeamsMod.UNIVERSE.getServerGraphWorld(serverWorld);

                graphWorld.addBlockNode(nodePos, new ItemTransferNodeEntity());

                return ActionResult.CONSUME;
            }
        } else {
            // handle weirdness
            return ActionResult.FAIL;
        }
    }
}
