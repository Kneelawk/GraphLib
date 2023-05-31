package com.kneelawk.graphlib.api.graph;

import java.util.Objects;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
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
    @NotNull GraphUniverse getUniverse();

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
     * Gets the node holder at the given position.
     *
     * @param pos the position to get the node at.
     * @return the node holder at the given position, if any.
     */
    @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos);

    /**
     * Checks whether the given node with the given position exists.
     *
     * @param pos the positioned node to try to find.
     * @return <code>true</code> if the node was found, <code>false</code> otherwise.
     */
    boolean nodeExistsAt(@NotNull NodePos pos);

    /**
     * Gets the graph id of the given node at the given position.
     *
     * @param pos th positioned node to find the graph id of.
     * @return the graph id of the node, of empty if the node was not found.
     */
    @Nullable BlockGraph getGraphForNode(@NotNull NodePos pos);

    /**
     * Gets the node entity at the given position, if it exists.
     *
     * @param pos the position to find the node entity at.
     * @return the node entity at the given position, if it exists.
     */
    @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos);

    /**
     * Gets the link entity at the given position, if it exists.
     *
     * @param pos the position to find the link entity at.
     * @return the link entity at the given position, if it exists.
     */
    @Nullable LinkEntity getLinkEntity(@NotNull LinkPos pos);

    /**
     * Gets the IDs of all graphs with nodes in the given block-position.
     *
     * @param pos the block-position to get the IDs of graphs with nodes at.
     * @return a stream of all the IDs of graphs with nodes in the given block-position.
     */
    @NotNull LongStream getAllGraphIdsAt(@NotNull BlockPos pos);

    /**
     * Gets all graphs with nodes in the given block-position.
     * <p>
     * Note: this loads any unloaded graphs at the given position. If that is not what you want,
     * {@link #getLoadedGraphsAt(BlockPos)} may be what you want.
     *
     * @param pos the block-position to get the graphs with nodes at.
     * @return all graphs with nodes in the given block-position.
     */
    default @NotNull Stream<BlockGraph> getAllGraphsAt(@NotNull BlockPos pos) {
        return getAllGraphIdsAt(pos).mapToObj(this::getGraph).filter(Objects::nonNull);
    }

    /**
     * Gets all loaded graphs at the given position.
     *
     * @param pos the block-position to get the loaded graphs with nodes at.
     * @return all loaded graphs at the given position.
     */
    @NotNull Stream<BlockGraph> getLoadedGraphsAt(@NotNull BlockPos pos);

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
    @NotNull LongStream getAllGraphIdsInChunkSection(@NotNull ChunkSectionPos pos);

    /**
     * Gets all graphs in the given chunk section.
     * <p>
     * Note: this loads all graphs in the given chunk section, which may not be what you want. If you only want the
     * loaded graphs, {@link #getLoadedGraphsInChunkSection(ChunkSectionPos)} may be a better fit.
     *
     * @param pos the position of the chunk section to get the graphs in.
     * @return a stream of all the graphs in the given chunk section.
     */
    default @NotNull Stream<BlockGraph> getAllGraphsInChunkSection(@NotNull ChunkSectionPos pos) {
        return getAllGraphIdsInChunkSection(pos).mapToObj(this::getGraph).filter(Objects::nonNull);
    }

    /**
     * Gets all loaded graphs in the given chunk section.
     *
     * @param pos the position of the chunk section to get the loaded graphs in.
     * @return a stream of all the loaded graphs in the given chunk section.
     */
    @NotNull Stream<BlockGraph> getLoadedGraphsInChunkSection(@NotNull ChunkSectionPos pos);

    /**
     * Gets all graph ids in the given chunk.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @param pos the position of the chunk to get the graphs in.
     * @return a stream of all graph ids in the given chunk.
     */
    @NotNull LongStream getAllGraphIdsInChunk(@NotNull ChunkPos pos);

    /**
     * Gets all graphs in the given chunk.
     * <p>
     * Note: this loads all graphs in the given chunk, which may not be what you want. If you only want the loaded
     * graphs, {@link #getLoadedGraphsInChunk(ChunkPos)} may be a better fit.
     *
     * @param pos the position of the chunk to get the graphs in.
     * @return a stream of all graphs in the given chunk.
     */
    default @NotNull Stream<BlockGraph> getAllGraphsInChunk(@NotNull ChunkPos pos) {
        return getAllGraphIdsInChunk(pos).mapToObj(this::getGraph).filter(Objects::nonNull);
    }

    /**
     * Gets all loaded graphs in the given chunk.
     *
     * @param pos the position of the chunk to get the loaded graphs in.
     * @return a stream of all loaded graphs in the given chunk.
     */
    @NotNull Stream<BlockGraph> getLoadedGraphsInChunk(@NotNull ChunkPos pos);

    /**
     * Gets all graph ids in this graph controller.
     * <p>
     * Note: Not all graph-ids returned here are guaranteed to belong to valid graphs. {@link #getGraph(long)} may
     * return <code>null</code>.
     *
     * @return a stream of all graph ids in this graph world.
     */
    @NotNull LongStream getAllGraphIds();

    /**
     * Gets all graphs in this graph world, loading all graphs. <b>Use with care.</b>
     * <p>
     * This filters out all the null graphs.
     * <p>
     * Because this loads all graphs in the graph world, it can cause significant lag. Chances are, you actually want
     * {@link #getLoadedGraphs()}.
     *
     * @return a stream of all the graphs in this graph world.
     */
    default @NotNull Stream<BlockGraph> getAllGraphs() {
        return getAllGraphIds().mapToObj(this::getGraph).filter(Objects::nonNull);
    }

    /**
     * Gets all currently loaded graphs.
     *
     * @return all the currently loaded graphs.
     */
    @NotNull Stream<BlockGraph> getLoadedGraphs();
}
