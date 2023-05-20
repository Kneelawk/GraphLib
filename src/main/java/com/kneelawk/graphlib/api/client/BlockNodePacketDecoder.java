package com.kneelawk.graphlib.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.client.ClientBlockNode;

/**
 * Used for decoding a {@link ClientBlockNode} from a {@link PacketByteBuf}.
 */
public interface BlockNodePacketDecoder {
    /**
     * Decodes a {@link ClientBlockNode} from a {@link PacketByteBuf}.
     *
     * @param buf a buffer for reading the data written by
     *            {@link BlockNode#toPacket(PacketByteBuf)}.
     *            Note: this buffer will contain other data besides this node's data.
     * @return a {@link ClientBlockNode} containing the data decoded from the {@link PacketByteBuf}.
     */
    @Nullable ClientBlockNode fromPacket(@NotNull PacketByteBuf buf);
}
