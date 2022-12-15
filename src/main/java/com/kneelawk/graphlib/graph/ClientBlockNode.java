package com.kneelawk.graphlib.graph;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * Implemented by any representation of a {@link BlockNode} on the client.
 * <p>
 * This could theoretically be implemented by the same class implementing {@link BlockNode}, as care has been taken to
 * make sure this interface does not depend on anything strictly client-sided, but that would likely be overkill for
 * most situations.
 */
public interface ClientBlockNode {
    /**
     * Gets the id of the renderer registered with {@link com.kneelawk.graphlib.client.GraphLibClient#BLOCK_NODE_RENDERER}.
     *
     * @return the id of the renderer to use to render this block node.
     */
    @NotNull Identifier getRenderId();
}
