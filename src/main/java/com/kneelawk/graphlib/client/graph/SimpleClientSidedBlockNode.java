package com.kneelawk.graphlib.client.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.SidedClientBlockNode;

public record SimpleClientSidedBlockNode(int hash, int classHash, @NotNull Direction side)
    implements ClientBlockNode, SidedClientBlockNode {
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple_sided");
    }

    @Override
    public @NotNull Direction getSide() {
        return side;
    }
}
