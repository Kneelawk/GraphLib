package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.util.SidedPos;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.api.wire.WireConnectionDiscoverers;
import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphWorld;

/**
 * Describes a block node that is positioned on the side of a block.
 * <p>
 * An example of a block node that is positioned on the side of a block would be a wire or gate from Wired Redstone.
 */
public interface SidedBlockNode extends BlockNode {
    /**
     * The side of the block this node is positioned at.
     * <p>
     * The value returned here corresponds to what nodes are returned by
     * {@link SimpleGraphWorld#getNodesAt(SidedPos)}, depending on the side given in the sided block-position. The
     * side returned here also influences the {@link WireConnectionDiscoverers} connection
     * logic.
     * <p>
     * A wire is determined to be on the {@link Direction#DOWN} side if it is sitting in the bottom of its block-space,
     * on the top side of the block beneath it. A wire is determined to be on the {@link Direction#NORTH} side if it is
     * sitting at the north side of its block-space, on the south side of the block to the north of it. This same logic
     * applies for all directions.
     *
     * @return the side of the block this node is positioned at.
     */
    @NotNull Direction getSide();

    /**
     * Encodes this block node to a {@link PacketByteBuf} to be sent to the client for client-side graph debug
     * rendering.
     * <p>
     * The default implementations of this method are compatible with the default client-side block node decoders.
     * This method does <b>not</b> need to be implemented in order for client-side graph debug rendering to work.
     * This method should only be overridden to provide custom data to the client.
     *
     * @param world     the block world that this node is associated with.
     * @param graphView the world of nodes.
     * @param pos       the block position of this node.
     * @param self      this block node's holder, providing information about this node's connections and graph id.
     * @param buf       the buffer to encode this node to.
     */
    @Override
    default void toPacket(@NotNull ServerWorld world, @NotNull GraphView graphView, @NotNull BlockPos pos,
                          @NotNull Node<NodeHolder> self, @NotNull PacketByteBuf buf) {
        // This keeps otherwise identical-looking client-side nodes separate.
        buf.writeInt(hashCode());

        // Class name hash for use in default node coloring
        buf.writeInt(getClass().getName().hashCode());

        // A 1 byte to distinguish ourselves from BlockNode, because both implementations use the same decoder
        buf.writeByte(1);

        // Our side
        buf.writeByte(getSide().getId());
    }
}
