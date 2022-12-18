package com.kneelawk.graphlib.net;

import com.kneelawk.graphlib.graph.BlockGraphController;
import com.kneelawk.graphlib.graph.struct.Graph;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Runs server side to decode a packet sent by
 * {@link com.kneelawk.graphlib.graph.ClientBlockNode#toInspectionPacket(Node, Graph, PacketByteBuf)}, look up the
 * requested node, and send information back to be decoded by
 * {@link BlockNodeInspectionPacketDecoder#decodeInspectedData(PacketByteBuf)}.
 */
public interface BlockNodeInspectionPacketHandler {
    /**
     * Handles an incoming inspection request packet, looking up a node from the packet and writing the node's relevant
     * data to the out-going packet.
     *
     * @param world      the world the node is in.
     * @param controller the block graph controller the node is in.
     * @param nodePos    the node's block position.
     * @param inBuf      the received buffer describing the node to look up.
     * @param outBuf     the buffer to write relevant node information to.
     */
    void inspect(@NotNull ServerWorld world, @NotNull BlockGraphController controller, @NotNull BlockPos nodePos,
                 @NotNull PacketByteBuf inBuf, @NotNull PacketByteBuf outBuf);
}
