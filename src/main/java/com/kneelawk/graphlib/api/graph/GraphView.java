package com.kneelawk.graphlib.api.graph;

import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.node.PosNodeKey;
import com.kneelawk.graphlib.api.node.SidedBlockNode;
import com.kneelawk.graphlib.api.util.SidedPos;

/**
 * Interface that allows access to the nodes at given positions.
 */
public interface GraphView {
    /**
     * Gets the universe this graph-view belongs to.
     *
     * @return the universe this belongs to.
     */
    @NotNull Identifier getUniverse();

    /**
     * Gets the nodes in the given block-position.
     *
     * @param pos the block-position to get the nodes in.
     * @return a stream of all the nodes in the given block-position.
     */
    @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos);



    /**
     * Gets the nodes in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in the given sided block-position.
     */
    @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos);

    /**
     * Gets the node with the given key, if it exists.
     *
     * @param key the key to look for the node by.
     * @return a node holder holding the node with the given key.
     */
    @Nullable NodeHolder<BlockNode> getNode(PosNodeKey key);

    /**
     * Gets the IDs of all graphs with nodes in the given block-position.
     *
     * @param pos the block-position to get the IDs of graphs with nodes at.
     * @return a stream of all the IDs of graphs with nodes in the given block-position.
     */
    @NotNull LongStream getGraphsAt(@NotNull BlockPos pos);

    /**
     * Gets the graph with the given ID.
     * <p>
     * Note: this <b>may</b> involve loading the graph from the filesystem.
     *
     * @param id the ID of the graph to get.
     * @return the graph with the given ID.
     */
    @Nullable
    BlockGraph getGraph(long id);

    /**
     * Gets all graph ids in the given chunk section.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @param pos the position of the chunk section to get the graphs in.
     * @return a stream of all graph ids in the given chunk section.
     */
    @NotNull LongStream getGraphsInChunkSection(@NotNull ChunkSectionPos pos);

    /**
     * Gets all graph ids in the given chunk.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @param pos the position of the chunk to get the graphs in.
     * @return a stream of all graph ids in the given chunk.
     */
    @NotNull LongStream getGraphsInChunk(@NotNull ChunkPos pos);

    /**
     * Gets all graph ids in this graph controller.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @return a stream of all graph ids in this graph controller.
     */
    @NotNull LongStream getGraphs();
}
