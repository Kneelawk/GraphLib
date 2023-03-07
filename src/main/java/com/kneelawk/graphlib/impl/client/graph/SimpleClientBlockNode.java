package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.client.GraphLibClient;
import com.kneelawk.graphlib.api.v1.node.client.ClientBlockNode;
import com.kneelawk.graphlib.impl.Constants;

public record SimpleClientBlockNode(int hash, int classHash) implements ClientBlockNode {
    /**
     * Gets the id of the renderer registered with {@link GraphLibClient#BLOCK_NODE_RENDERER}.
     *
     * @return the id of the renderer to use to render this block node.
     */
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple");
    }
}
