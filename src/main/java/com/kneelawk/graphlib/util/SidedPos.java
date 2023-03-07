package com.kneelawk.graphlib.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

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
        this.pos = pos.toImmutable();
        this.side = side;
    }

    /**
     * Encodes this SidedPos to an NBT compound.
     * <p>
     * This writes to the {@code pos} and {@code side} elements within the given NBT compound.
     *
     * @param nbt the NBT compound to write to.
     * @see #toNbt()
     * @see #fromNbt(NbtCompound)
     */
    public void toNbt(@NotNull NbtCompound nbt) {
        nbt.putIntArray("pos", new int[]{pos.getX(), pos.getY(), pos.getZ()});
        nbt.putByte("side", (byte) side.getId());
    }

    /**
     * Encodes this SidedPos to an NBT compound and returns it.
     *
     * @return the encoded NBT compound.
     * @see #toNbt(NbtCompound)
     * @see #fromNbt(NbtCompound)
     */
    public @NotNull NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        toNbt(nbt);
        return nbt;
    }

    /**
     * Writes this SidedPos to the given PacketByteBuf.
     *
     * @param buf the buffer to write to.
     * @see #fromPacket(PacketByteBuf)
     */
    public void toPacket(@NotNull PacketByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeByte(side.getId());
    }

    /**
     * Reads a SidedPos from the given NBT compound.
     *
     * @param nbt the NBT compound to read from.
     * @return a new SidedPos with the data read from the given NBT compound.
     * @see #toNbt(NbtCompound)
     * @see #toNbt()
     */
    @Contract("_ -> new")
    public static @NotNull SidedPos fromNbt(@NotNull NbtCompound nbt) {
        int[] pos = nbt.getIntArray("pos");
        return new SidedPos(new BlockPos(pos[0], pos[1], pos[2]), Direction.byId(nbt.getByte("side")));
    }

    /**
     * Reads a SidedPos from the given PacketByteBuf.
     *
     * @param buf the buffer to read from.
     * @return a new SidedPos with the data read from the given buffer.
     * @see #toPacket(PacketByteBuf)
     */
    @Contract("_ -> new")
    public static @NotNull SidedPos fromPacket(@NotNull PacketByteBuf buf) {
        return new SidedPos(buf.readBlockPos(), Direction.byId(buf.readByte()));
    }

    /**
     * Codec for encoding and decoding SidedPoses with a matching structure as produced and consumed by
     * {@link #toNbt(NbtCompound)} and {@link #fromNbt(NbtCompound)}.
     *
     * @see #toNbt(NbtCompound)
     * @see #toNbt()
     * @see #fromNbt(NbtCompound)
     */
    public static Codec<SidedPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(SidedPos::pos),
        Codec.BYTE.xmap(Direction::byId, side -> (byte) side.getId()).fieldOf("side").forGetter(SidedPos::side)
    ).apply(instance, SidedPos::new));
}
