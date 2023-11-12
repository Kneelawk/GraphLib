package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyPacketDecoder;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
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
     * Packet decoder for the empty link key. Always returns this singleton's instance.
     */
    public static final LinkKeyPacketDecoder PACKET_DECODER = (buf, ctx) -> INSTANCE;

    /**
     * Always returns this singleton's link key instance.
     */
    public static final LinkKeyFactory FACTORY = (self, other) -> INSTANCE;

    /**
     * The link key type for the empty link key.
     */
    public static final LinkKeyType TYPE = LinkKeyType.of(TYPE_ID, DECODER, PACKET_DECODER);

    private EmptyLinkKey() {}

    @Override
    public @NotNull LinkKeyType getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }
}
