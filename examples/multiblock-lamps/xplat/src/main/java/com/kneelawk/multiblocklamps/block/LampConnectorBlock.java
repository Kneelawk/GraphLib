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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.serialization.MapCodec;
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

    public static final MapCodec<ConnectedLampBlock> CODEC = simpleCodec(ConnectedLampBlock::new);

    public LampConnectorBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    //
    // GraphLib Stuff
    //

    @Override
    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor world, BlockPos pos, int flags, int maxUpdateDepth) {
        // only update nodes on the server
        if (world instanceof ServerLevel serverWorld) {
            MultiblockLamps.UNIVERSE.getGraphWorld(serverWorld).updateNodes(pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos,
                               boolean notify) {
        if (world instanceof ServerLevel serverWorld) {
            // grab a node holder for the node that *should* be at our current position and update it
            NodeHolder<BlockNode> node = MultiblockLamps.UNIVERSE.getGraphWorld(serverWorld)
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
