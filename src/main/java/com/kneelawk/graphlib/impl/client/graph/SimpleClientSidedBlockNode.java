package com.kneelawk.graphlib.impl.client.graph;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.v1.node.client.SidedClientBlockNode;
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
