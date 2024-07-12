/*
 * MIT License
 *
 * Copyright (c) 2023-2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.graphlib.syncing.knet.api.graph.user;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.codextra.api.CodextraStreams;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.syncing.api.util.ObjectSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.util.IdPaletteUtils;
import com.kneelawk.graphlib.syncing.knet.impl.StreamCodecHelper;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Holds a link key encoder and decoder.
 */
public final class LinkKeySyncing implements ObjectSyncing<LinkKeyType> {
    /**
     * {@link LinkKeySyncing} static codec.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link IdPaletteUtils#ID_PALETTE} attachment.
     */
    public static final StreamCodec<FriendlyByteBuf, LinkKeySyncing> REF_CODEC =
        StreamCodecHelper.createRefStreamCodec(GraphUniverse::getLinkKeyType, KNetSyncedUniverse::getLinkKeySyncing,
            LinkKeySyncing::getType, "LinkKey");

    /**
     * {@link LinkKeySyncing} codec getter.
     *
     * @param universe the universe containing the link keys to decode.
     * @return the codec associated with the given universe.
     */
    public static StreamCodec<FriendlyByteBuf, LinkKeySyncing> refCodec(KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, REF_CODEC);
    }

    private final @NotNull LinkKeyType type;
    private final @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkKey> codec;

    private LinkKeySyncing(@NotNull LinkKeyType type,
                           @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkKey> codec) {
        this.type = type;
        this.codec = codec;
    }

    /**
     * {@return this syncing descriptor's type}
     */
    @Override
    public @NotNull LinkKeyType getType() {
        return type;
    }

    /**
     * {@return this syncing descriptor's stream codec}
     */
    public @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkKey> getCodec() {
        return codec;
    }

    /**
     * Makes a {@link LinkKey} syncing descriptor.
     *
     * @param type  the link key type this syncing is associated with.
     * @param codec the link key's stream codec.
     * @return a link key syncing descriptor.
     */
    public static @NotNull LinkKeySyncing ofRegistry(@NotNull LinkKeyType type, @NotNull
    StreamCodec<? super NetRegistryByteBuf, ? extends LinkKey> codec) {
        return new LinkKeySyncing(type, codec);
    }

    /**
     * Makes a {@link LinkKey} syncing descriptor.
     *
     * @param type  the link key type this syncing is associated with.
     * @param codec the link key's stream codec.
     * @return a link key syncing descriptor.
     */
    public static @NotNull LinkKeySyncing ofNet(@NotNull LinkKeyType type, @NotNull
    StreamCodec<? super RegistryNetByteBuf, ? extends LinkKey> codec) {
        return new LinkKeySyncing(type, codec.mapStream(NetBufs::registryNetOf));
    }

    /**
     * Makes a {@link LinkKey} syncing descriptor that does not do any encoding or decoding.
     *
     * @param type     the link key type this syncing is associated with.
     * @param supplier supplies the instance(s) of the link key.
     * @return a link key syncing descriptor.
     */
    public static @NotNull LinkKeySyncing ofNoOp(@NotNull LinkKeyType type,
                                                 @NotNull Supplier<? extends LinkKey> supplier) {
        return new LinkKeySyncing(type, CodextraStreams.unit(supplier));
    }
}
