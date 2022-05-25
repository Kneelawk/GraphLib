package com.kneelawk.graphlib;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@SuppressWarnings("unused")
public class GraphLibQuiltMod implements ModInitializer {
    @Override
    public void onInitialize(ModContainer mod) {
        GLLog.setupLogging(QuiltLoader.getGameDir());

        GraphLib.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, integrated, dedicated) -> GraphLib.registerCommands(dispatcher));

        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            try {
                GraphLib.getSimpleController(world).onWorldChunkLoad(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error loading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
            try {
                SimpleBlockGraphController controller = GraphLib.getSimpleController(world);
                controller.saveChunk(chunk.getPos());
                controller.onWorldChunkUnload(chunk.getPos());
            } catch (Exception e) {
                GLLog.error("Error unloading chunk in BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerWorldTickEvents.END.register((server, world) -> {
            try {
                GraphLib.getSimpleController(world).tick();
            } catch (Exception e) {
                GLLog.error("Error ticking BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
        ServerWorldLoadEvents.UNLOAD.register((server, world) -> {
            try {
                GraphLib.getSimpleController(world).close();
            } catch (Exception e) {
                GLLog.error("Error closing BlockGraphController. World: '{}'/{}", world,
                        world.getRegistryKey().getValue(), e);
            }
        });
    }
}
