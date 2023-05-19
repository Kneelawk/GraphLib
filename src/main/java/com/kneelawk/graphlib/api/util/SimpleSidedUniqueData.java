package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.node.UniqueData;

/**
 * Generic {@link SidedBlockNode} unique data for use when implementing {@link BlockNode#getUniqueData()}.
 *
 * @param typeId the type-id of the sided-block-node.
 * @param side   the side that the node is on.
 */
public record SimpleSidedUniqueData(Identifier typeId, Direction side) implements UniqueData {
    @Override
    public @NotNull Identifier getTypeId() {
        return typeId;
    }

    @Override
    public @NotNull NbtElement toTag() {
        return NbtByte.of((byte) side.getId());
    }
}
