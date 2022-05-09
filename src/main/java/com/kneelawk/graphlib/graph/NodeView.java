package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public interface NodeView {
    @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull BlockPos pos);

    @NotNull Stream<Node<BlockNodeWrapper<?>>> getNodesAt(@NotNull SidedPos pos);
}
