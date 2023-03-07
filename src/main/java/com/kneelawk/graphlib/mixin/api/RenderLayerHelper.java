package com.kneelawk.graphlib.mixin.api;

import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.render.RenderLayer;

import com.kneelawk.graphlib.mixin.impl.RenderLayerAccessor;

public final class RenderLayerHelper {
    private RenderLayerHelper() {
    }

    public static RenderLayer of(
        String name,
        VertexFormat vertexFormat,
        VertexFormat.DrawMode drawMode,
        int expectedBufferSize,
        boolean hasCrumbling,
        boolean translucent,
        RenderLayer.MultiPhaseParameters phases
    ) {
        return RenderLayerAccessor.callOf(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent,
            phases);
    }
}
