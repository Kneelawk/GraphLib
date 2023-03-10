package com.kneelawk.graphlib.api.v1.event;

import com.kneelawk.graphlib.api.v1.graph.GraphUniverse;

/**
 * Entrypoint {@code graphlib:universe_build} that is invoked before every universe build.
 */
public interface PreBuildUniverse {
    /**
     * Invoked before every {@link GraphUniverse} is built.
     *
     * @param builder the builder building the given {@link GraphUniverse}.
     */
    void preBuild(GraphUniverse.Builder builder);
}
