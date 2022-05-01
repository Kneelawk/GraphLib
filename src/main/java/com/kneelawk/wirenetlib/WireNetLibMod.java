package com.kneelawk.wirenetlib;

import com.kneelawk.wirenetlib.mixin.api.StorageHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WireNetLibMod implements ModInitializer {
    public static Logger log = LoggerFactory.getLogger(Constants.MOD_ID);

    @Override
    public void onInitialize() {
        ServerChunkEvents.CHUNK_LOAD.register(
                (world, chunk) -> StorageHelper.getController(world).onWorldChunkLoad(chunk.getPos()));
        ServerChunkEvents.CHUNK_UNLOAD.register(
                (world, chunk) -> StorageHelper.getController(world).onWorldChunkUnload(chunk.getPos()));
        ServerWorldTickEvents.END.register((server, world) -> StorageHelper.getController(world).tick());
    }
}
