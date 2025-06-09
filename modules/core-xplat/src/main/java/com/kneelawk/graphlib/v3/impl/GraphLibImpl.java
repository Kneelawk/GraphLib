package com.kneelawk.graphlib.v3.impl;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.v3.api.graph.GraphUniverse;

public class GraphLibImpl {
    public static final Map<ResourceLocation, GraphUniverse> UNIVERSES = new Object2ObjectLinkedOpenHashMap<>();
}
