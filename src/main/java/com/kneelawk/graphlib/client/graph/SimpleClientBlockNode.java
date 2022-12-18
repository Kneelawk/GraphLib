package com.kneelawk.graphlib.client.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.client.GraphLibClient;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Graph;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public @NotNull Identifier getInspectionId() {
        return Constants.id("simple");
    }

    @Override
    public void toInspectionPacket(@NotNull Node<ClientBlockNodeHolder> holderNode, @NotNull Graph<ClientBlockNodeHolder> graph,
                                   @NotNull PacketByteBuf buf) {
        buf.writeByte(0);

        buf.writeInt(hash);
        buf.writeInt(classHash);
    }
}
