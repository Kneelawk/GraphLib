package com.kneelawk.graphlib.api.node.client;

import com.kneelawk.graphlib.api.client.BlockNodeRenderer;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;
import com.kneelawk.graphlib.api.graph.struct.Node;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Describes a {@link ClientBlockNode} that is positioned on the side of a block.
 */
public interface SidedClientBlockNode extends ClientBlockNode {
    /**
     * The side of the block this node is positioned at.
     * <p>
     * The value returned here corresponds to how nodes are grouped together during rendering and influences the
     * <code>nodeCount</code> argument passed to
     * {@link BlockNodeRenderer#getLineEndpoint(ClientBlockNode, Node, ClientBlockGraph, int, int, List)}.
     * <p>
     * A wire is determined to be on the {@link Direction#DOWN} side if it is sitting in the bottom of its block-space,
     * on the top side of the block beneath it. A wire is determined to be on the {@link Direction#NORTH} side if it is
     * sitting at the north side of its block-space, on the south side of the block to the north of it. This same logic
     * applies for all directions.
     *
     * @return the side of the block this node is positioned at.
     */
    @NotNull Direction getSide();
}
