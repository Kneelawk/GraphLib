package com.kneelawk.graphlib.client.graph;

import com.kneelawk.graphlib.graph.struct.Graph;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

public record ClientBlockGraph(long graphId, @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
