package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kneelawk.graphlib.v3.api.graph.user.GraphNode;
import com.kneelawk.graphlib.v3.api.pos.DimensionRef;
import com.kneelawk.graphlib.v3.api.pos.NodePos;

/**
 * Holds a graph node and all associated information.
 */
public interface NodeHolder {
    /**
     * {@return the node pos}
     */
    NodePos pos();

    /**
     * {@return the graph view that this node holder is from}
     */
    GraphView graphView();

    /**
     * {@return the current graph id of this node}
     */
    long graphId();

    /**
     * {@return the graph node within the node pos}
     */
    default GraphNode node() {
        return pos().node();
    }

    /**
     * {@return the dimension reference associated with the node, if any}
     */
    @Nullable
    default DimensionRef dimensionRef() {
        return pos().dimension();
    }

    /**
     * {@return the level associated with this node, if any}
     */
    @Nullable
    default Level level() {
        DimensionRef ref = dimensionRef();
        if (ref == null) return null;
        return graphView().getUniverse().getLevel(ref);
    }

    /**
     * {@return the block pos associated with this node, if any}
     */
    @Nullable
    default BlockPos blockPos() {
        return pos().blockPos();
    }

    /**
     * {@return the block state at the position of this node, if this node even has a block position}
     */
    @Nullable
    default BlockState blockState() {
        Level level = level();
        if (level == null) return null;
        BlockPos pos = blockPos();
        if (pos == null) return null;
        return level.getBlockState(pos);
    }

    /**
     * {@return the block entity at the position of this node, if any}
     */
    @Nullable
    default BlockEntity blockEntity() {
        Level level = level();
        if (level == null) return null;
        BlockPos pos = blockPos();
        if (pos == null) return null;
        return level.getBlockEntity(pos);
    }
}
