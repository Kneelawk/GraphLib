package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.NodeKey;

/**
 * Generic {@link BlockNode} key that can be used as an implementation of {@link NodeKey}.
 *
 * @param typeId the type id of the block node this is associated with.
 */
public record SimpleNodeKey(Identifier typeId) implements NodeKey {
    @Override
    public @NotNull Identifier getTypeId() {
        return typeId;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
