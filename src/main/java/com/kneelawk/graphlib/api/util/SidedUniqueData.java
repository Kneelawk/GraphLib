package com.kneelawk.graphlib.api.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.SidedBlockNode;

/**
 * Generic {@link SidedBlockNode} unique data for use when implementing {@link BlockNode#getUniqueData()}.
 *
 * @param typeId the type-id of the sided-block-node.
 * @param side   the side that the node is on.
 */
public record SidedUniqueData(Identifier typeId, Direction side) {
}
