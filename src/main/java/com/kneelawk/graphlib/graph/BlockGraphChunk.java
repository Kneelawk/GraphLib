package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.world.StorageChunk;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import org.jetbrains.annotations.NotNull;

public class BlockGraphChunk implements StorageChunk {
    final ChunkSectionPos chunkPos;

    Short2ObjectMap<LongSet> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    LongSet graphsInChunk = new LongLinkedOpenHashSet();

    public BlockGraphChunk(@NotNull NbtCompound nbt, @NotNull ChunkSectionPos chunkPos) {
        this.chunkPos = chunkPos;

        NbtList inChunkList = nbt.getList("inChunk", NbtElement.LONG_TYPE);
        for (NbtElement element : inChunkList) {
            graphsInChunk.add(((NbtLong) element).longValue());
        }

        NbtList inPosList = nbt.getList("inPos", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : inPosList) {
            NbtCompound com = (NbtCompound) element;
            // positions are bytes because they are only 0-15
            BlockPos pos = new BlockPos(com.getByte("x"), com.getByte("y"), com.getByte("z"));
            NbtList ids = com.getList("ids", NbtElement.LONG_TYPE);
            if (!ids.isEmpty()) {
                LongSet inPos = graphsInPos.computeIfAbsent(ChunkSectionPos.packLocal(pos),
                        s -> new LongLinkedOpenHashSet());
                for (NbtElement idElement : ids) {
                    inPos.add(((NbtLong) idElement).longValue());
                }
            }
        }
    }

    public BlockGraphChunk(@NotNull ChunkSectionPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void toNbt(@NotNull NbtCompound nbt) {
        NbtList inChunkList = new NbtList();
        for (long id : graphsInChunk) {
            inChunkList.add(NbtLong.of(id));
        }
        nbt.put("inChunk", inChunkList);

        NbtList inPosList = new NbtList();
        ShortIterator keyIterator = graphsInPos.keySet().iterator();
        while (keyIterator.hasNext()) {
            short shortPos = keyIterator.nextShort();
            NbtCompound inPos = new NbtCompound();
            inPos.putByte("x", (byte) ChunkSectionPos.unpackLocalX(shortPos));
            inPos.putByte("y", (byte) ChunkSectionPos.unpackLocalY(shortPos));
            inPos.putByte("z", (byte) ChunkSectionPos.unpackLocalZ(shortPos));
            NbtList ids = new NbtList();
            for (long id : graphsInPos.get(shortPos)) {
                ids.add(NbtLong.of(id));
            }
            inPos.put("ids", ids);
            inPosList.add(inPos);
        }
        nbt.put("inPos", inPosList);
    }

    public void addGraphInPos(long id, @NotNull BlockPos pos) {
        graphsInChunk.add(id);
        graphsInPos.computeIfAbsent(ChunkSectionPos.packLocal(pos), s -> new LongLinkedOpenHashSet()).add(id);
    }

    public void removeGraph(long id) {
        graphsInChunk.remove(id);

        // Worst possible case here is 4096 iterations.
        IntIterator posIterator = graphsInPos.keySet().intIterator();
        while (posIterator.hasNext()) {
            short posShort = (short) posIterator.nextInt();
            LongSet graphs = graphsInPos.get(posShort);
            graphs.remove(id);
            if (graphs.isEmpty()) {
                posIterator.remove();
            }
        }
    }
}
