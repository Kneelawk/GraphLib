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

package com.kneelawk.transferbeams.mixin.impl;

import java.util.List;
import java.util.PrimitiveIterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.TransferNodeEntity;

/**
 * Detects when an inventory block has been removed and removes its associated nodes if there are any.
 */
@Mixin(ServerWorld.class)
public class ServerWorldMixin {
    @Inject(
        method = "onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V",
        at = @At("HEAD"))
    private void onBlockChangedHook(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) (Object) this;
        world.getServer().execute(() -> {
            GraphWorld graphWorld = TransferBeamsMod.UNIVERSE.getServerGraphWorld(world);
            PrimitiveIterator.OfLong iter = graphWorld.getAllGraphIdsAt(pos).iterator();
            if (iter.hasNext()) {
                List<NodeHolder<BlockNode>> collected = new ObjectArrayList<>();
                while (iter.hasNext()) {
                    BlockGraph graph = graphWorld.getGraph(iter.nextLong());
                    if (graph != null) {
                        graph.getNodesAt(pos).forEach(collected::add);
                    }
                }

                // Remove all the nodes if the block no longer has an inventory
                for (NodeHolder<BlockNode> node : collected) {
                    TransferNodeEntity entity = node.getNodeEntity(TransferNodeEntity.class);
                    if (entity == null) continue;

                    if (!entity.hasInventory(newBlock)) {
                        graphWorld.removeBlockNode(node.getPos());
                    }
                }
            }
        });
    }
}
