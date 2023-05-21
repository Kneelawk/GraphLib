package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
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
import com.kneelawk.graphlib.api.node.NodeKeyDecoder;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.node.NodeKey;
import com.kneelawk.graphlib.api.world.StorageChunk;
import com.kneelawk.graphlib.impl.GLLog;

public class SimpleBlockGraphChunk implements StorageChunk {
    final ChunkSectionPos chunkPos;
    private final Runnable markDirty;

    private final Short2ObjectMap<LongSet> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    private final LongSet graphsInChunk = new LongLinkedOpenHashSet();
    private @Nullable Short2ObjectMap<Object2LongMap<NodeKey>> graphKeys = null;

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
                    NodeKeyDecoder decoder = universe.getNodeKeyDecoder(typeId);

                    if (decoder == null) {
                        GLLog.error("Chunk tried to load unknown NodeKey type: {} @ {}.", typeId, keyPos);
                        continue;
                    }

                    NbtElement dataTag = keyCom.get("data");
                    NodeKey data = decoder.decode(dataTag);

                    if (graphKeys == null) {
                        graphKeys = new Short2ObjectLinkedOpenHashMap<>();
                    }
                    graphKeys.computeIfAbsent(shortPos, pos1 -> new Object2LongLinkedOpenHashMap<>())
                        .put(data, graphId);
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
        graphKeys = new Short2ObjectLinkedOpenHashMap<>();
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
            BlockPos localPos =
                new BlockPos(ChunkSectionPos.unpackLocalX(shortPos), ChunkSectionPos.unpackLocalY(shortPos),
                    ChunkSectionPos.unpackLocalZ(shortPos));
            inPos.putByte("x", (byte) localPos.getX());
            inPos.putByte("y", (byte) localPos.getY());
            inPos.putByte("z", (byte) localPos.getZ());

            if (graphKeys == null || !graphKeys.containsKey(shortPos)) {
                // We never built our graph-key map
                NbtList ids = new NbtList();
                for (long id : graphsInPos.get(shortPos)) {
                    ids.add(NbtLong.of(id));
                }
                inPos.put("ids", ids);
            } else {
                Object2LongMap<NodeKey> keyMap = graphKeys.get(shortPos);
                NbtList keys = new NbtList();
                for (Object2LongMap.Entry<NodeKey> entry : keyMap.object2LongEntrySet()) {
                    NbtCompound keyCom = new NbtCompound();

                    long id = entry.getLongValue();
                    keyCom.putLong("id", id);

                    NbtElement data = entry.getKey().toTag();
                    if (data != null) {
                        keyCom.put("data", data);
                    }

                    keyCom.putString("type", entry.getKey().getTypeId().toString());

                    keys.add(keyCom);
                }
                inPos.put("keys", keys);
            }
            inPosList.add(inPos);
        }
        nbt.put("inPos", inPosList);
    }

    public void putGraphWithKey(long id, @NotNull PosNodeKey key, Long2ObjectFunction<BlockGraph> graphGetter) {
        markDirty.run();

        short posShort = ChunkSectionPos.packLocal(key.pos());

        Short2ObjectMap<Object2LongMap<NodeKey>> graphKeys = getGraphKeys(graphGetter);
        graphKeys.computeIfAbsent(posShort, pos -> new Object2LongLinkedOpenHashMap<>()).put(key.nodeKey(), id);

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
            if (removed && graphKeys != null) {
                Object2LongMap<NodeKey> keys = graphKeys.get(posShort);
                if (keys != null) {
                    keys.values().removeIf(l -> l == id);

                    if (keys.isEmpty()) {
                        graphKeys.remove(posShort);
                    }
                }
            }
        }
    }

    public @Nullable BlockGraph getGraphForKey(PosNodeKey key, Long2ObjectFunction<BlockGraph> graphGetter) {
        Short2ObjectMap<Object2LongMap<NodeKey>> keys = getGraphKeys(graphGetter);

        Object2LongMap<NodeKey> uKeys = keys.get(ChunkSectionPos.packLocal(key.pos()));
        if (uKeys == null) return null;
        if (!uKeys.containsKey(key.nodeKey())) return null;

        return graphGetter.get(uKeys.getLong(key));
    }

    public LongSet getGraphsAt(BlockPos pos) {
        return graphsInPos.get(ChunkSectionPos.packLocal(pos));
    }

    public LongSet getGraphs() {
        return graphsInChunk;
    }

    public void removeGraphWithKey(@NotNull PosNodeKey key) {
        markDirty.run();
        short posShort = ChunkSectionPos.packLocal(key.pos());
        if (graphKeys != null) {
            Object2LongMap<NodeKey> keys = graphKeys.get(posShort);
            if (keys != null) {
                keys.removeLong(key.nodeKey());

                if (keys.isEmpty()) {
                    graphKeys.remove(posShort);
                }
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

    private @NotNull Short2ObjectMap<Object2LongMap<NodeKey>> getGraphKeys(
        Long2ObjectFunction<BlockGraph> graphGetter) {
        if (graphKeys == null) {
            graphKeys = new Short2ObjectLinkedOpenHashMap<>();

            rebuildGraphKeys(graphGetter);
        }
        return graphKeys;
    }

    private void rebuildGraphKeys(Long2ObjectFunction<BlockGraph> graphGetter) {
        // Should only be called when it is known that graphKeys != null
        assert graphKeys != null;

        markDirty.run();

        graphKeys.clear();
        for (LongIterator iter = graphsInChunk.iterator(); iter.hasNext(); ) {
            long graphId = iter.nextLong();

            BlockGraph graph = graphGetter.get(graphId);
            if (graph == null) {
                GLLog.warn("Chunk encountered null graph for {} when rebuilding graph keys @ {}", graphId, chunkPos);
                continue;
            }

            for (NodeHolder<BlockNode> holder : graph.getNodes().toList()) {
                PosNodeKey key = holder.getNodeKey();
                BlockPos pos = key.pos();
                if (chunkPos.getMinX() <= pos.getX() && pos.getX() <= chunkPos.getMaxX() &&
                    chunkPos.getMinY() <= pos.getY() && pos.getY() <= chunkPos.getMaxY() &&
                    chunkPos.getMinZ() <= pos.getZ() && pos.getZ() <= chunkPos.getMaxZ()) {
                    graphKeys.computeIfAbsent(ChunkSectionPos.packLocal(pos),
                        pos1 -> new Object2LongLinkedOpenHashMap<>()).put(key.nodeKey(), graphId);
                }
            }
        }
    }
}
