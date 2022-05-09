package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.wire.SidedWireBlockNode;
import com.kneelawk.graphlib.wire.SidedWireConnectionFilter;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface BlockNode {
    Identifier getTypeId();

    @Nullable NbtElement toTag();

    /**
     * Collects nodes in the world that this node can connect to.
     * <p>
     * <b>Contract:</b> This method must only return nodes that
     * {@link #canConnect(ServerWorld, NodeView, BlockPos, Node)} would have returned <code>true</code> for.
     *
     * @param world    the world of blocks.
     * @param nodeView the world of nodes.
     * @param pos      this node's block-position.
     * @return all nodes this node can connect to.
     * @see com.kneelawk.graphlib.wire.WireConnectionDiscoverers#wireFindConnections(SidedWireBlockNode, ServerWorld, NodeView, BlockPos, SidedWireConnectionFilter)
     */
    Collection<Node<BlockNodeWrapper<?>>> findConnections(ServerWorld world, NodeView nodeView, BlockPos pos);

    /**
     * Determines whether this node can connect to another node.
     * <p>
     * <b>Contract:</b> This method must only return <code>true</code> for nodes that would be returned from
     * {@link #findConnections(ServerWorld, NodeView, BlockPos)}.
     *
     * @param world    the world of blocks.
     * @param nodeView the world of nodes.
     * @param pos      this node's block-position.
     * @param other    the other node to attempt to connect to.
     * @return whether this node can connect to the other node.
     * @see com.kneelawk.graphlib.wire.WireConnectionDiscoverers#wireCanConnect(SidedWireBlockNode, ServerWorld, BlockPos, SidedWireConnectionFilter, Node)
     */
    boolean canConnect(ServerWorld world, NodeView nodeView, BlockPos pos, Node<BlockNodeWrapper<?>> other);

    void onChanged(ServerWorld world, BlockPos pos);

    @Override
    int hashCode();

    @Override
    boolean equals(Object o);
}
