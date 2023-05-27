package com.kneelawk.graphlib.api.graph.user.client;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.client.render.BlockNodeRenderer;
import com.kneelawk.graphlib.api.util.graph.Node;
import com.kneelawk.graphlib.impl.client.graph.ClientBlockGraph;

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
