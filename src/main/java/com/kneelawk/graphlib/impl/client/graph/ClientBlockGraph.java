package com.kneelawk.graphlib.impl.client.graph;

import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.graph.struct.Graph;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.NotNull;

public record ClientBlockGraph(long graphId, @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
