package com.kneelawk.graphlib.impl.mixin.api;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.graphlib.impl.graph.ServerGraphWorldStorage;

public interface GraphWorldStorageAccess {
    @NotNull ServerGraphWorldStorage graphlib_getGraphWorldStorage();
}
