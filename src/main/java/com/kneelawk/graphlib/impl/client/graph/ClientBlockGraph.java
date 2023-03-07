package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.LongSet;

import com.kneelawk.graphlib.api.v1.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.v1.util.graph.Graph;

public record ClientBlockGraph(long graphId, @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
