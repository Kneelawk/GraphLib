package com.kneelawk.graphlib.client.graph;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.LongSet;

import com.kneelawk.graphlib.graph.struct.Graph;

public record ClientBlockGraph(long graphId, @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
