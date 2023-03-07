package com.kneelawk.graphlib.api.v1.net;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.v1.graph.GraphView;
import com.kneelawk.graphlib.api.v1.graph.NodeHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.util.graph.Node;

/**
 * Holds a {@link BlockNodePacketEncoder} along with the class of its associated {@link BlockNode}.
 *
 * @param nodeClass   the class of the block node the encoder is associated with.
 * @param nodeEncoder the encoder for encoding the associated type of block node.
 * @param <N>         the type of the block node the encoder is associated with.
 */
public record BlockNodePacketEncoderHolder<N extends BlockNode>(@NotNull Class<N> nodeClass,
                                                                @NotNull BlockNodePacketEncoder<N> nodeEncoder) {
    public void toPacket(@NotNull BlockNode node, @NotNull Node<NodeHolder> holderNode, @NotNull ServerWorld world,
                         @NotNull GraphView view, @NotNull PacketByteBuf buf) {
        if (nodeClass.isInstance(node)) {
            nodeEncoder.toPacket(nodeClass.cast(node), holderNode, world, view, buf);
        }
    }
}
