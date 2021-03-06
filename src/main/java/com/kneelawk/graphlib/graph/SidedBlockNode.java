package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

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
     * {@link SimpleBlockGraphController#getNodesAt(SidedPos)}, depending on the side given in the sided block-position. The
     * side returned here also influences the {@link com.kneelawk.graphlib.wire.WireConnectionDiscoverers} connection
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
}
