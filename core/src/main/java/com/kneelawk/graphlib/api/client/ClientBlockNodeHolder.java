package com.kneelawk.graphlib.api.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.user.debug.DebugBlockNode;

/**
 * Holds a {@link DebugBlockNode} along with its {@link BlockPos}.
 *
 * @param pos     the block position of the node.
 * @param node    the node itself.
 * @param graphId the id of the graph this node belongs to.
 */
public record ClientBlockNodeHolder(@NotNull BlockPos pos, @NotNull DebugBlockNode node, long graphId) {
}
