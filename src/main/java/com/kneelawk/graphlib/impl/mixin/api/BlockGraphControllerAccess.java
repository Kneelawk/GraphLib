package com.kneelawk.graphlib.impl.mixin.api;

import com.kneelawk.graphlib.impl.graph.simple.SimpleGraphWorld;
import org.jetbrains.annotations.NotNull;

public interface BlockGraphControllerAccess {
    @NotNull SimpleGraphWorld graphlib_getGraphController();
}
