package com.kneelawk.graphlib.impl.graph.simple;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import com.kneelawk.codextra.api.Codextra;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.world.RegionBasedStorage;

public class SimpleBlockGraphChunk {
    // why did I use a different key for node data in chunks than everywhere else????
    private static final MapCodec<BlockNode> CHUNK_NODE_CODEC = BlockNodeType.REF_CODEC.dispatchMap(BlockNode::getType,
        type -> Codextra.unitHandlingFieldOf("data", type.getCodec()));

    private static final Codec<LongSet> LONG_SET_CODEC =
        Codec.LONG_STREAM.xmap(LongLinkedOpenHashSet::toSet, LongCollection::longStream);

    // positions are bytes because they are only 0-15
    private static final MapCodec<BlockPos> BLOCK_POS_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Codec.BYTE.fieldOf("x").forGetter(pos -> (byte) pos.getX()),
        Codec.BYTE.fieldOf("y").forGetter(pos -> (byte) pos.getY()),
        Codec.BYTE.fieldOf("z").forGetter(pos -> (byte) pos.getZ())
    ).apply(instance, BlockPos::new));

    public static final Codec<SimpleBlockGraphChunk> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        SimpleServerGraphWorld.CONTROLLER.retrieve(),
        RegionBasedStorage.SECTION_POS.retrieve(),
        RegionBasedStorage.MARK_DIRTY.retrieve(),
        Serial.CODEC.forGetter(SimpleBlockGraphChunk::toSerial)
    ).apply(instance, SimpleBlockGraphChunk::new));

    private record Serial(LongSet inChunk, List<SerialInPos> inPos) {
        public static final MapCodec<Serial> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LONG_SET_CODEC.fieldOf("inChunk").forGetter(Serial::inChunk),
            SerialInPos.CODEC.listOf().fieldOf("inPos").forGetter(Serial::inPos)
        ).apply(instance, Serial::new));
    }

    private record SerialInPos(BlockPos pos, Either<List<SerialNode>, LongSet> either) {
        static final Codec<SerialInPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BLOCK_POS_CODEC.forGetter(SerialInPos::pos),
            Codec.mapEither(SerialNode.CODEC.listOf().fieldOf("nodes"), LONG_SET_CODEC.fieldOf("ids"))
                .forGetter(SerialInPos::either)
        ).apply(instance, SerialInPos::new));
    }

    private record SerialNode(long id, BlockNode node) {
        static final Codec<SerialNode> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("id").forGetter(SerialNode::id),
            CHUNK_NODE_CODEC.forGetter(SerialNode::node)
        ).apply(instance, SerialNode::new));
    }

    final SectionPos chunkPos;
    private final Runnable markDirty;

    private final Short2ObjectMap<LongSet> graphsInPos = new Short2ObjectLinkedOpenHashMap<>();
    private final LongSet graphsInChunk = new LongLinkedOpenHashSet();
    private final Short2ObjectMap<Object2LongMap<BlockNode>> blockNodes = new Short2ObjectLinkedOpenHashMap<>();

    private SimpleBlockGraphChunk(@NotNull SimpleServerGraphWorld world, @NotNull SectionPos chunkPos,
                                  @NotNull Runnable markDirty, @NotNull Serial serial) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;

        graphsInChunk.addAll(serial.inChunk());

        for (SerialInPos serialInPos : serial.inPos()) {
            BlockPos pos = serialInPos.pos();
            short shortPos = SectionPos.sectionRelativePos(pos);
            BlockPos keyPos = pos.offset(chunkPos.minBlockX(), chunkPos.minBlockY(), chunkPos.minBlockZ());

            LongSet inPos = graphsInPos.computeIfAbsent(shortPos, pos1 -> new LongLinkedOpenHashSet());
            Object2LongMap<BlockNode> nodes =
                blockNodes.computeIfAbsent(shortPos, pos1 -> new Object2LongLinkedOpenHashMap<>());

            // load graphs in pos & node->graphId map
            serialInPos.either().ifLeft(serialNodes -> {
                for (SerialNode serialNode : serialNodes) {
                    inPos.add(serialNode.id());
                    nodes.put(serialNode.node(), serialNode.id());
                }
            });

            // Legacy route
            serialInPos.either().ifRight(longs -> {
                inPos.addAll(longs);

                // build missing node->graphId map
                for (LongIterator it = longs.iterator(); it.hasNext(); ) {
                    long graphId = it.nextLong();

                    SimpleBlockGraph graph = world.getGraph(graphId);
                    if (graph != null) {
                        for (var iter = graph.getNodesAt(keyPos).iterator(); iter.hasNext(); ) {
                            var holder = iter.next();
                            nodes.put(holder.getNode(), graphId);
                        }
                    }
                }
            });
        }
    }

    public SimpleBlockGraphChunk(@NotNull SectionPos chunkPos, @NotNull Runnable markDirty) {
        this.chunkPos = chunkPos;
        this.markDirty = markDirty;
    }

    private @NotNull Serial toSerial() {
        List<SerialInPos> inPosList = new ObjectArrayList<>();

        for (ShortIterator keyIter = blockNodes.keySet().iterator(); keyIter.hasNext(); ) {
            short shortPos = keyIter.nextShort();
            BlockPos localPos =
                new BlockPos(SectionPos.sectionRelativeX(shortPos), SectionPos.sectionRelativeY(shortPos),
                    SectionPos.sectionRelativeZ(shortPos));

            List<SerialNode> serialNodes = new ObjectArrayList<>();
            for (Object2LongMap.Entry<BlockNode> nodeEntry : blockNodes.get(shortPos).object2LongEntrySet()) {
                serialNodes.add(new SerialNode(nodeEntry.getLongValue(), nodeEntry.getKey()));
            }

            inPosList.add(new SerialInPos(localPos, Either.left(serialNodes)));
        }

        return new Serial(graphsInChunk, inPosList);
    }

    public void clear() {
        graphsInPos.clear();
        graphsInChunk.clear();
        blockNodes.clear();
    }

    public void putGraphWithNode(long id, @NotNull NodePos key) {
        markDirty.run();

        short posShort = SectionPos.sectionRelativePos(key.pos());

        blockNodes.computeIfAbsent(posShort, pos -> new Object2LongLinkedOpenHashMap<>()).put(key.node(), id);

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
            if (removed) {
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
        Object2LongMap<BlockNode> uNodes = blockNodes.get(SectionPos.sectionRelativePos(key.pos()));
        if (uNodes == null) return null;
        if (!uNodes.containsKey(key.node())) return null;

        return graphGetter.get(uNodes.getLong(key.node()));
    }

    public boolean containsNode(NodePos key) {
        Object2LongMap<BlockNode> uNodes = blockNodes.get(SectionPos.sectionRelativePos(key.pos()));
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
        Object2LongMap<BlockNode> nodes = blockNodes.get(posShort);
        if (nodes != null) {
            nodes.removeLong(key.node());

            if (nodes.isEmpty()) {
                blockNodes.remove(posShort);
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
}
