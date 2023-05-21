package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.node.NodeKey;

/**
 * Generic {@link SidedBlockNode} key that can be used as an implementation of {@link NodeKey}.
 *
 * @param typeId the type-id of the sided-block-node.
 * @param side   the side that the node is on.
 */
public record SimpleSidedNodeKey(Identifier typeId, Direction side) implements NodeKey {
    @Override
    public @NotNull Identifier getTypeId() {
        return typeId;
    }

    @Override
    public @NotNull NbtElement toTag() {
        return NbtByte.of((byte) side.getId());
    }
}
