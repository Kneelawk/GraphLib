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

package com.kneelawk.multiblocklamps.block;

import java.util.Collection;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.multiblocklamps.LampLogic;
import com.kneelawk.multiblocklamps.MultiblockLamps;
import com.kneelawk.multiblocklamps.node.LampConnectorNode;

public class LampConnectorBlock extends Block implements ConnectableBlock {
    //
    // Vanilla Stuff
    //

    public static final MapCodec<ConnectedLampBlock> CODEC = method_54094(ConnectedLampBlock::new);

    public LampConnectorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    //
    // GraphLib Stuff
    //

    @Override
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        // only update nodes on the server
        if (world instanceof ServerWorld serverWorld) {
            MultiblockLamps.UNIVERSE.get().getServerGraphWorld(serverWorld).updateNodes(pos);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos,
                               boolean notify) {
        if (world instanceof ServerWorld serverWorld) {
            // grab a node holder for the node that *should* be at our current position and update it
            NodeHolder<BlockNode> node = MultiblockLamps.UNIVERSE.get().getServerGraphWorld(serverWorld)
                .getNodeAt(new NodePos(pos, LampConnectorNode.INSTANCE));
            if (node != null) {
                LampLogic.onLampUpdated(node);
            }
        }
    }

    //
    // Custom Stuff
    //

    @Override
    public Collection<BlockNode> createNodes() {
        return List.of(LampConnectorNode.INSTANCE);
    }
}
