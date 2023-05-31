package com.kneelawk.graphlib.api.graph;

import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.GraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.api.graph.user.SidedBlockNode;
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
     * Gets the node holder at a specific position.
     * @param pos the position of the node to get.
     * @return the node holder at the given position.
     */
    @Nullable NodeHolder<BlockNode> getNodeAt(@NotNull NodePos pos);

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
     * Gets all the nodes in this graph.
     *
     * @return a stream of all the nodes in this graph.
     */
    @NotNull Stream<NodeHolder<BlockNode>> getNodes();

    /**
     * Gets all nodes in this graph with the given type.
     *
     * @param type the type that all returned nodes must be.
     * @param <T>  the type of the nodes we're searching for.
     * @return a stream of all the nodes in this graph with the given type.
     */
    @NotNull <T extends BlockNode> Stream<NodeHolder<T>> getNodesOfType(@NotNull Class<T> type);

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
