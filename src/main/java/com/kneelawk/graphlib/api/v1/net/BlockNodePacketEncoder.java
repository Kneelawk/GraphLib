package com.kneelawk.graphlib.api.v1.net;

import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.graph.NodeHolder;
import com.kneelawk.graphlib.api.v1.graph.GraphView;
import com.kneelawk.graphlib.api.v1.util.graph.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

/**
 * Used for encoding a {@link BlockNode} to a {@link PacketByteBuf} for sending to the client for debug rendering.
 *
 * @param <N> the exact type of {@link BlockNode} this is designed to encode.
 */
public interface BlockNodePacketEncoder<N extends BlockNode> {
    /**
     * Encodes a {@link BlockNode} to a {@link PacketByteBuf}.
     *
     * Note the given buffer will have {@link BlockNode}s encoded before and after this one.
     * @param node the block node to encode.
     * @param holderNode
     * @param world
     * @param view
     * @param buf
     */
    void toPacket(@NotNull N node, @NotNull Node<NodeHolder> holderNode, @NotNull ServerWorld world,
                  @NotNull GraphView view, @NotNull PacketByteBuf buf);
}