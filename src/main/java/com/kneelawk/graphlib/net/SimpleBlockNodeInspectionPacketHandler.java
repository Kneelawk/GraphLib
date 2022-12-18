package com.kneelawk.graphlib.net;

import com.kneelawk.graphlib.graph.BlockGraphController;
import com.kneelawk.graphlib.graph.BlockNode;
import com.kneelawk.graphlib.graph.BlockNodeHolder;
import com.kneelawk.graphlib.graph.struct.Node;
import com.kneelawk.graphlib.util.SidedPos;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

public final class SimpleBlockNodeInspectionPacketHandler implements BlockNodeInspectionPacketHandler {
    public static final SimpleBlockNodeInspectionPacketHandler INSTANCE = new SimpleBlockNodeInspectionPacketHandler();

    private SimpleBlockNodeInspectionPacketHandler() {
    }

    @Override
    public void inspect(@NotNull ServerWorld world, @NotNull BlockGraphController controller, @NotNull BlockPos pos,
                        @NotNull PacketByteBuf inBuf, @NotNull PacketByteBuf outBuf) {
        Stream<Node<BlockNodeHolder>> nodeStream = switch (inBuf.readByte()) {
            case 0 -> controller.getNodesAt(pos);
            case 1 -> controller.getNodesAt(new SidedPos(pos, Direction.byId(inBuf.readByte())));
            default -> null;
        };

        if (nodeStream == null) {
            // write error code
            outBuf.writeByte(1);
            return;
        }

        int hash = inBuf.readInt();
        int classHash = inBuf.readInt();

        Optional<Node<BlockNodeHolder>> maybeNode = nodeStream.filter(holderNode -> {
            BlockNode node = holderNode.data().getNode();
            return node.hashCode() == hash && node.getClass().getName().hashCode() == classHash;
        }).findFirst();

        if (maybeNode.isPresent()) {
            Node<BlockNodeHolder> holderNode = maybeNode.get();
            BlockNode node = holderNode.data().getNode();

            // write success code
            outBuf.writeByte(0);

            // Send details packet back
            outBuf.writeString(node.getClass().getName());
            outBuf.writeString(node.toString());
        } else {
            // write error code
            outBuf.writeByte(1);
        }
    }
}
