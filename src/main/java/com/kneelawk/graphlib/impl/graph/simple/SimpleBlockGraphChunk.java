package com.kneelawk.graphlib.impl.graph.simple;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.BlockNodeDecoder;
import com.kneelawk.graphlib.api.node.NodeKey;
import com.kneelawk.graphlib.api.node.UniqueData;
import com.kneelawk.graphlib.api.world.StorageChunk;
import com.kneelawk.graphlib.impl.GLLog;

public class SimpleBlockGraphChunk implements StorageChunk {
    final ChunkSectionPos chunkPos;
    private final Runnable markDirty;

    private final Short2ObjectMap<LongSet> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    private final LongSet graphsInChunk = new LongLinkedOpenHashSet();
    private @Nullable Object2LongMap<NodeKey> graphKeys = null;
    private final Short2ObjectMap<ObjectSet<NodeKey>> keysInPos = new Short2ObjectLinkedOpenHashMap<>();

    public SimpleBlockGraphChunk(@NotNull NbtCompound nbt, @NotNull ChunkSectionPos chunkPos,
                                 @NotNull Runnable markDirty, SimpleGraphUniverse universe) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;

        NbtList inChunkList = nbt.getList("inChunk", NbtElement.LONG_TYPE);
        for (NbtElement element : inChunkList) {
            graphsInChunk.add(((NbtLong) element).longValue());
        }

        NbtList inPosList = nbt.getList("inPos", NbtElement.COMPOUND_TYPE);
        for (NbtElement element : inPosList) {
            NbtCompound com = (NbtCompound) element;
            // positions are bytes because they are only 0-15
            BlockPos pos = new BlockPos(com.getByte("x"), com.getByte("y"), com.getByte("z"));

            // This also decodes graph keys
            NbtList keys = com.getList("keys", NbtElement.COMPOUND_TYPE);
            if (!keys.isEmpty()) {
                short shortPos = ChunkSectionPos.packLocal(pos);
                LongSet inPos = graphsInPos.computeIfAbsent(shortPos, s -> new LongLinkedOpenHashSet());
                for (NbtElement keyElement : keys) {
                    NbtCompound keyCom = (NbtCompound) keyElement;
                    long graphId = keyCom.getLong("id");
                    inPos.add(graphId);

                    BlockPos keyPos = pos.add(chunkPos.getMinX(), chunkPos.getMinY(), chunkPos.getMinZ());

                    Identifier typeId = new Identifier(keyCom.getString("type"));
                    BlockNodeDecoder decoder = universe.getDecoder(typeId);

                    if (decoder == null) {
                        GLLog.warn("Chunk tried to load unknown UniqueData type: {} @ {}", typeId, keyPos);
                        continue;
                    }

                    NbtElement dataTag = keyCom.get("data");
                    UniqueData node = decoder.createUniqueDataFromTag(dataTag);

                    if (graphKeys == null) {
                        graphKeys = new Object2LongLinkedOpenHashMap<>();
                    }
                    NodeKey key = new NodeKey(keyPos, node);
                    graphKeys.put(key, graphId);
                    keysInPos.computeIfAbsent(shortPos, pos1 -> new ObjectLinkedOpenHashSet<>()).add(key);
                }
            }

            // Legacy route
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

    public SimpleBlockGraphChunk(@NotNull ChunkSectionPos chunkPos, @NotNull Runnable markDirty) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;
        graphKeys = new Object2LongLinkedOpenHashMap<>();
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

            Set<NodeKey> keySet = keysInPos.get(shortPos);
            if (graphKeys == null || keySet == null) {
                // We never built our graph-key map
                NbtList ids = new NbtList();
                for (long id : graphsInPos.get(shortPos)) {
                    ids.add(NbtLong.of(id));
                }
                inPos.put("ids", ids);
            } else {
                NbtList keys = new NbtList();
                for (NodeKey key : keysInPos.get(shortPos)) {
                    NbtCompound keyCom = new NbtCompound();

                    if (!graphKeys.containsKey(key)) {
                        GLLog.warn("Chunk keys-in-pos & chunk-keys are out of sync! Key {} has no id! Ignoring...",
                            key);
                        continue;
                    }

                    long id = graphKeys.getLong(key);
                    keyCom.putLong("id", id);

                    NbtElement data = key.uniqueData().toTag();
                    if (data != null) {
                        keyCom.put("data", data);
                    }

                    keyCom.putString("type", key.uniqueData().getTypeId().toString());

                    keys.add(keyCom);
                }
                inPos.put("keys", keys);
            }
            inPosList.add(inPos);
        }
        nbt.put("inPos", inPosList);
    }

    public void putGraphWithKey(long id, @NotNull NodeKey key, Long2ObjectFunction<BlockGraph> graphGetter) {
        markDirty.run();

        short posShort = ChunkSectionPos.packLocal(key.pos());

        Object2LongMap<NodeKey> graphKeys = getGraphKeys(graphGetter);
        graphKeys.put(key, id);
        keysInPos.computeIfAbsent(posShort, pos -> new ObjectLinkedOpenHashSet<>()).add(key);

        graphsInChunk.add(id);
        graphsInPos.computeIfAbsent(posShort, s -> new LongLinkedOpenHashSet()).add(id);
    }

    public void removeGraph(long id) {
        markDirty.run();
        graphsInChunk.remove(id);

        // Worst possible case here is 4096 iterations.
        IntIterator posIterator = graphsInPos.keySet().intIterator();
        while (posIterator.hasNext()) {
            short posShort = (short) posIterator.nextInt();
            LongSet graphs = graphsInPos.get(posShort);
            boolean removed = graphs.remove(id);
            if (graphs.isEmpty()) {
                posIterator.remove();
            }

            // also remove keys associated with said graph
            Set<NodeKey> keys = keysInPos.get(posShort);
            if (removed && graphKeys != null && keys != null) {
                for (NodeKey key : keys) {
                    if (graphKeys.getLong(key) == id) {
                        graphKeys.removeLong(key);
                        keys.remove(key);
                    }
                }

                if (keys.isEmpty()) {
                    keysInPos.remove(posShort);
                }
            }
        }
    }

    public @Nullable BlockGraph getGraphForKey(NodeKey key, Long2ObjectFunction<BlockGraph> graphGetter) {
        Object2LongMap<NodeKey> keys = getGraphKeys(graphGetter);

        if (!keys.containsKey(key)) return null;

        return graphGetter.get(keys.getLong(key));
    }

    public LongSet getGraphsAt(BlockPos pos) {
        return graphsInPos.get(ChunkSectionPos.packLocal(pos));
    }

    public LongSet getGraphs() {
        return graphsInChunk;
    }

    public void removeGraphWithKey(long id, @NotNull NodeKey key) {
        markDirty.run();
        short posShort = ChunkSectionPos.packLocal(key.pos());
        Set<NodeKey> keys = keysInPos.get(posShort);
        if (graphKeys != null && keys != null) {
            graphKeys.removeLong(key);
            keys.remove(key);

            if (keys.isEmpty()) {
                keysInPos.remove(posShort);
            }
        }
    }

    public void removeGraphInPos(long id, @NotNull BlockPos pos) {
        markDirty.run();
        short local = ChunkSectionPos.packLocal(pos);
        LongSet graphs = graphsInPos.get(local);
        graphs.remove(id);
        if (graphs.isEmpty()) {
            graphsInPos.remove(local);
        }
    }

    public void removeGraphUnchecked(long id) {
        markDirty.run();
        graphsInChunk.remove(id);
    }

    private @NotNull Object2LongMap<NodeKey> getGraphKeys(Long2ObjectFunction<BlockGraph> graphGetter) {
        if (graphKeys == null) {
            graphKeys = new Object2LongLinkedOpenHashMap<>();

            rebuildGraphKeys(graphGetter);
        }
        return graphKeys;
    }

    private void rebuildGraphKeys(Long2ObjectFunction<BlockGraph> graphGetter) {
        // Should only be called when it is known that graphKeys != null
        assert graphKeys != null;

        markDirty.run();

        graphKeys.clear();
        keysInPos.clear();
        for (LongIterator iter = graphsInChunk.iterator(); iter.hasNext(); ) {
            long graphId = iter.nextLong();

            BlockGraph graph = graphGetter.get(graphId);
            if (graph == null) {
                GLLog.warn("Chunk encountered null graph for {} when rebuilding graph keys @ {}", graphId, chunkPos);
                continue;
            }

            for (NodeHolder<BlockNode> holder : graph.getNodes().toList()) {
                NodeKey key = holder.toNodeKey();
                graphKeys.put(key, graphId);
                keysInPos.computeIfAbsent(ChunkSectionPos.packLocal(holder.getPos()),
                    pos -> new ObjectLinkedOpenHashSet<>()).add(key);
            }
        }
    }
}
