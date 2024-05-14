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
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntityType;
import com.kneelawk.graphlib.syncing.knet.api.GraphLibSyncingKNet;
import com.kneelawk.graphlib.syncing.knet.api.graph.KNetSyncedUniverse;
import com.kneelawk.graphlib.syncing.knet.impl.StreamCodecHelper;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Holds a node entity encoder and decoder.
 */
public final class NodeEntitySyncing {
    /**
     * {@link NodeEntitySyncing} static codec.
     * <p>
     * <b>This requires the {@link KNetSyncedUniverse#ATTACHMENT_KEY} attachment.</b>
     * This can optionally make use of the {@link GraphLibSyncingKNet#ID_PALETTE_ATTACHMENT} attachment.
     */
    public static final StreamCodec<FriendlyByteBuf, NodeEntitySyncing> REF_CODEC =
        StreamCodecHelper.createRefStreamCodec(GraphUniverse::getNodeEntityType,
            KNetSyncedUniverse::getNodeEntitySyncing, NodeEntitySyncing::getType, "NodeEntity");

    /**
     * {@link NodeEntitySyncing} codec getter.
     *
     * @param universe the universe containing the node entities to decode.
     * @return the codec associated with the given universe.
     */
    public static StreamCodec<FriendlyByteBuf, NodeEntitySyncing> refCodec(KNetSyncedUniverse universe) {
        return KNetSyncedUniverse.ATTACHMENT_KEY.attachingStreamCodec(universe, REF_CODEC);
    }

    private final @NotNull NodeEntityType type;
    private final @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends NodeEntity> codec;

    private NodeEntitySyncing(@NotNull NodeEntityType type,
                              @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends NodeEntity> codec) {
        this.type = type;
        this.codec = codec;
    }

    /**
     * {@return this syncing descriptor's type}
     */
    public @NotNull NodeEntityType getType() {
        return type;
    }

    /**
     * {@return this syncing descriptor's stream codec}
     */
    public @NotNull StreamCodec<? super NetRegistryByteBuf, ? extends NodeEntity> getCodec() {
        return codec;
    }

    /**
     * Makes a {@link NodeEntity} syncing descriptor.
     *
     * @param type  the node entity type this syncing is associated with.
     * @param codec the node entity's stream codec.
     * @return a new node entity syncing descriptor.
     */
    public static @NotNull NodeEntitySyncing ofRegistry(@NotNull NodeEntityType type, @NotNull
    StreamCodec<? super NetRegistryByteBuf, ? extends NodeEntity> codec) {
        return new NodeEntitySyncing(type, codec);
    }

    /**
     * Makes a {@link NodeEntity} syncing descriptor.
     *
     * @param type  the node entity type this syncing is associated with.
     * @param codec the node entity's stream codec.
     * @return a new node entity syncing descriptor.
     */
    public static @NotNull NodeEntitySyncing ofNet(@NotNull NodeEntityType type, @NotNull
    StreamCodec<? super RegistryNetByteBuf, ? extends NodeEntity> codec) {
        return new NodeEntitySyncing(type, codec.mapStream(NetBufs::registryNetOf));
    }

    /**
     * Makes a {@link NodeEntity} syncing descriptor that does no encoding or decoding.
     *
     * @param type     the node entity type this syncing is associated with.
     * @param supplier supplies new instances of node entities.
     * @return a new node entity syncing descriptor.
     */
    public static @NotNull NodeEntitySyncing ofNoOp(@NotNull NodeEntityType type,
                                                    @NotNull Supplier<? extends NodeEntity> supplier) {
        return new NodeEntitySyncing(type, CodextraStreams.unit(supplier));
    }
}
