package com.kneelawk.graphlib.net;

import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used for decoding a {@link ClientBlockNode} from a {@link PacketByteBuf}.
 */
public interface BlockNodePacketDecoder {
    /**
     * Decodes a {@link ClientBlockNode} from a {@link PacketByteBuf}.
     *
     * @param buf a buffer for reading the data written by
     *            {@link com.kneelawk.graphlib.net.BlockNodePacketEncoder#toPacket(BlockNode, Node, ServerWorld, NodeView, PacketByteBuf)}.
     *            Note: this buffer will contain other data besides this node's data.
     * @return a {@link ClientBlockNode} containing the data decoded from the {@link PacketByteBuf}.
     */
    @Nullable ClientBlockNode fromPacket(@NotNull PacketByteBuf buf);
}
