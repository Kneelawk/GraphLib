package com.kneelawk.graphlib.impl.graph.simple;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.GraphWorldImpl;
import com.kneelawk.graphlib.impl.mixin.api.StorageHelper;

public class SimpleGraphUniverse implements GraphUniverse, GraphUniverseImpl {
    private final Identifier id;

    public SimpleGraphUniverse(Identifier universeId, SimpleGraphUniverseBuilder builder) {
        this.id = universeId;
    }

    @Override
    public @NotNull GraphWorldImpl getGraphWorld(@NotNull ServerWorld world) {
        return StorageHelper.getStorage(world).get(id);
    }

    @Override
    public @NotNull Identifier getId() {
        return id;
    }

    @Override
    public GraphWorldImpl createGraphWorld(ServerWorld world, Path path, boolean syncChunkWrites) {
        return new SimpleGraphWorld(this, world, path, syncChunkWrites);
    }
}
