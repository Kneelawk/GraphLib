package com.kneelawk.graphlib.api.graph;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
import com.kneelawk.graphlib.api.util.CacheCategory;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.graphlib.api.util.SidedPos;

/**
 * Holds and manages a set of block nodes.
 */
public interface BlockGraph {
    /**
     * Gets this graph's graph ID.
     *
     * @return the ID of this graph.
     */
    long getId();

    /**
     * Gets the graph view that this graph exists within.
     *
     * @return the graph view that this graph exists within.
     */
    GraphView getGraphView();

    /**
     * Gets all the nodes in this graph in the given block-position.
     *
     * @param pos the block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given block-position.
     */
    @NotNull Stream<NodeHolder<BlockNode>> getNodesAt(@NotNull BlockPos pos);

    /**
     * Gets all the nodes in this graph in the given sided block-position.
     *
     * @param pos the sided block-position to get the nodes in.
     * @return a stream of all the nodes in this graph in the given sided block-position.
     */
    @NotNull Stream<NodeHolder<SidedBlockNode>> getNodesAt(@NotNull SidedPos pos);

    /**
     * Checks whether the given node actually exists in this graph.
     *
     * @param pos the position of the node to check.
     * @return <code>true</code> if the given node actually exists.
     */
    boolean nodeExistsAt(@NotNull NodePos pos);

    /**
     * Gets the node holder at a specific position.
     *
     * @param pos the position of the node to get.
     * @return the node holder at the given position.
     */
    @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos);

    /**
     * Checks whether the given link actually exists in this graph.
     *
     * @param pos the position of the link to check.
     * @return <code>true</code> if the given link actually exists.
     */
    boolean linkExistsAt(@NotNull LinkPos pos);

    /**
     * Gets the link holder at the given position, if it exists.
     *
     * @param pos the position to get the link at.
     * @return the link holder at the given position, if it exists.
     */
    @Nullable LinkHolder<LinkKey> getLinkAt(@NotNull LinkPos pos);

    /**
     * Gets the node entity at a given pos, if it exists.
     *
     * @param pos the position to find the node entity at.
     * @return the node entity, or <code>null</code> if there is no node entity present at the given location.
     */
    @Nullable NodeEntity getNodeEntity(@NotNull NodePos pos);

    /**
     * Gets the link entity at the given pos, if it exists.
     *
     * @param pos the position to find the link entity at.
     * @return the link entity, or <code>null</code> if there is no link entity present at the given location.
     */
    @Nullable LinkEntity getLinkEntity(@NotNull LinkPos pos);

    /**
     * Gets all the nodes in the given chunk section.
     *
     * @param pos the position of the chunk section to get all nodes from.
     * @return a stream of all nodes in the given chunk section.
     */
    @NotNull Stream<NodeHolder<BlockNode>> getNodesInChunkSection(ChunkSectionPos pos);

    /**
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @NotNull Stream<NodeHolder<BlockNode>> getNodes();

    /**
     * Gets all node entities in this graph.
     *
     * @return a stream of all node entities in this graph.
     */
    @NotNull Stream<NodeEntity> getNodeEntities();

    /**
     * Gets all link entities in this graph.
     *
     * @return a stream of all link entities in this graph.
     */
    @NotNull Stream<LinkEntity> getLinkEntities();

    /**
     * Gets all nodes in this graph that match the given cache category.
     *
     * @param category the category of the cache to retrieve.
     * @param <T>      the type of node being retrieved.
     * @return all nodes in this graph that match the given cache category.
     */
    @NotNull <T extends BlockNode> Collection<NodeHolder<T>> getCachedNodes(@NotNull CacheCategory<T> category);

    /**
     * Gets all the chunk sections that this graph currently has nodes in.
     *
     * @return a stream of all the chunk sections this graph is in.
     */
    @NotNull Stream<ChunkSectionPos> getChunks();

    /**
     * Gets a graph entity attached to this graph.
     *
     * @param type the type of graph entity to retrieve.
     * @param <G>  the type of graph entity to retrieve.
     * @return the given graph entity attached to this graph.
     * @throws IllegalArgumentException if the given graph entity type has not been registered with this graph's universe.
     */
    @NotNull <G extends GraphEntity<G>> G getGraphEntity(GraphEntityType<G> type);

    /**
     * Gets the number of nodes in this graph.
     *
     * @return the number of nodes in this graph.
     */
    int size();

    /**
     * Gets whether this graph is empty.
     *
     * @return <code>true</code> if this graph has no nodes, <code>false</code> otherwise.
     */
    boolean isEmpty();
}
