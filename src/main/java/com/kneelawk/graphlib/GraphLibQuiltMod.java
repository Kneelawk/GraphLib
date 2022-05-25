package com.kneelawk.graphlib;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@SuppressWarnings("unused")
public class GraphLibQuiltMod implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        GraphLib.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, integrated, dedicated) -> GraphLib.registerCommands(dispatcher));

        ServerChunkEvents.CHUNK_LOAD.register(
                (world, chunk) -> GraphLib.getSimpleController(world).onWorldChunkLoad(chunk.getPos()));
        ServerChunkEvents.CHUNK_UNLOAD.register(
                (world, chunk) -> {
                    SimpleBlockGraphController controller = GraphLib.getSimpleController(world);
                    controller.saveChunk(chunk.getPos());
                    controller.onWorldChunkUnload(chunk.getPos());
                });
        ServerWorldTickEvents.END.register((server, world) -> GraphLib.getSimpleController(world).tick());
        ServerWorldLoadEvents.UNLOAD.register((server, world) -> {
            try {
                GraphLib.getSimpleController(world).close();
            } catch (Exception e) {
                GraphLib.log.error("Error closing BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
    }
}
