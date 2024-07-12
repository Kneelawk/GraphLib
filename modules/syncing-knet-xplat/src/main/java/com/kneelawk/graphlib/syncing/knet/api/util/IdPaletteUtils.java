/*
 * MIT License
 *
 * Copyright (c) 2024 Cyan Kneelawk.
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

package com.kneelawk.graphlib.syncing.knet.api.util;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.codextra.api.attach.stream.ChildBufferFactory;
import com.kneelawk.knet.api.util.NetBuf;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.Palette;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Contains GraphLib palettes used in syncing.
 */
public class IdPaletteUtils {
    private IdPaletteUtils() {}

    /**
     * Attachment key for a palette of {@link ResourceLocation}s.
     */
    public static final AttachmentKey<Palette<ResourceLocation>> ID_PALETTE = AttachmentKey.ofStaticFieldName();

    /**
     * {@link ResourceLocation} codec that can use an {@link #ID_PALETTE} attachment if present.
     */
    public static final StreamCodec<FriendlyByteBuf, ResourceLocation> PALETTED_ID_CODEC =
        ID_PALETTE.dispatchIfPresentStreamCodec(palette -> palette.asCodec("id palette"),
            ResourceLocation.STREAM_CODEC);

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding.
     * <p>
     * This provides the {@link IdPaletteUtils#ID_PALETTE} attachment.
     *
     * @param wrappedCodec    the codec to wrap.
     * @param childBufferCtor the constructor for the buffer type the wrapped codec uses.
     * @param <B1>            the type of the parent buffer.
     * @param <B2>            the type of the child buffer.
     * @param <V>             the result type.
     * @return the wrapper stream codec.
     */
    public static <B1 extends FriendlyByteBuf & NetBuf<B1>, B2 extends FriendlyByteBuf, V> StreamCodec<B1, V> attachPalette(
        StreamCodec<? super B2, V> wrappedCodec, ChildBufferFactory<? super B1, B2> childBufferCtor) {
        return ID_PALETTE.mutReadAttachingStreamCodec(Palette.codec(ResourceLocation.STREAM_CODEC), childBufferCtor,
            wrappedCodec, obj -> new Palette<>());
    }

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding, using a
     * buffer capable of being used as a {@link net.minecraft.network.RegistryFriendlyByteBuf}.
     * <p>
     * This provides the {@link IdPaletteUtils#ID_PALETTE} attachment.
     *
     * @param wrappedCodec the codec to wrap.
     * @param <V>          the result type.
     * @return the wrapper stream codec.
     */
    public static <V> StreamCodec<NetRegistryByteBuf, V> registryAttachPalette(
        StreamCodec<? super NetRegistryByteBuf, V> wrappedCodec) {
        return attachPalette(wrappedCodec, (cap, old) -> NetBufs.netRegistryBuf(cap, old.registryAccess()));
    }

    /**
     * Wraps the given {@link StreamCodec} codec in a palette that will be used in both encoding and decoding, using a
     * buffer capable of being used as a {@link NetByteBuf}.
     * <p>
     * This provides the {@link IdPaletteUtils#ID_PALETTE} attachment.
     *
     * @param wrappedCodec the codec to wrap.
     * @param <V>          the result type.
     * @return the wrapper stream codec.
     */
    public static <V> StreamCodec<NetRegistryByteBuf, V> netAttachPalette(
        StreamCodec<? super RegistryNetByteBuf, V> wrappedCodec) {
        return attachPalette(wrappedCodec, (cap, old) -> NetBufs.registryNetBuf(cap, old.registryAccess()));
    }
}
