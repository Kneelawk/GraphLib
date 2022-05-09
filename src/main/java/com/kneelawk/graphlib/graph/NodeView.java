package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.util.math.BlockPos;

import java.util.stream.Stream;

public interface NodeView {
    Stream<Node<BlockNodeWrapper<?>>> getNodesAt(BlockPos pos);

    Stream<Node<BlockNodeWrapper<?>>> getNodesAt(SidedPos pos);
}
