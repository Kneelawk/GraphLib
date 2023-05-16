package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.UniqueData;

/**
 * Generic {@link BlockNode} unique data for use when implementing {@link BlockNode#getUniqueData()}.
 *
 * @param typeId the type id of the block node this is associated with.
 */
public record SimpleUniqueData(Identifier typeId) implements UniqueData {
    @Override
    public @NotNull Identifier getTypeId() {
        return typeId;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
