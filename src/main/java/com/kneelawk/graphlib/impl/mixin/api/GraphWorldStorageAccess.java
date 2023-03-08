package com.kneelawk.graphlib.impl.mixin.api;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.impl.graph.GraphWorldStorage;

public interface GraphWorldStorageAccess {
    @NotNull GraphWorldStorage graphlib_getGraphWorldStorage();
}
