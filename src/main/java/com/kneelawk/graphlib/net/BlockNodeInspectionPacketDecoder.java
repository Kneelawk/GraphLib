package com.kneelawk.graphlib.net;

import com.kneelawk.graphlib.graph.BlockGraphController;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Decodes inspection data for use in rendering.
 * <p>
 * This decodes the packet sent by
 * {@link BlockNodeInspectionPacketHandler#inspect(ServerWorld, BlockGraphController, BlockPos, PacketByteBuf, PacketByteBuf)}.
 */
public interface BlockNodeInspectionPacketDecoder {
    /**
     * Decodes the packet sent containing the extra node inspection data.
     *
     * @param buf the buffer containing the extra inspection data.
     * @return an object to be passed to the inspection data gui renderer.
     */
    @Nullable Object decodeInspectedData(PacketByteBuf buf);
}
