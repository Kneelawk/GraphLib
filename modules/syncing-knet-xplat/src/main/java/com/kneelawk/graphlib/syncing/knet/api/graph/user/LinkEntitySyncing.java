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
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.impl.StreamCodecHelper;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

/**
 * Holds a link entity encoder and decoder.
 */
public final class LinkEntitySyncing {
    /**
     * {@link LinkEntitySyncing} static codec.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE_ATTACHMENT} attachment.
     */
    public static final StreamCodec<FriendlyByteBuf, LinkEntitySyncing> REF_CODEC =
        StreamCodecHelper.createRefStreamCodec(GraphUniverse::getLinkEntityType,
            KNetSyncedUniverse::getLinkEntitySyncing, LinkEntitySyncing::getType, "LinkEntity");

    /**
     * {@link LinkEntitySyncing} codec getter.
     *
     * @param universe the universe containing the link entities to decode.
     * @return the codec associated with the given universe.
     */
    public static StreamCodec<FriendlyByteBuf, LinkEntitySyncing> refCodec(KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, REF_CODEC);
    }

    private final @NotNull LinkEntityType type;
    private final @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkEntity> codec;

    private LinkEntitySyncing(@NotNull LinkEntityType type,
                              @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkEntity> codec) {
        this.type = type;
        this.codec = codec;
    }

    /**
     * {@return this syncing descriptor's type}
     */
    public @NotNull LinkEntityType getType() {
        return type;
    }

    /**
     * {@return this syncing descriptor's stream codec}
     */
    public @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends LinkEntity> getCodec() {
        return codec;
    }

    /**
     * Makes a {@link LinkEntity} syncing descriptor.
     *
     * @param type  the link entity type this syncing is associated with.
     * @param codec the link entity's stream codec.
     * @return a new link entity syncing descriptor.
     */
    public static @NotNull LinkEntitySyncing of(@NotNull LinkEntityType type, @NotNull
    StreamCodec<? super NetRegistryByteBuf, ? extends LinkEntity> codec) {
        return new LinkEntitySyncing(type, codec);
    }

    /**
     * Makes a {@link LinkEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param type     the link entity type this syncing is associated with.
     * @param supplier supplies new instances of the link entity.
     * @return a new link entity syncing descriptor.
     */
    public static @NotNull LinkEntitySyncing ofNoOp(@NotNull LinkEntityType type,
                                                    @NotNull Supplier<? extends LinkEntity> supplier) {
        return new LinkEntitySyncing(type, CodextraStreams.unit(supplier));
    }
}
