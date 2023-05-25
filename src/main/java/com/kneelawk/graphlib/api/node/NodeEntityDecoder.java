package com.kneelawk.graphlib.api.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.NodeEntityContext;

/**
 * Decoder for {@link NodeEntity}s.
 */
public interface NodeEntityDecoder {
    /**
     * Decodes a {@link NodeEntity} from the given tag.
     * <p>
     * The NBT element given here should be exactly the same as the one returned by {@link NodeEntity#toTag()}.
     *
     * @param tag the NBT element used to decode this node entity.
     * @param ctx the node entity context for the decoded node entity.
     * @return a newly decoded node entity.
     */
    @Nullable NodeEntity decode(@Nullable NbtElement tag, @NotNull NodeEntityContext ctx);
}
