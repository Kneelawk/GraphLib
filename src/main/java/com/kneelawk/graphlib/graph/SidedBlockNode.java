package com.kneelawk.graphlib.graph;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public interface SidedBlockNode extends BlockNode {
    @NotNull Direction getSide();
}
