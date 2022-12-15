package com.kneelawk.graphlib.client.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.client.GraphLibClient;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public record SimpleClientSidedBlockNode(int hash, @NotNull Direction side) implements ClientBlockNode {
    /**
     * Gets the id of the renderer registered with {@link GraphLibClient#BLOCK_NODE_RENDERER}.
     *
     * @return the id of the renderer to use to render this block node.
     */
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple_sided");
    }
}
