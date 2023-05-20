package com.kneelawk.graphlib.api.wire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.node.BlockNode;
import com.kneelawk.graphlib.api.util.DirectionUtils;

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
     * {@link BlockNode#findConnections()} implementations.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     * @see BlockNode#findConnections()
     */
    public static @NotNull Collection<NodeHolder<BlockNode>> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                                 @NotNull NodeHolder<BlockNode> selfNode,
                                                                                 @NotNull ServerWorld world,
                                                                                 @NotNull GraphView graphView,
                                                                                 @Nullable SidedWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        Direction side = self.getSide();
        List<NodeHolder<BlockNode>> collector = new ArrayList<>();

        // add all the internal connections
        graphView.getNodesAt(pos).filter(other -> wireCanConnect(self, selfNode, world, other, filter))
            .forEach(collector::add);

        // add all external connections
        for (Direction external : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(pos.offset(external))
                .filter(other -> wireCanConnect(self, selfNode, world, other, filter)).forEach(collector::add);
        }

        // add all corner connections
        BlockPos under = pos.offset(side);
        for (Direction corner : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(under.offset(corner))
                .filter(other -> wireCanConnect(self, selfNode, world, other, filter)).forEach(collector::add);
        }

        // add full-block under connection
        graphView.getNodesAt(under).filter(other -> wireCanConnect(self, selfNode, world, other, filter))
            .forEach(collector::add);

        return collector;
    }

    /**
     * Checks if this wire node can connect to the given node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#canConnect(NodeHolder)} implementations.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to check the connection in.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean wireCanConnect(@NotNull SidedWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                                         @NotNull ServerWorld world, @NotNull NodeHolder<BlockNode> otherNode,
                                         @Nullable SidedWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        Direction side = self.getSide();
        BlockPos otherPos = otherNode.getPos();
        BlockNode other = otherNode.getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();

            // check internal connections first
            if (otherPos.equals(pos)) {
                return !otherSide.getAxis().equals(side.getAxis()) && (filter == null ||
                    filter.canConnect(self, selfNode, world, otherSide, WireConnectionType.INTERNAL, otherNode)) &&
                    self.canConnect(selfNode, world, otherSide, WireConnectionType.INTERNAL, otherNode);
            }

            // next check the external connections
            if (posDiffDir != null) {
                return !posDiffDir.getAxis().equals(side.getAxis()) && otherSide.equals(side) && (filter == null ||
                    filter.canConnect(self, selfNode, world, posDiffDir, WireConnectionType.EXTERNAL, otherNode)) &&
                    self.canConnect(selfNode, world, posDiffDir, WireConnectionType.EXTERNAL, otherNode);
            }

            // finally check the corner connections
            BlockPos under = pos.offset(side);
            BlockPos underPosDiff = otherPos.subtract(under);
            Direction underPosDiffDir =
                Direction.method_50026(underPosDiff.getX(), underPosDiff.getY(), underPosDiff.getZ());

            if (underPosDiffDir != null) {
                return !underPosDiffDir.getAxis().equals(side.getAxis()) &&
                    otherSide.equals(underPosDiffDir.getOpposite()) && (filter == null ||
                    filter.canConnect(self, selfNode, world, underPosDiffDir, WireConnectionType.CORNER, otherNode)) &&
                    self.canConnect(selfNode, world, underPosDiffDir, WireConnectionType.CORNER, otherNode);
            }

            return false;
        } else if (other instanceof FullWireBlockNode) {
            // implementing external connections here might be useful, but I don't see a use for them right now
            WireConnectionType type = side.equals(posDiffDir) ? WireConnectionType.UNDER : WireConnectionType.EXTERNAL;

            return posDiffDir != null && !posDiffDir.equals(side.getOpposite()) &&
                (filter == null || filter.canConnect(self, selfNode, world, posDiffDir, type, otherNode)) &&
                self.canConnect(selfNode, world, posDiffDir, type, otherNode);
        } else if (other instanceof CenterWireBlockNode) {
            // center-wire connections are only valid if we're both in the same block
            return posDiffDir == null && (filter == null ||
                filter.canConnect(self, selfNode, world, side.getOpposite(), WireConnectionType.ABOVE, otherNode)) &&
                self.canConnect(selfNode, world, side.getOpposite(), WireConnectionType.ABOVE, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<NodeHolder<BlockNode>> fullBlockFindConnections(@NotNull FullWireBlockNode self,
                                                                                      @NotNull NodeHolder<BlockNode> selfNode,
                                                                                      @NotNull ServerWorld world,
                                                                                      @NotNull GraphView graphView,
                                                                                      @Nullable FullWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        List<NodeHolder<BlockNode>> collector = new ArrayList<>();

        for (Direction side : Direction.values()) {
            graphView.getNodesAt(pos.offset(side))
                .filter(other -> fullBlockCanConnect(self, selfNode, world, other, filter)).forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this full-block node can connect to the given node.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to check the connection in.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean fullBlockCanConnect(@NotNull FullWireBlockNode self, @NotNull NodeHolder<BlockNode> selfNode,
                                              @NotNull ServerWorld world, @NotNull NodeHolder<BlockNode> otherNode,
                                              @Nullable FullWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        BlockPos otherPos = otherNode.getPos();
        BlockNode other = otherNode.getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (posDiffDir == null) {
            return false;
        }

        if (other instanceof FullWireBlockNode || other instanceof CenterWireBlockNode) {
            return (filter == null || filter.canConnect(self, selfNode, world, posDiffDir, null, otherNode)) &&
                self.canConnect(selfNode, world, posDiffDir, null, otherNode);
        } else if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();
            return !otherSide.equals(posDiffDir) &&
                (filter == null || filter.canConnect(self, selfNode, world, posDiffDir, otherSide, otherNode)) &&
                self.canConnect(selfNode, world, posDiffDir, otherSide, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this center-wire node.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to find connections in.
     * @param graphView the node world to find connections in.
     * @param filter    a general connection filter, used to filter connections.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<NodeHolder<BlockNode>> centerWireFindConnections(
        @NotNull CenterWireBlockNode self,
        @NotNull NodeHolder<BlockNode> selfNode,
        @NotNull ServerWorld world,
        @NotNull GraphView graphView,
        @Nullable CenterWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        List<NodeHolder<BlockNode>> collector = new ArrayList<>();

        // add internal connections
        graphView.getNodesAt(pos).filter(other -> centerWireCanConnect(self, selfNode, world, other, filter))
            .forEach(collector::add);

        // add external connections
        for (Direction external : Direction.values()) {
            graphView.getNodesAt(pos.offset(external))
                .filter(other -> centerWireCanConnect(self, selfNode, world, other, filter))
                .forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this center-wire node can connect to the given node.
     *
     * @param self      this node.
     * @param selfNode  this node's holder.
     * @param world     the block world to check the connection in.
     * @param otherNode the node that this node could potentially connect to.
     * @param filter    a general connection filter, used to filter connections.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean centerWireCanConnect(@NotNull CenterWireBlockNode self,
                                               @NotNull NodeHolder<BlockNode> selfNode,
                                               @NotNull ServerWorld world,
                                               @NotNull NodeHolder<BlockNode> otherNode,
                                               @Nullable CenterWireConnectionFilter filter) {
        BlockPos pos = selfNode.getPos();
        BlockPos otherPos = otherNode.getPos();
        BlockNode other = otherNode.getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (other instanceof CenterWireBlockNode || other instanceof FullWireBlockNode) {
            return posDiffDir != null &&
                (filter == null || filter.canConnect(self, selfNode, world, posDiffDir, otherNode)) &&
                self.canConnect(selfNode, world, posDiffDir, otherNode);
        } else if (other instanceof SidedWireBlockNode otherSided) {
            Direction otherSide = otherSided.getSide();
            return posDiffDir == null &&
                (filter == null || filter.canConnect(self, selfNode, world, otherSide, otherNode)) &&
                self.canConnect(selfNode, world, otherSide, otherNode);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }
}
