package com.kneelawk.graphlib.impl.mixin.api;

import com.kneelawk.graphlib.impl.mixin.impl.RenderLayerAccessor;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.RenderLayer;

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
        return RenderLayerAccessor.callOf(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
    }
}
