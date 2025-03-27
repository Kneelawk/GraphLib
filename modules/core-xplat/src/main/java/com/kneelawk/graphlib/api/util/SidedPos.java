package com.kneelawk.graphlib.api.util;

import java.util.Optional;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Represents both a block-position and the side of that block-position.
 *
 * @param pos  the block-position.
 * @param side the side of the block-position.
 */
public record SidedPos(@NotNull BlockPos pos, @NotNull Direction side) {

    /**
     * Constructs a SidedPos, making sure to convert to an immutable block position.
     *
     * @param pos  the block-position.
     * @param side the side of the block-position.
     */
    public SidedPos(@NotNull BlockPos pos, @NotNull Direction side) {
        this.pos = pos.immutable();
        this.side = side;
    }

    /**
     * Encodes this SidedPos to an NBT compound.
     * <p>
     * This writes to the {@code pos} and {@code side} elements within the given NBT compound.
     *
     * @param nbt the NBT compound to write to.
     * @see #toNbt()
     * @see #fromNbt(CompoundTag)
     */
    public void toNbt(@NotNull CompoundTag nbt) {
        nbt.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        nbt.putByte("side", (byte) side.get3DDataValue());
    }

    /**
     * Encodes this SidedPos to an NBT compound and returns it.
     *
     * @return the encoded NBT compound.
     * @see #toNbt(CompoundTag)
     * @see #fromNbt(CompoundTag)
     */
    public @NotNull CompoundTag toNbt() {
        CompoundTag nbt = new CompoundTag();
        toNbt(nbt);
        return nbt;
    }

    /**
     * Writes this SidedPos to the given PacketByteBuf.
     *
     * @param buf the buffer to write to.
     * @see #fromPacket(FriendlyByteBuf)
     */
    public void toPacket(@NotNull FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(side.get3DDataValue());
    }

    /**
     * Reads a SidedPos from the given NBT compound.
     *
     * @param nbt the NBT compound to read from.
     * @return a new SidedPos with the data read from the given NBT compound.
     * @see #toNbt(CompoundTag)
     * @see #toNbt()
     */
    @Contract("_ -> new")
    public static @NotNull SidedPos fromNbt(@NotNull CompoundTag nbt) {
        Optional<int[]> posOpt = nbt.getIntArray("pos");
        if (posOpt.isEmpty())
            throw new IllegalArgumentException("Attempted to decode a SidedPos that has no block-pos");

        int[] pos = posOpt.get();
        return new SidedPos(new BlockPos(pos[0], pos[1], pos[2]),
            Direction.from3DDataValue(nbt.getByteOr("side", (byte) 0)));
    }

    /**
     * Reads a SidedPos from the given PacketByteBuf.
     *
     * @param buf the buffer to read from.
     * @return a new SidedPos with the data read from the given buffer.
     * @see #toPacket(FriendlyByteBuf)
     */
    @Contract("_ -> new")
    public static @NotNull SidedPos fromPacket(@NotNull FriendlyByteBuf buf) {
        return new SidedPos(buf.readBlockPos(), Direction.from3DDataValue(buf.readByte()));
    }

    /**
     * Codec for encoding and decoding SidedPoses with a matching structure as produced and consumed by
     * {@link #toNbt(CompoundTag)} and {@link #fromNbt(CompoundTag)}.
     *
     * @see #toNbt(CompoundTag)
     * @see #toNbt()
     * @see #fromNbt(CompoundTag)
     */
    public static final Codec<SidedPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(SidedPos::pos),
        DirectionUtils.BYTE_CODEC.fieldOf("side").forGetter(SidedPos::side)
    ).apply(instance, SidedPos::new));

    /**
     * Stream codec for encoding and decoding {@link SidedPos}es with matching structure as produced and consumed by
     * {@link #toPacket(FriendlyByteBuf)} and {@link #fromPacket(FriendlyByteBuf)}.
     *
     * @see #toPacket(FriendlyByteBuf)
     * @see #fromPacket(FriendlyByteBuf)
     */
    public static final StreamCodec<FriendlyByteBuf, SidedPos> STREAM_CODEC =
        StreamCodec.ofMember(SidedPos::toPacket, SidedPos::fromPacket);
}
