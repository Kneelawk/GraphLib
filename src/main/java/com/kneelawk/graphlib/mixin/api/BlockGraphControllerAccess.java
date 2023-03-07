package com.kneelawk.graphlib.mixin.api;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;

public interface BlockGraphControllerAccess {
    @NotNull SimpleBlockGraphController graphlib_getGraphController();
}
