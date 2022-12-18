package com.kneelawk.graphlib.client.graph;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.graph.ClientBlockNode;
import com.kneelawk.graphlib.graph.SidedClientBlockNode;
import com.kneelawk.graphlib.graph.struct.Graph;
import com.kneelawk.graphlib.graph.struct.Node;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

public record SimpleClientSidedBlockNode(int hash, int classHash, @NotNull Direction side)
    implements ClientBlockNode, SidedClientBlockNode {
    @Override
    public @NotNull Identifier getRenderId() {
        return Constants.id("simple_sided");
    }

    @Override
    public @NotNull Direction getSide() {
        return side;
    }

    @Override
    public @NotNull Identifier getInspectionId() {
        return Constants.id("simple");
    }

    @Override
    public void toInspectionPacket(@NotNull Node<ClientBlockNodeHolder> holderNode,
                                   @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull PacketByteBuf buf) {
        buf.writeByte(1);
        buf.writeByte(side.getId());

        buf.writeInt(hash);
        buf.writeInt(classHash);
    }
}
