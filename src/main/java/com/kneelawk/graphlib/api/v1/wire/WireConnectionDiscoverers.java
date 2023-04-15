package com.kneelawk.graphlib.api.v1.wire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.v1.graph.GraphView;
import com.kneelawk.graphlib.api.v1.graph.NodeHolder;
import com.kneelawk.graphlib.api.v1.node.BlockNode;
import com.kneelawk.graphlib.api.v1.util.DirectionUtils;
import com.kneelawk.graphlib.api.v1.util.graph.Node;

/**
 * Contains wire connection finder and checker implementations for use in {@link BlockNode}
 * implementations.
 */
public final class WireConnectionDiscoverers {
    private WireConnectionDiscoverers() {
    }

    /**
     * Finds nodes that can connect to this wire node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#findConnections(ServerWorld, GraphView, BlockPos, Node)} implementations.
     *
     * @param self      this node.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param pos       this node's position.
     * @param selfNode  this node's holder.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     * @see BlockNode#findConnections(ServerWorld, GraphView, BlockPos, Node)
     */
    public static @NotNull Collection<Node<NodeHolder>> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                            @NotNull ServerWorld world,
                                                                            @NotNull GraphView graphView,
                                                                            @NotNull BlockPos pos,
                                                                            @NotNull Node<NodeHolder> selfNode,
                                                                            @Nullable SidedWireConnectionFilter filter) {
        Direction side = self.getSide();
        List<Node<NodeHolder>> collector = new ArrayList<>();

        // add all the internal connections
        graphView.getNodesAt(pos).filter(other -> wireCanConnect(self, world, pos, selfNode, other, filter))
            .forEach(collector::add);

        // add all external connections
        for (Direction external : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(pos.offset(external))
                .filter(other -> wireCanConnect(self, world, pos, selfNode, other, filter)).forEach(collector::add);
        }

        // add all corner connections
        BlockPos under = pos.offset(side);
        for (Direction corner : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(under.offset(corner))
                .filter(other -> wireCanConnect(self, world, pos, selfNode, other, filter)).forEach(collector::add);
        }

        // add full-block under connection
        graphView.getNodesAt(under).filter(other -> wireCanConnect(self, world, pos, selfNode, other, filter))
            .forEach(collector::add);

        return collector;
    }

    /**
     * Checks if this wire node can connect to the given node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#canConnect(ServerWorld, GraphView, BlockPos, Node, Node)} implementations.
     *
     * @param self      this node.
     * @param world     the block world to check the connection in.
     * @param pos       this node's position.
     * @param selfNode  this node's holder.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean wireCanConnect(@NotNull SidedWireBlockNode self, @NotNull ServerWorld world,
                                         @NotNull BlockPos pos, @NotNull Node<NodeHolder> selfNode,
                                         @NotNull Node<NodeHolder> otherNode,
                                         @Nullable SidedWireConnectionFilter filter) {
        Direction side = self.getSide();
        BlockPos otherPos = otherNode.data().getPos();
        BlockNode other = otherNode.data().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.fromVector(posDiff);

        if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();

            // check internal connections first
            if (otherPos.equals(pos)) {
                return !otherSide.getAxis().equals(side.getAxis()) && (filter == null ||
                    filter.canConnect(self, world, pos, otherSide, WireConnectionType.INTERNAL, selfNode,
                        otherNode)) &&
                    self.canConnect(world, pos, otherSide, WireConnectionType.INTERNAL, selfNode, otherNode);
            }

            // next check the external connections
            if (posDiffDir != null) {
                return !posDiffDir.getAxis().equals(side.getAxis()) && otherSide.equals(side) && (filter == null ||
                    filter.canConnect(self, world, pos, posDiffDir, WireConnectionType.EXTERNAL, selfNode,
                        otherNode)) &&
                    self.canConnect(world, pos, posDiffDir, WireConnectionType.EXTERNAL, selfNode, otherNode);
            }

            // finally check the corner connections
            BlockPos under = pos.offset(side);
            BlockPos underPosDiff = otherPos.subtract(under);
            Direction underPosDiffDir = Direction.fromVector(underPosDiff);

            if (underPosDiffDir != null) {
                return !underPosDiffDir.getAxis().equals(side.getAxis()) &&
                    otherSide.equals(underPosDiffDir.getOpposite()) && (filter == null ||
                    filter.canConnect(self, world, pos, underPosDiffDir, WireConnectionType.CORNER, selfNode,
                        otherNode)) &&
                    self.canConnect(world, pos, underPosDiffDir, WireConnectionType.CORNER, selfNode, otherNode);
            }

            return false;
        } else if (other instanceof FullWireBlockNode) {
            // implementing external connections here might be useful, but I don't see a use for them right now
            WireConnectionType type = side.equals(posDiffDir) ? WireConnectionType.UNDER : WireConnectionType.EXTERNAL;

            return posDiffDir != null && !posDiffDir.equals(side.getOpposite()) &&
                (filter == null || filter.canConnect(self, world, pos, posDiffDir, type, selfNode, otherNode)) &&
                self.canConnect(world, pos, posDiffDir, type, selfNode, otherNode);
        } else if (other instanceof CenterWireBlockNode) {
            // center-wire connections are only valid if we're both in the same block
            return posDiffDir == null && (filter == null ||
                filter.canConnect(self, world, pos, side.getOpposite(), WireConnectionType.ABOVE, selfNode,
                    otherNode)) &&
                self.canConnect(world, pos, side.getOpposite(), WireConnectionType.ABOVE, selfNode, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self      this node.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param pos       the position of this node.
     * @param selfNode  this node's holder.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<Node<NodeHolder>> fullBlockFindConnections(@NotNull FullWireBlockNode self,
                                                                                 @NotNull ServerWorld world,
                                                                                 @NotNull GraphView graphView,
                                                                                 @NotNull BlockPos pos,
                                                                                 @NotNull Node<NodeHolder> selfNode,
                                                                                 @Nullable FullWireConnectionFilter filter) {
        List<Node<NodeHolder>> collector = new ArrayList<>();

        for (Direction side : Direction.values()) {
            graphView.getNodesAt(pos.offset(side))
                .filter(other -> fullBlockCanConnect(self, world, pos, selfNode, other, filter))
                .forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this full-block node can connect to the given node.
     *
     * @param self      this node.
     * @param world     the block world to check the connection in.
     * @param pos       this node's position.
     * @param selfNode  this node's holder.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean fullBlockCanConnect(@NotNull FullWireBlockNode self, @NotNull ServerWorld world,
                                              @NotNull BlockPos pos, @NotNull Node<NodeHolder> selfNode,
                                              @NotNull Node<NodeHolder> otherNode,
                                              @Nullable FullWireConnectionFilter filter) {
        BlockPos otherPos = otherNode.data().getPos();
        BlockNode other = otherNode.data().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.fromVector(posDiff);

        if (posDiffDir == null) {
            return false;
        }

        if (other instanceof FullWireBlockNode || other instanceof CenterWireBlockNode) {
            return (filter == null || filter.canConnect(self, world, pos, posDiffDir, null, selfNode, otherNode)) &&
                self.canConnect(world, pos, posDiffDir, null, selfNode, otherNode);
        } else if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();
            return !otherSide.equals(posDiffDir) && (filter == null ||
                filter.canConnect(self, world, pos, posDiffDir, otherSide, selfNode, otherNode)) &&
                self.canConnect(world, pos, posDiffDir, otherSide, selfNode, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this center-wire node.
     *
     * @param self      this node.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param pos       the position of this node.
     * @param selfNode  this node's holder.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<Node<NodeHolder>> centerWireFindConnections(
        @NotNull CenterWireBlockNode self, @NotNull ServerWorld world, @NotNull GraphView graphView,
        @NotNull BlockPos pos, @NotNull Node<NodeHolder> selfNode, @Nullable CenterWireConnectionFilter filter) {
        List<Node<NodeHolder>> collector = new ArrayList<>();

        // add internal connections
        graphView.getNodesAt(pos).filter(other -> centerWireCanConnect(self, world, pos, selfNode, other, filter))
            .forEach(collector::add);

        // add external connections
        for (Direction external : Direction.values()) {
            graphView.getNodesAt(pos.offset(external))
                .filter(other -> centerWireCanConnect(self, world, pos, selfNode, other, filter))
                .forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this center-wire node can connect to the given node.
     *
     * @param self      this node.
     * @param world     the block world to check the connection in.
     * @param pos       this node's position.
     * @param selfNode  this node's holder.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean centerWireCanConnect(@NotNull CenterWireBlockNode self, @NotNull ServerWorld world,
                                               @NotNull BlockPos pos, @NotNull Node<NodeHolder> selfNode,
                                               @NotNull Node<NodeHolder> otherNode,
                                               @Nullable CenterWireConnectionFilter filter) {
        BlockPos otherPos = otherNode.data().getPos();
        BlockNode other = otherNode.data().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.fromVector(posDiff);

        if (other instanceof CenterWireBlockNode || other instanceof FullWireBlockNode) {
            return posDiffDir != null &&
                (filter == null || filter.canConnect(self, world, pos, posDiffDir, selfNode, otherNode)) &&
                self.canConnect(world, pos, posDiffDir, selfNode, otherNode);
        } else if (other instanceof SidedWireBlockNode otherSided) {
            Direction otherSide = otherSided.getSide();
            return posDiffDir == null &&
                (filter == null || filter.canConnect(self, world, pos, otherSide, selfNode, otherNode)) &&
                self.canConnect(world, pos, otherSide, selfNode, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }
}
