package com.kneelawk.graphlib.impl.client.graph;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.client.ClientBlockNodeHolder;
import com.kneelawk.graphlib.api.util.graph.Graph;

public record ClientBlockGraph(@NotNull Identifier universeId, long graphId,
                               @NotNull Graph<ClientBlockNodeHolder> graph, @NotNull LongSet chunks) {
}
