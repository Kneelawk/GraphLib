package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.world.StorageChunk;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.util.UUID;

public class BlockGraphChunk implements StorageChunk {
    final ChunkSectionPos chunkPos;

    Short2ObjectMap<ObjectSet<UUID>> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    ObjectSet<UUID> graphsInChunk = new ObjectLinkedOpenHashSet<>();

    public BlockGraphChunk(NbtCompound nbt, ChunkSectionPos chunkPos) {
        this.chunkPos = chunkPos;

        NbtList inChunkList = nbt.getList("inChunk", NbtType.INT_ARRAY);
        for (NbtElement element : inChunkList) {
            graphsInChunk.add(NbtHelper.toUuid(element));
        }

        NbtList inPosList = nbt.getList("inPos", NbtType.COMPOUND);
        for (NbtElement element : inPosList) {
            NbtCompound com = (NbtCompound) element;
            // positions are bytes because they are only 0-15
            BlockPos pos = new BlockPos(com.getByte("x"), com.getByte("y"), com.getByte("z"));
            NbtList ids = com.getList("ids", NbtType.INT_ARRAY);
            if (!ids.isEmpty()) {
                ObjectSet<UUID> inPos = graphsInPos.computeIfAbsent(ChunkSectionPos.packLocal(pos),
                        (short s) -> new ObjectLinkedOpenHashSet<>());
                for (NbtElement idElement : ids) {
                    inPos.add(NbtHelper.toUuid(idElement));
                }
            }
        }
    }

    public BlockGraphChunk(ChunkSectionPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    @Override
    public void toNbt(NbtCompound nbt) {
        NbtList inChunkList = new NbtList();
        for (UUID id : graphsInChunk) {
            inChunkList.add(NbtHelper.fromUuid(id));
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
            for (UUID id : graphsInPos.get(shortPos)) {
                ids.add(NbtHelper.fromUuid(id));
            }
            inPos.put("ids", ids);
            inPosList.add(inPos);
        }
        nbt.put("inPos", inPosList);
    }
}
