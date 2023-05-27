package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.user.client.ClientBlockNode;
import com.kneelawk.graphlib.impl.Constants;

public record SimpleClientBlockNode(int hash, int color) implements ClientBlockNode {
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple");
    }
}
