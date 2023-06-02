package com.kneelawk.graphlib.api.wire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.util.DirectionUtils;
import com.kneelawk.graphlib.api.util.EmptyLinkKey;
import com.kneelawk.graphlib.api.util.HalfLink;

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
     * {@link BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)} implementations.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @return a collection of nodes this node can connect to.
     * @see BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)
     */
    public static @NotNull Collection<HalfLink> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                    @NotNull NodeContext ctx) {
        return wireFindConnections(self, ctx, null, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this wire node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)} implementations.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return a collection of nodes this node can connect to.
     * @see BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)
     */
    public static @NotNull Collection<HalfLink> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                    @NotNull NodeContext ctx,
                                                                    @Nullable SidedWireConnectionFilter filter) {
        return wireFindConnections(self, ctx, filter, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this wire node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)} implementations.
     *
     * @param self       this node.
     * @param ctx        the node context for the given node.
     * @param filter     a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @param keyFactory a link key factory used for creating all the keys for the connections returned.
     * @return a collection of nodes this node can connect to.
     * @see BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeContext)
     */
    public static @NotNull Collection<HalfLink> wireFindConnections(@NotNull SidedWireBlockNode self,
                                                                    @NotNull NodeContext ctx,
                                                                    @Nullable SidedWireConnectionFilter filter,
                                                                    @NotNull LinkKeyFactory keyFactory) {
        GraphView graphView = ctx.graphWorld();
        BlockPos pos = ctx.getPos();
        Direction side = self.getSide();
        List<HalfLink> collector = new ArrayList<>();

        // add all the internal connections
        graphView.getNodesAt(pos).map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
            .filter(link -> wireCanConnect(self, ctx, link, filter)).forEach(collector::add);

        // add all external connections
        for (Direction external : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(pos.offset(external))
                .map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
                .filter(link -> wireCanConnect(self, ctx, link, filter)).forEach(collector::add);
        }

        // add all corner connections
        BlockPos under = pos.offset(side);
        for (Direction corner : DirectionUtils.perpendiculars(side)) {
            graphView.getNodesAt(under.offset(corner))
                .map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
                .filter(link -> wireCanConnect(self, ctx, link, filter)).forEach(collector::add);
        }

        // add full-block under connection
        graphView.getNodesAt(under).map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
            .filter(link -> wireCanConnect(self, ctx, link, filter)).forEach(collector::add);

        return collector;
    }

    /**
     * Checks if this wire node can connect to the given node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#canConnect(com.kneelawk.graphlib.api.graph.NodeContext, com.kneelawk.graphlib.api.util.HalfLink)} implementations.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @param link the link that this node could potentially form.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean wireCanConnect(@NotNull SidedWireBlockNode self, @NotNull NodeContext ctx,
                                         @NotNull HalfLink link) {
        return wireCanConnect(self, ctx, link, null);
    }

    /**
     * Checks if this wire node can connect to the given node.
     * <p>
     * This is intended for use in
     * {@link BlockNode#canConnect(com.kneelawk.graphlib.api.graph.NodeContext, com.kneelawk.graphlib.api.util.HalfLink)} implementations.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param link   the link that this node could potentially form.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean wireCanConnect(@NotNull SidedWireBlockNode self, @NotNull NodeContext ctx,
                                         @NotNull HalfLink link, @Nullable SidedWireConnectionFilter filter) {
        BlockPos pos = ctx.getPos();
        Direction side = self.getSide();
        BlockPos otherPos = link.other().getPos();
        BlockNode other = link.other().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();

            // check internal connections first
            if (otherPos.equals(pos)) {
                return !otherSide.getAxis().equals(side.getAxis()) &&
                    (filter == null || filter.canConnect(self, ctx, otherSide, WireConnectionType.INTERNAL, link)) &&
                    self.canConnect(ctx, otherSide, WireConnectionType.INTERNAL, link);
            }

            // next check the external connections
            if (posDiffDir != null) {
                return !posDiffDir.getAxis().equals(side.getAxis()) && otherSide.equals(side) &&
                    (filter == null || filter.canConnect(self, ctx, posDiffDir, WireConnectionType.EXTERNAL, link)) &&
                    self.canConnect(ctx, posDiffDir, WireConnectionType.EXTERNAL, link);
            }

            // finally check the corner connections
            BlockPos under = pos.offset(side);
            BlockPos underPosDiff = otherPos.subtract(under);
            Direction underPosDiffDir =
                Direction.method_50026(underPosDiff.getX(), underPosDiff.getY(), underPosDiff.getZ());

            if (underPosDiffDir != null) {
                return !underPosDiffDir.getAxis().equals(side.getAxis()) &&
                    otherSide.equals(underPosDiffDir.getOpposite()) && (filter == null ||
                    filter.canConnect(self, ctx, underPosDiffDir, WireConnectionType.CORNER, link)) &&
                    self.canConnect(ctx, underPosDiffDir, WireConnectionType.CORNER, link);
            }

            return false;
        } else if (other instanceof FullWireBlockNode) {
            // implementing external connections here might be useful, but I don't see a use for them right now
            WireConnectionType type = side.equals(posDiffDir) ? WireConnectionType.UNDER : WireConnectionType.EXTERNAL;

            return posDiffDir != null && !posDiffDir.equals(side.getOpposite()) &&
                (filter == null || filter.canConnect(self, ctx, posDiffDir, type, link)) &&
                self.canConnect(ctx, posDiffDir, type, link);
        } else if (other instanceof CenterWireBlockNode) {
            // center-wire connections are only valid if we're both in the same block
            return pos.equals(otherPos) &&
                (filter == null || filter.canConnect(self, ctx, side.getOpposite(), WireConnectionType.ABOVE, link)) &&
                self.canConnect(ctx, side.getOpposite(), WireConnectionType.ABOVE, link);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> fullBlockFindConnections(@NotNull FullWireBlockNode self,
                                                                         @NotNull NodeContext ctx) {
        return fullBlockFindConnections(self, ctx, null, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> fullBlockFindConnections(@NotNull FullWireBlockNode self,
                                                                         @NotNull NodeContext ctx,
                                                                         @Nullable FullWireConnectionFilter filter) {
        return fullBlockFindConnections(self, ctx, filter, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this full-block node.
     *
     * @param self       this node.
     * @param ctx        the node context for the given node.
     * @param filter     a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @param keyFactory a link key factory used for creating all the keys for the connections returned.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> fullBlockFindConnections(@NotNull FullWireBlockNode self,
                                                                         @NotNull NodeContext ctx,
                                                                         @Nullable FullWireConnectionFilter filter,
                                                                         @NotNull LinkKeyFactory keyFactory) {
        GraphView graphView = ctx.graphWorld();
        BlockPos pos = ctx.getPos();
        List<HalfLink> collector = new ArrayList<>();

        for (Direction side : Direction.values()) {
            graphView.getNodesAt(pos.offset(side))
                .map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
                .filter(link -> fullBlockCanConnect(self, ctx, link, filter)).forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this full-block node can connect to the given node.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @param link the link that this node could potentially form.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean fullBlockCanConnect(@NotNull FullWireBlockNode self, @NotNull NodeContext ctx,
                                              @NotNull HalfLink link) {
        return fullBlockCanConnect(self, ctx, link, null);
    }

    /**
     * Checks if this full-block node can connect to the given node.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param link   the link that this node could potentially form.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean fullBlockCanConnect(@NotNull FullWireBlockNode self, @NotNull NodeContext ctx,
                                              @NotNull HalfLink link, @Nullable FullWireConnectionFilter filter) {
        BlockPos pos = ctx.getPos();
        BlockPos otherPos = link.other().getPos();
        BlockNode other = link.other().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (posDiffDir == null) {
            return false;
        }

        if (other instanceof FullWireBlockNode || other instanceof CenterWireBlockNode) {
            return (filter == null || filter.canConnect(self, ctx, posDiffDir, null, link)) &&
                self.canConnect(ctx, posDiffDir, null, link);
        } else if (other instanceof SidedWireBlockNode otherSidedNode) {
            Direction otherSide = otherSidedNode.getSide();
            return !otherSide.equals(posDiffDir) &&
                (filter == null || filter.canConnect(self, ctx, posDiffDir, otherSide, link)) &&
                self.canConnect(ctx, posDiffDir, otherSide, link);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }

    /**
     * Finds nodes that can connect to this center-wire node.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> centerWireFindConnections(@NotNull CenterWireBlockNode self,
                                                                          @NotNull NodeContext ctx) {
        return centerWireFindConnections(self, ctx, null, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this center-wire node.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> centerWireFindConnections(@NotNull CenterWireBlockNode self,
                                                                          @NotNull NodeContext ctx,
                                                                          @Nullable CenterWireConnectionFilter filter) {
        return centerWireFindConnections(self, ctx, filter, EmptyLinkKey.FACTORY);
    }

    /**
     * Finds nodes that can connect to this center-wire node.
     *
     * @param self       this node.
     * @param ctx        the node context for the given node.
     * @param filter     a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @param keyFactory a link key factory used for creating all the keys for the connections returned.
     * @return a collection of nodes this node can connect to.
     */
    public static @NotNull Collection<HalfLink> centerWireFindConnections(@NotNull CenterWireBlockNode self,
                                                                          @NotNull NodeContext ctx,
                                                                          @Nullable CenterWireConnectionFilter filter,
                                                                          @NotNull LinkKeyFactory keyFactory) {
        GraphView graphView = ctx.graphWorld();
        BlockPos pos = ctx.getPos();
        List<HalfLink> collector = new ArrayList<>();

        // add internal connections
        graphView.getNodesAt(pos).map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
            .filter(link -> centerWireCanConnect(self, ctx, link, filter)).forEach(collector::add);

        // add external connections
        for (Direction external : Direction.values()) {
            graphView.getNodesAt(pos.offset(external))
                .map(other -> new HalfLink(keyFactory.createLinkKey(ctx, other), other))
                .filter(link -> centerWireCanConnect(self, ctx, link, filter)).forEach(collector::add);
        }

        return collector;
    }

    /**
     * Checks if this center-wire node can connect to the given node.
     *
     * @param self this node.
     * @param ctx  the node context for the given node.
     * @param link the link that this node could potentially form.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean centerWireCanConnect(@NotNull CenterWireBlockNode self, @NotNull NodeContext ctx,
                                               @NotNull HalfLink link) {
        return centerWireCanConnect(self, ctx, link, null);
    }

    /**
     * Checks if this center-wire node can connect to the given node.
     *
     * @param self   this node.
     * @param ctx    the node context for the given node.
     * @param link   the link that this node could potentially form.
     * @param filter a general connection filter, used to filter connections, or <code>null</code> if no filter is needed.
     * @return <code>true</code> if this node can connect to the given node.
     */
    public static boolean centerWireCanConnect(@NotNull CenterWireBlockNode self, @NotNull NodeContext ctx,
                                               @NotNull HalfLink link, @Nullable CenterWireConnectionFilter filter) {
        BlockPos pos = ctx.getPos();
        BlockPos otherPos = link.other().getPos();
        BlockNode other = link.other().getNode();

        BlockPos posDiff = otherPos.subtract(pos);
        Direction posDiffDir = Direction.method_50026(posDiff.getX(), posDiff.getY(), posDiff.getZ());

        if (other instanceof CenterWireBlockNode || other instanceof FullWireBlockNode) {
            return posDiffDir != null && (filter == null || filter.canConnect(self, ctx, posDiffDir, link)) &&
                self.canConnect(ctx, posDiffDir, link);
        } else if (other instanceof SidedWireBlockNode otherSided) {
            Direction otherSide = otherSided.getSide();
            return pos.equals(otherPos) && (filter == null || filter.canConnect(self, ctx, otherSide, link)) &&
                self.canConnect(ctx, otherSide, link);
        } else {
            // we only know how to handle connections to SidedWireBlockNodes, CenterWireBlockNodes, and FullWireBlockNodes for now
            return false;
        }
    }
}
