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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.codextra.api.CodextraStreams;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.syncing.api.util.ObjectSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.api.util.IdPaletteUtils;
import com.kneelawk.graphlib.syncing.knet.impl.StreamCodecHelper;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Holds a graph entity encoder and decoder.
 *
 * @param <G> the type of graph entity this syncs.
 */
public final class GraphEntitySyncing<G extends GraphEntity<G>> implements ObjectSyncing<GraphEntityType<G>> {
    /**
     * {@link GraphEntitySyncing} static codec.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link IdPaletteUtils#ID_PALETTE} attachment.
     */
    public static final StreamCodec<FriendlyByteBuf, GraphEntitySyncing<?>> REF_CODEC =
        StreamCodecHelper.createRefStreamCodec(GraphUniverse::getGraphEntityType,
            (universe, type) -> universe.getGraphEntitySyncing(type), GraphEntitySyncing::getType, "GraphEntity");

    /**
     * {@link GraphEntitySyncing} codec caster.
     *
     * @param <G> the type of graph entity this is related to.
     * @return {@link #REF_CODEC} cast to the desired graph entity type.
     */
    @SuppressWarnings("unchecked")
    public static <G extends GraphEntity<G>> StreamCodec<FriendlyByteBuf, GraphEntitySyncing<G>> refCodec() {
        return (StreamCodec<FriendlyByteBuf, GraphEntitySyncing<G>>) (Object) REF_CODEC;
    }

    /**
     * {@link GraphEntitySyncing} codec getter.
     *
     * @param universe the universe to attach to the codec.
     * @param <G>      the type of graph entity this is related to.
     * @return the codec with the given universe attached.
     */
    public static <G extends GraphEntity<G>> StreamCodec<FriendlyByteBuf, GraphEntitySyncing<G>> refCodec(
        KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, refCodec());
    }

    private final @NotNull GraphEntityType<G> type;
    private final @NotNull StreamCodec<? super NetRegistryByteBuf, G> codec;

    private GraphEntitySyncing(@NotNull GraphEntityType<G> type,
                               @NotNull StreamCodec<? super NetRegistryByteBuf, G> codec) {
        this.type = type;
        this.codec = codec;
    }

    /**
     * {@return this syncing descriptor's type}
     */
    @Override
    public @NotNull GraphEntityType<G> getType() {
        return type;
    }

    /**
     * {@return this syncing descriptor's stream codec}
     */
    public @NotNull StreamCodec<? super NetRegistryByteBuf, G> getCodec() {
        return codec;
    }

    /**
     * Makes a new {@link GraphEntity} syncing descriptor.
     *
     * @param <G>   the type of graph entity this descriptor syncs.
     * @param type  the graph entity type this syncing is associated with.
     * @param codec graph entity's stream codec.
     * @return a new graph entity syncing descriptor.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> ofRegistry(@NotNull GraphEntityType<G> type,
                                                                                       @NotNull
                                                                                       StreamCodec<? super NetRegistryByteBuf, G> codec) {
        return new GraphEntitySyncing<>(type, codec);
    }

    /**
     * Makes a new {@link GraphEntity} syncing descriptor.
     *
     * @param <G>   the type of graph entity this descriptor syncs.
     * @param type  the graph entity type this syncing is associated with.
     * @param codec graph entity's stream codec.
     * @return a new graph entity syncing descriptor.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> ofNet(@NotNull GraphEntityType<G> type,
                                                                                  @NotNull
                                                                                  StreamCodec<? super RegistryNetByteBuf, G> codec) {
        return new GraphEntitySyncing<>(type, codec.mapStream(NetBufs::registryNetOf));
    }

    /**
     * Makes a new {@link GraphEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param <G>      the type of graph entity this descriptor syncs.
     * @param type     the graph entity type this syncing is associated with.
     * @param supplier supplies instances of the graph entity.
     * @return a new graph entity syncing descriptor.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntitySyncing<G> ofNoOp(@NotNull GraphEntityType<G> type,
                                                                                   @NotNull Supplier<G> supplier) {
        return new GraphEntitySyncing<G>(type, CodextraStreams.unit(supplier));
    }
}
