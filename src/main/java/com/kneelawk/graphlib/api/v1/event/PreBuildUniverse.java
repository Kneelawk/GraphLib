package com.kneelawk.graphlib.api.v1.event;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;

/**
 * Entrypoint {@code graphlib:universe_build} that is invoked before every universe build.
 */
public interface PreBuildUniverse {
    /**
     * Invoked before every {@link GraphUniverse} is built.
     *
     * @param universeId the unique id of the GraphUniverse being built.
     * @param builder the builder building the given {@link GraphUniverse}.
     */
    void preBuild(Identifier universeId, GraphUniverse.Builder builder);
}
