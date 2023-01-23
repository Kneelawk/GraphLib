package com.kneelawk.graphlib.client.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.node.client.ClientBlockNode;
import com.kneelawk.graphlib.node.client.SidedClientBlockNode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public record SimpleClientSidedBlockNode(int hash, int classHash, @NotNull Direction side) implements ClientBlockNode, SidedClientBlockNode {
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple_sided");
    }

    @Override
    public @NotNull Direction getSide() {
        return side;
    }
}
