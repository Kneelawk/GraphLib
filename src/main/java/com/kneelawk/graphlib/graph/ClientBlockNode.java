package com.kneelawk.graphlib.graph;

import com.kneelawk.graphlib.client.graph.ClientBlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Graph;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
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

    /**
     * Gets the id for packet encoders and decoders used when requesting extra information on a block node.
     * <p>
     * Note this is the handler registered with
     * {@link com.kneelawk.graphlib.GraphLib#BLOCK_NODE_INSPECTION_PACKET_HANDLER} and
     * {@link com.kneelawk.graphlib.client.GraphLibClient#BLOCK_NODE_INSPECTION_PACKET_DECODER}.
     *
     * @return the id of the inspection handler to use to look up a node's details on the server.
     */
    @NotNull Identifier getInspectionId();

    /**
     * Writes the necessary information to allow the server to look up the server-sided block-node that this client
     * block node is associated with.
     *
     * @param holderNode the graph-node for this node, for ease of getting connections or position information.
     * @param graph      the graph this node is part of.
     * @param buf        the buffer to write the identifying information to.
     */
    void toInspectionPacket(@NotNull Node<ClientBlockNodeHolder> holderNode,
                            @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull PacketByteBuf buf);
}
