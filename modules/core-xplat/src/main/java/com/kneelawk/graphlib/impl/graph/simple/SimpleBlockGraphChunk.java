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

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.world.StorageChunk;
import com.kneelawk.graphlib.impl.GLLog;

public class SimpleBlockGraphChunk implements StorageChunk {
    final SectionPos chunkPos;
    private final Runnable markDirty;

    private final Short2ObjectMap<LongSet> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    private final LongSet graphsInChunk = new LongLinkedOpenHashSet();
    private @Nullable Short2ObjectMap<Object2LongMap<BlockNode>> blockNodes = null;

    public SimpleBlockGraphChunk(@NotNull CompoundTag nbt, @NotNull SectionPos chunkPos,
                                 @NotNull Runnable markDirty, SimpleGraphUniverse universe) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;

        ListTag inChunkList = nbt.getList("inChunk", Tag.TAG_LONG);
        for (Tag element : inChunkList) {
            graphsInChunk.add(((LongTag) element).getAsLong());
        }

        ListTag inPosList = nbt.getList("inPos", Tag.TAG_COMPOUND);
        for (Tag element : inPosList) {
            CompoundTag com = (CompoundTag) element;
            // positions are bytes because they are only 0-15
            BlockPos pos = new BlockPos(com.getByte("x"), com.getByte("y"), com.getByte("z"));

            // This also decodes block node -> graph lookups
            ListTag nodes = com.getList("nodes", Tag.TAG_COMPOUND);
            if (!nodes.isEmpty()) {
                short shortPos = SectionPos.sectionRelativePos(pos);
                LongSet inPos = graphsInPos.computeIfAbsent(shortPos, s -> new LongLinkedOpenHashSet());
                for (Tag keyElement : nodes) {
                    CompoundTag keyCom = (CompoundTag) keyElement;
                    long graphId = keyCom.getLong("id");
                    inPos.add(graphId);

                    BlockPos keyPos = pos.offset(chunkPos.minBlockX(), chunkPos.minBlockY(), chunkPos.minBlockZ());

                    ResourceLocation typeId = new ResourceLocation(keyCom.getString("type"));
                    BlockNodeType type = universe.getNodeType(typeId);

                    if (type == null) {
                        GLLog.error("Chunk tried to load unknown BlockNode type: {} @ {}.", typeId, keyPos);
                        continue;
                    }

                    Tag dataTag = keyCom.get("data");
                    BlockNode data = type.getDecoder().decode(dataTag);

                    if (data == null) {
                        GLLog.error("Unable to decode chunk BlockNode with type: {} @ {}.", typeId, keyPos);
                        continue;
                    }

                    if (blockNodes == null) {
                        blockNodes = new Short2ObjectLinkedOpenHashMap<>();
                    }
                    blockNodes.computeIfAbsent(shortPos, pos1 -> new Object2LongLinkedOpenHashMap<>())
                        .put(data, graphId);
                }
            }

            // Legacy route
            ListTag ids = com.getList("ids", Tag.TAG_LONG);
            if (!ids.isEmpty()) {
                LongSet inPos = graphsInPos.computeIfAbsent(SectionPos.sectionRelativePos(pos),
                    s -> new LongLinkedOpenHashSet());
                for (Tag idElement : ids) {
                    inPos.add(((LongTag) idElement).getAsLong());
                }
            }
        }
    }

    public SimpleBlockGraphChunk(@NotNull SectionPos chunkPos, @NotNull Runnable markDirty) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;
        blockNodes = new Short2ObjectLinkedOpenHashMap<>();
    }

    @Override
    public void toNbt(@NotNull CompoundTag nbt) {
        ListTag inChunkList = new ListTag();
        for (long id : graphsInChunk) {
            inChunkList.add(LongTag.valueOf(id));
        }
        nbt.put("inChunk", inChunkList);

        ListTag inPosList = new ListTag();
        ShortIterator keyIterator = graphsInPos.keySet().iterator();
        while (keyIterator.hasNext()) {
            short shortPos = keyIterator.nextShort();
            CompoundTag inPos = new CompoundTag();
            BlockPos localPos =
                new BlockPos(SectionPos.sectionRelativeX(shortPos), SectionPos.sectionRelativeY(shortPos),
                    SectionPos.sectionRelativeZ(shortPos));
            inPos.putByte("x", (byte) localPos.getX());
            inPos.putByte("y", (byte) localPos.getY());
            inPos.putByte("z", (byte) localPos.getZ());

            if (blockNodes == null || !blockNodes.containsKey(shortPos)) {
                // We never built our graph-key map
                ListTag ids = new ListTag();
                for (long id : graphsInPos.get(shortPos)) {
                    ids.add(LongTag.valueOf(id));
                }
                inPos.put("ids", ids);
            } else {
                Object2LongMap<BlockNode> keyMap = blockNodes.get(shortPos);
                ListTag nodes = new ListTag();
                for (Object2LongMap.Entry<BlockNode> entry : keyMap.object2LongEntrySet()) {
                    CompoundTag keyCom = new CompoundTag();

                    long id = entry.getLongValue();
                    keyCom.putLong("id", id);

                    Tag data = entry.getKey().toTag();
                    if (data != null) {
                        keyCom.put("data", data);
                    }

                    keyCom.putString("type", entry.getKey().getType().getId().toString());

                    nodes.add(keyCom);
                }
                inPos.put("nodes", nodes);
            }
            inPosList.add(inPos);
        }
        nbt.put("inPos", inPosList);
    }

    public void clear() {
        graphsInPos.clear();
        graphsInChunk.clear();
        if (blockNodes == null) {
            blockNodes = new Short2ObjectLinkedOpenHashMap<>();
        } else {
            blockNodes.clear();
        }
    }

    public void putGraphWithNode(long id, @NotNull NodePos key, Long2ObjectFunction<SimpleBlockGraph> graphGetter) {
        markDirty.run();

        short posShort = SectionPos.sectionRelativePos(key.pos());

        Short2ObjectMap<Object2LongMap<BlockNode>> graphNodes = getGraphNodes(graphGetter);
        graphNodes.computeIfAbsent(posShort, pos -> new Object2LongLinkedOpenHashMap<>()).put(key.node(), id);

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

            // also remove nodes associated with said graph
            if (removed && blockNodes != null) {
                Object2LongMap<BlockNode> nodes = blockNodes.get(posShort);
                if (nodes != null) {
                    nodes.values().removeIf(l -> l == id);

                    if (nodes.isEmpty()) {
                        blockNodes.remove(posShort);
                    }
                }
            }
        }
    }

    public @Nullable SimpleBlockGraph getGraphForNode(NodePos key, Long2ObjectFunction<SimpleBlockGraph> graphGetter) {
        Short2ObjectMap<Object2LongMap<BlockNode>> nodes = getGraphNodes(graphGetter);

        Object2LongMap<BlockNode> uNodes = nodes.get(SectionPos.sectionRelativePos(key.pos()));
        if (uNodes == null) return null;
        if (!uNodes.containsKey(key.node())) return null;

        return graphGetter.get(uNodes.getLong(key.node()));
    }

    public boolean containsNode(NodePos key, Long2ObjectFunction<SimpleBlockGraph> graphGetter) {
        Short2ObjectMap<Object2LongMap<BlockNode>> nodes = getGraphNodes(graphGetter);

        Object2LongMap<BlockNode> uNodes = nodes.get(SectionPos.sectionRelativePos(key.pos()));
        if (uNodes == null) return false;
        return uNodes.containsKey(key.node());
    }

    public LongSet getGraphsAt(BlockPos pos) {
        return graphsInPos.get(SectionPos.sectionRelativePos(pos));
    }

    public LongSet getGraphs() {
        return graphsInChunk;
    }

    public void removeGraphWithNodeUnchecked(@NotNull NodePos key) {
        markDirty.run();
        short posShort = SectionPos.sectionRelativePos(key.pos());
        if (blockNodes != null) {
            Object2LongMap<BlockNode> nodes = blockNodes.get(posShort);
            if (nodes != null) {
                nodes.removeLong(key.node());

                if (nodes.isEmpty()) {
                    blockNodes.remove(posShort);
                }
            }
        }
    }

    public void removeGraphInPosUnchecked(long id, @NotNull BlockPos pos) {
        markDirty.run();
        short local = SectionPos.sectionRelativePos(pos);
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

    private @NotNull Short2ObjectMap<Object2LongMap<BlockNode>> getGraphNodes(
        Long2ObjectFunction<SimpleBlockGraph> graphGetter) {
        if (blockNodes == null) {
            blockNodes = new Short2ObjectLinkedOpenHashMap<>();

            rebuildGraphNodes(graphGetter);
        }
        return blockNodes;
    }

    private void rebuildGraphNodes(Long2ObjectFunction<SimpleBlockGraph> graphGetter) {
        // Should only be called when it is known that blockNodes != null
        assert blockNodes != null;

        GLLog.debug("Rebuilding block node -> graph lookup for chunk {}", chunkPos);

        markDirty.run();

        blockNodes.clear();
        for (LongIterator iter = graphsInChunk.iterator(); iter.hasNext(); ) {
            long graphId = iter.nextLong();

            BlockGraph graph = graphGetter.get(graphId);
            if (graph == null) {
                GLLog.warn("Chunk encountered null graph for {} when rebuilding block node -> graph lookup @ {}",
                    graphId, chunkPos);
                continue;
            }

            for (NodeHolder<BlockNode> holder : graph.getNodes().toList()) {
                NodePos key = holder.getPos();
                BlockPos pos = key.pos();
                if (chunkPos.minBlockX() <= pos.getX() && pos.getX() <= chunkPos.maxBlockX() &&
                    chunkPos.minBlockY() <= pos.getY() && pos.getY() <= chunkPos.maxBlockY() &&
                    chunkPos.minBlockZ() <= pos.getZ() && pos.getZ() <= chunkPos.maxBlockZ()) {
                    blockNodes.computeIfAbsent(SectionPos.sectionRelativePos(pos),
                        pos1 -> new Object2LongLinkedOpenHashMap<>()).put(key.node(), graphId);
                }
            }
        }
    }
}
