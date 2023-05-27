package com.kneelawk.graphlib.api.graph.user.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.client.GraphLibClient;
import com.kneelawk.graphlib.api.client.render.BlockNodeRenderer;
import com.kneelawk.graphlib.api.graph.user.BlockNode;

/**
 * Implemented by any representation of a {@link BlockNode} on the client.
 * <p>
 * This could theoretically be implemented by the same class implementing {@link BlockNode}, as care has been taken to
 * make sure this interface does not depend on anything strictly client-sided, but that would likely be overkill for
 * most situations.
 */
public interface ClientBlockNode {
    /**
     * Gets the id of the renderer registered with
     * {@link GraphLibClient#registerRenderer(Identifier, Identifier, Class, BlockNodeRenderer)}.
     *
     * @return the id of the renderer to use to render this block node.
     */
    @NotNull Identifier getRenderId();
}
