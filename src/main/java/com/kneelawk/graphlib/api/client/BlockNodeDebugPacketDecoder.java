package com.kneelawk.graphlib.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.debug.DebugBlockNode;

/**
 * Used for decoding a {@link DebugBlockNode} from a {@link PacketByteBuf}.
 */
public interface BlockNodeDebugPacketDecoder {
    /**
     * Decodes a {@link DebugBlockNode} from a {@link PacketByteBuf}.
     *
     * @param buf a buffer for reading the data written by
     *            {@link BlockNode#toDebugPacket(com.kneelawk.graphlib.api.graph.NodeHolder, PacketByteBuf)}.
     *            Note: this buffer will contain other data besides this node's data.
     * @return a {@link DebugBlockNode} containing the data decoded from the {@link PacketByteBuf}.
     */
    @Nullable DebugBlockNode fromPacket(@NotNull PacketByteBuf buf);
}
