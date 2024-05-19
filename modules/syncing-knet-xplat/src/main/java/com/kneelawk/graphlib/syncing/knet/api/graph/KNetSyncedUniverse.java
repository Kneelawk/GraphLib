/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.graphlib.syncing.knet.api.graph;

import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.netty.handler.codec.DecoderException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.syncing.api.GraphLibSyncing;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.syncing.knet.api.SyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.BlockNodeSyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.GraphEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.LinkKeySyncing;
import com.kneelawk.graphlib.syncing.knet.api.graph.user.NodeEntitySyncing;
import com.kneelawk.graphlib.syncing.knet.impl.graph.simple.SimpleKNetSyncedUniverseBuilder;

/**
 * KNet-based universe synchronization.
 */
public interface KNetSyncedUniverse extends SyncedUniverse {
    /**
     * Attachment key for the current synced universe.
     */
    AttachmentKey<KNetSyncedUniverse> ATTACHMENT_KEY = AttachmentKey.ofStaticFieldName();

    /**
     * Codec for referencing a specific {@link KNetSyncedUniverse}.
     */
    StreamCodec<FriendlyByteBuf, KNetSyncedUniverse> REF_CODEC = SyncingKNet.PALETTED_ID_CODEC.map(id -> {
        if (!GraphLibSyncing.syncingEnabled(id))
            throw new DecoderException("There is no synced universe called '" + id + "'");
        return SyncingKNet.getUniverse(id);
    }, KNetSyncedUniverse::getId);

    /**
     * Creates a {@link StreamCodec} that wraps the given codec and attaches a universe read from the stream.
     * <p>
     * This provides both the {@link #ATTACHMENT_KEY} and {@link GraphUniverse#ATTACHMENT_KEY} attachments.
     *
     * @param wrappedCodec the codec to wrap and provide the universe attachment to.
     * @param getter       the function to get the universe from the wrapped type.
     * @param <B>          the buffer type.
     * @param <V>          the result type.
     * @return the created stream codec.
     */
    static <B extends FriendlyByteBuf, V> StreamCodec<B, V> readAttachingCodec(StreamCodec<? super B, V> wrappedCodec,
                                                                               Function<? super V, ? extends KNetSyncedUniverse> getter) {
        return AttachmentKey.readAttachingStreamCodec(REF_CODEC,
            universe -> Map.of(ATTACHMENT_KEY, universe, GraphUniverse.ATTACHMENT_KEY, universe.getUniverse()),
            wrappedCodec, getter);
    }

    /**
     * Creates a {@link StreamCodec.CodecOperation} that attaches a universe read from the stream.
     * <p>
     * This provides both the {@link #ATTACHMENT_KEY} and {@link GraphUniverse#ATTACHMENT_KEY} attachments.
     *
     * @param getter the function to get the universe from the wrapped type.
     * @param <B>    the buffer type.
     * @param <V>    the result type.
     * @return the created codec operation.
     */
    static <B extends FriendlyByteBuf, V> StreamCodec.CodecOperation<B, V, V> readAttachingOp(
        Function<? super V, ? extends KNetSyncedUniverse> getter) {
        return streamCodec -> readAttachingCodec(streamCodec, getter);
    }

    /**
     * Registers an encoder and decoder for the given block node type.
     *
     * @param syncing the syncing for the block node.
     */
    void addNodeSyncing(@NotNull BlockNodeSyncing syncing);

    /**
     * Gets the encoder and decoder for the given block node type.
     *
     * @param type the type of block node to get the syncing for.
     * @return the syncing for the given block node type.
     */
    @Nullable
    BlockNodeSyncing getNodeSyncing(@NotNull BlockNodeType type);

    /**
     * Registers an encoder and decoder for the given node entity type.
     *
     * @param syncing the node entity syncing.
     */
    void addNodeEntitySyncing(@NotNull NodeEntitySyncing syncing);

    /**
     * Gets the encoder and decoder for the given node entity type.
     *
     * @param type the type of node entity to get the syncing for.
     * @return the syncing for the given node entity type.
     */
    @Nullable
    NodeEntitySyncing getNodeEntitySyncing(@NotNull NodeEntityType type);

    /**
     * Registers an encoder and decoder for the given link key type.
     *
     * @param syncing the link key syncing.
     */
    void addLinkKeySyncing(@NotNull LinkKeySyncing syncing);

    /**
     * Gets the encoder and decoder for the given link key type.
     *
     * @param type the type of link key to get the syncing for.
     * @return the syncing for the given link key type.
     */
    @Nullable
    LinkKeySyncing getLinkKeySyncing(@NotNull LinkKeyType type);

    /**
     * Registers an encoder and decoder for the given link entity type.
     *
     * @param syncing the link entity syncing.
     */
    void addLinkEntitySyncing(@NotNull LinkEntitySyncing syncing);

    /**
     * Gets the encoder and decoder for the given link entity type.
     *
     * @param type the type of link entity to get the syncing for.
     * @return the syncing for the given link entity type.
     */
    @Nullable
    LinkEntitySyncing getLinkEntitySyncing(@NotNull LinkEntityType type);

    /**
     * Registers an encoder and decoder for the given graph entity type.
     *
     * @param <G>     the type of graph entity to add syncing for.
     * @param syncing the graph entity syncing.
     */
    <G extends GraphEntity<G>> void addGraphEntitySyncing(@NotNull GraphEntitySyncing<G> syncing);

    /**
     * Gets the encoder and decoder for the given graph entity type.
     *
     * @param type the type of graph entity to get the syncing for.
     * @param <G>  the type of graph entity to get the syncing for.
     * @return the syncing for the given graph entity type.
     */
    <G extends GraphEntity<G>> @Nullable GraphEntitySyncing<G> getGraphEntitySyncing(@NotNull GraphEntityType<G> type);

    /**
     * Creates a new SyncedUniverse builder.
     *
     * @return a new builder for building a SyncedUniverse.
     */
    @Contract(value = "-> new", pure = true)
    static @NotNull Builder builder() {
        return new SimpleKNetSyncedUniverseBuilder();
    }

    /**
     * A builder for {@link SyncedUniverse}s.
     */
    interface Builder {
        /**
         * Builds a universe synchronization handler for the given universe.
         * <p>
         * <b>Note: This does not register synchronization handlers. Registration should be performed with the
         * {@link SyncedUniverse#register()} method.</b>
         *
         * @param universe the universe that this synchronization handler is to synchronize.
         * @return a new universe synchronization handler.
         */
        @NotNull
        KNetSyncedUniverse build(@NotNull GraphUniverse universe);

        /**
         * Sets whether this graph universe should be synchronized to the client.
         * <p>
         * This is set to {@link SyncProfile#SYNC_EVERYTHING} by default.
         * <p>
         * The {@link CacheCategory} in the given sync profile will be automatically registered on universe creation.
         *
         * @param profile a profile describing whether and how this graph universe should be synchronized to the client.
         * @return this builder for call chaining.
         */
        @NotNull
        Builder synchronizeToClient(@NotNull SyncProfile profile);
    }
}
