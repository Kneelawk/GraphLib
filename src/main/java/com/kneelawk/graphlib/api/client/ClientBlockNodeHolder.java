package com.kneelawk.graphlib.api.client;

import com.kneelawk.graphlib.api.node.client.ClientBlockNode;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Holds a {@link ClientBlockNode} along with its {@link BlockPos}.
 *
 * @param pos     the block position of the node.
 * @param node    the node itself.
 * @param graphId the id of the graph this node belongs to.
 */
public record ClientBlockNodeHolder(@NotNull BlockPos pos, @NotNull ClientBlockNode node, long graphId) {
}
