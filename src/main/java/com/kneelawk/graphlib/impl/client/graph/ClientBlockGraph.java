package com.kneelawk.graphlib.impl.client.graph;

import com.kneelawk.graphlib.api.v1.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.v1.graph.struct.Graph;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

public record ClientBlockGraph(long graphId, @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
