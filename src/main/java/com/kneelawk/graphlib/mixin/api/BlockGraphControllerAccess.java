package com.kneelawk.graphlib.mixin.api;

import com.kneelawk.graphlib.graph.BlockGraphController;
import org.jetbrains.annotations.NotNull;

public interface BlockGraphControllerAccess {
    @NotNull BlockGraphController graphlib_getGraphController();
}
