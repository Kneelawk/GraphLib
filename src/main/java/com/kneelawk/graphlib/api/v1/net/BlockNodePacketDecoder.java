package com.kneelawk.graphlib.api.v1.net;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;

import com.kneelawk.graphlib.api.v1.graph.GraphView;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.v1.util.graph.Node;

/**
 * Used for decoding a {@link ClientBlockNode} from a {@link PacketByteBuf}.
 */
public interface BlockNodePacketDecoder {
    /**
     * Decodes a {@link ClientBlockNode} from a {@link PacketByteBuf}.
     *
     * @param buf a buffer for reading the data written by
     *            {@link BlockNodePacketEncoder#toPacket(BlockNode, Node, ServerWorld, GraphView, PacketByteBuf)}.
     *            Note: this buffer will contain other data besides this node's data.
     * @return a {@link ClientBlockNode} containing the data decoded from the {@link PacketByteBuf}.
     */
    @Nullable ClientBlockNode fromPacket(@NotNull PacketByteBuf buf);
}
