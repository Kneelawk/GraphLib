package com.kneelawk.graphlib.graph;

import net.minecraft.util.math.Direction;

public interface SidedBlockNode extends BlockNode {
    Direction getSide();
}
