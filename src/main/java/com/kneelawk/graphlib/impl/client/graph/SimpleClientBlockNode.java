package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.node.client.ClientBlockNode;
import com.kneelawk.graphlib.api.node.client.ClientUniqueData;
import com.kneelawk.graphlib.impl.Constants;

public record SimpleClientBlockNode(int hash, int color) implements ClientBlockNode, ClientUniqueData {
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple");
    }

    @Override
    public @NotNull ClientUniqueData getUniqueData() {
        return this;
    }
}
