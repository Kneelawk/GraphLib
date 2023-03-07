package com.kneelawk.graphlib.impl.mixin.api;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphWorld;

public interface BlockGraphControllerAccess {
    @NotNull SimpleGraphWorld graphlib_getGraphController();
}
