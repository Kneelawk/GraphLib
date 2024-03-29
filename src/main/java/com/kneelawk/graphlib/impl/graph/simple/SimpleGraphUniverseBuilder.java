package com.kneelawk.graphlib.impl.graph.simple;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.api.world.SaveMode;
import com.kneelawk.graphlib.impl.GraphLibImpl;

public class SimpleGraphUniverseBuilder implements GraphUniverse.Builder {
    SaveMode saveMode = SaveMode.UNLOAD;
    SyncProfile profile = SyncProfile.SYNC_NOTHING;

    @Override
    public @NotNull GraphUniverse build(@NotNull Identifier universeId) {
        GraphLibImpl.preBuild(universeId, this);
        return new SimpleGraphUniverse(universeId, this);
    }

    @Override
    public GraphUniverse.@NotNull Builder saveMode(@NotNull SaveMode saveMode) {
        this.saveMode = saveMode;
        return this;
    }

    @Override
    public GraphUniverse.@NotNull Builder synchronizeToClient(@NotNull SyncProfile profile) {
        this.profile = profile;
        return this;
    }
}
