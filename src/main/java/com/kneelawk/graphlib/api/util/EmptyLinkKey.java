package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.wire.LinkKeyFactory;
import com.kneelawk.graphlib.impl.Constants;

/**
 * An empty link key.
 * <p>
 * This is the default link key.
 */
public class EmptyLinkKey implements LinkKey {
    /**
     * The type of the empty link key.
     */
    public static final Identifier TYPE_ID = Constants.id("empty");

    /**
     * The empty link key is a singleton. Here is its instance.
     */
    public static final EmptyLinkKey INSTANCE = new EmptyLinkKey();

    /**
     * Decoder for the empty link key. Always returns this singleton's instance.
     */
    public static final LinkKeyDecoder DECODER = tag -> INSTANCE;

    /**
     * Always returns this singleton's link key instance.
     */
    public static final LinkKeyFactory FACTORY = (self, other) -> INSTANCE;

    private EmptyLinkKey() {}

    @Override
    public @NotNull Identifier getTypeId() {
        return TYPE_ID;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
