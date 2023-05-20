package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.node.NodeKeyExtra;

/**
 * Generic {@link SidedBlockNode} unique data for use when implementing {@link BlockNode#getKeyExtra()}.
 *
 * @param typeId the type-id of the sided-block-node.
 * @param side   the side that the node is on.
 */
public record SimpleSidedNodeKeyExtra(Identifier typeId, Direction side) implements NodeKeyExtra {
    @Override
    public @NotNull Identifier getTypeId() {
        return typeId;
    }

    @Override
    public @NotNull NbtElement toTag() {
        return NbtByte.of((byte) side.getId());
    }
}
