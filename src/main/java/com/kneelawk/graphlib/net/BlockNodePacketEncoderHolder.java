package com.kneelawk.graphlib.net;

import com.kneelawk.graphlib.node.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a {@link BlockNodePacketEncoder} along with the class of its associated {@link BlockNode}.
 *
 * @param nodeClass   the class of the block node the encoder is associated with.
 * @param nodeEncoder the encoder for encoding the associated type of block node.
 * @param <N>         the type of the block node the encoder is associated with.
 */
public record BlockNodePacketEncoderHolder<N extends BlockNode>(@NotNull Class<N> nodeClass,
                                                                @NotNull BlockNodePacketEncoder<N> nodeEncoder) {
    public void toPacket(@NotNull BlockNode node, @NotNull Node<BlockNodeHolder> holderNode, @NotNull ServerWorld world,
                         @NotNull NodeView view, @NotNull PacketByteBuf buf) {
        if (nodeClass.isInstance(node)) {
            nodeEncoder.toPacket(nodeClass.cast(node), holderNode, world, view, buf);
        }
    }
}
