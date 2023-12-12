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

package com.kneelawk.multiblocklamps.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.wire.FullWireBlockNode;
import com.kneelawk.multiblocklamps.LampLogic;
import com.kneelawk.multiblocklamps.MultiblockLampsMod;
import com.kneelawk.multiblocklamps.block.ConnectedLampBlock;

import static com.kneelawk.multiblocklamps.MultiblockLampsMod.id;

public class ConnectedLampNode implements BlockNode, FullWireBlockNode, LampInputNode, LampNode {
    public static final ConnectedLampNode INSTANCE = new ConnectedLampNode();
    public static final BlockNodeType TYPE = BlockNodeType.of(id("connected_lamp"), () -> INSTANCE);

    private ConnectedLampNode() {}

    @Override
    public @NotNull BlockNodeType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        // This node is a singleton so no data needs to be encoded
        return null;
    }

    @Override
    public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {
        LampLogic.onLampUpdated(self);
    }

    //
    // Our custom logic
    //

    @Override
    public boolean isPowered(NodeHolder<LampInputNode> self) {
        return self.getBlockWorld().isReceivingRedstonePower(self.getBlockPos());
    }

    @Override
    public void setLit(NodeHolder<LampNode> self, boolean lit) {
        if (self.getBlockState().isOf(MultiblockLampsMod.CONNECTED_LAMP_BLOCK)) {
            self.getBlockWorld().setBlockState(self.getBlockPos(),
                MultiblockLampsMod.CONNECTED_LAMP_BLOCK.getDefaultState().with(ConnectedLampBlock.LIT, lit));
        }
    }
}
