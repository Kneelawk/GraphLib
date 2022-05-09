package com.kneelawk.graphlib.wire;

import com.kneelawk.graphlib.graph.BlockNodeWrapper;
import com.kneelawk.graphlib.graph.NodeView;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.DirectionUtils;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Contains wire connection finder and checker implementations for use in {@link com.kneelawk.graphlib.graph.BlockNode}
 * implementations.
 */
public final class WireConnectionDiscoverers {
    private WireConnectionDiscoverers() {
    }

    /**
     * Finds nodes that can connect to this wire node.
     * <p>
     * This is intended for use in
     * {@link com.kneelawk.graphlib.graph.BlockNode#findConnections(ServerWorld, NodeView, BlockPos)} implementations.
     *
     * @param self     this node.
     * @param world    the block world to find connections in.
     * @param nodeView the node world to find connections in.
     * @param pos      this node's position.
     * @return a collection of nodes this node can connect to.
     * @see com.kneelawk.graphlib.graph.BlockNode#findConnections(ServerWorld, NodeView, BlockPos)
     */
    public static @NotNull Collection<Node<BlockNodeWrapper<?>>> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                                     @NotNull ServerWorld world,
                                                                                     @NotNull NodeView nodeView,
                                                                                     @NotNull BlockPos pos) {
        Direction side = self.getSide();
        List<Node<BlockNodeWrapper<?>>> collector = new ArrayList<>();

        // add all the internal connections
        nodeView.getNodesAt(pos).filter(other -> wireCanConnect(self, world, pos, other)).forEach(collector::add);

        // add all external connections
        for (Direction external : DirectionUtils.perpendiculars(side)) {
            nodeView.getNodesAt(pos.offset(external)).filter(other -> wireCanConnect(self, world, pos, other))
                    .forEach(collector::add);
        }

        // add all corner connections
        BlockPos under = pos.offset(side);
        for (Direction corner : DirectionUtils.perpendiculars(side)) {
            nodeView.getNodesAt(under.offset(corner)).filter(other -> wireCanConnect(self, world, pos, other))
                    .forEach(collector::add);
        }

        // add full-block under connection
        nodeView.getNodesAt(under).filter(other -> wireCanConnect(self, world, pos, other)).forEach(collector::add);

        return collector;
    }

    /**
     * Checks if this wire node can connect to the given node.
     * <p>
     * This is intended for use in
     * {@link com.kneelawk.graphlib.graph.BlockNode#canConnect(ServerWorld, NodeView, BlockPos, Node)} implementations.
     *
     * @param self  this node.
     * @param world the block world to check the connection in.
     * @param pos   this node's position.
     * @param other the node that this node could potentially connect to.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean wireCanConnect(@NotNull SidedWireBlockNode self, @NotNull ServerWorld world,
                                         @NotNull BlockPos pos, @NotNull Node<BlockNodeWrapper<?>> other) {
        Direction side = self.getSide();
        BlockPos otherPos = other.data().pos();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.fromVector(posDiff);

        if (other.data().node() instanceof SidedWireBlockNode otherNode) {
            Direction otherSide = otherNode.getSide();

            // check internal connections first
            if (otherPos.equals(pos)) {
                return !otherSide.getAxis().equals(side.getAxis()) &&
                        self.canConnect(world, pos, otherSide, WireConnectionType.INTERNAL);
            }

            // next check the external connections
            if (posDiffDir != null) {
                return !posDiffDir.getAxis().equals(side.getAxis()) && otherSide.equals(side) &&
                        self.canConnect(world, pos, posDiffDir, WireConnectionType.EXTERNAL);
            }

            // finally check the corner connections
            BlockPos under = pos.offset(side);
            BlockPos underPosDiff = otherPos.subtract(under);
            Direction underPosDiffDir = Direction.fromVector(underPosDiff);

            if (underPosDiffDir != null) {
                return !underPosDiffDir.getAxis().equals(side.getAxis()) &&
                        otherSide.equals(underPosDiffDir.getOpposite()) &&
                        self.canConnect(world, pos, underPosDiffDir, WireConnectionType.CORNER);
            }

            return false;
        } else if (other.data().node() instanceof FullWireBlockNode) {
            // implementing external connections here might be useful, but I don't see a use for them right now

            return posDiffDir != null && !posDiffDir.equals(side.getOpposite()) &&
                    self.canConnect(world, pos, posDiffDir,
                            posDiffDir.equals(side) ? WireConnectionType.UNDER : WireConnectionType.EXTERNAL);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self     this node.
     * @param world    the block world to find connections in.
     * @param nodeView the node world to find connections in.
     * @param pos      the position of this node.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<Node<BlockNodeWrapper<?>>> fullBlockFindConnections(
            @NotNull FullWireBlockNode self,
            @NotNull ServerWorld world,
            @NotNull NodeView nodeView,
            @NotNull BlockPos pos) {
        List<Node<BlockNodeWrapper<?>>> collector = new ArrayList<>();

        for (Direction side : Direction.values()) {
            nodeView.getNodesAt(pos.offset(side)).filter(other -> fullBlockCanConnect(self, world, pos, other))
                    .forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this full-block node can connect to the given node.
     *
     * @param self  this node.
     * @param world the block world to check the connection in.
     * @param pos   this node's position.
     * @param other the node that this node could potentially connect to.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean fullBlockCanConnect(@NotNull FullWireBlockNode self, @NotNull ServerWorld world,
                                              @NotNull BlockPos pos, @NotNull Node<BlockNodeWrapper<?>> other) {
        BlockPos otherPos = other.data().pos();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.fromVector(posDiff);

        if (posDiffDir == null) {
            return false;
        }

        if (other.data().node() instanceof FullWireBlockNode) {
            return self.canConnect(world, pos, posDiffDir, null);
        } else if (other.data().node() instanceof SidedWireBlockNode otherNode) {
            Direction otherSide = otherNode.getSide();
            return !otherSide.equals(posDiffDir) && self.canConnect(world, pos, posDiffDir, otherSide);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes and FullWireBlockNodes for now
            return false;
        }
    }
}
