package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import com.kneelawk.graphlib.api.graph.user.client.ClientBlockNode;
import com.kneelawk.graphlib.api.graph.user.client.SidedClientBlockNode;
import com.kneelawk.graphlib.impl.Constants;

public record SimpleClientSidedBlockNode(int hash, int color, @NotNull Direction side)
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
