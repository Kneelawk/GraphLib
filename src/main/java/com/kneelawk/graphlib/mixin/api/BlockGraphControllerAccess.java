package com.kneelawk.graphlib.mixin.api;

import com.kneelawk.graphlib.graph.simple.SimpleBlockGraphController;
import org.jetbrains.annotations.NotNull;

public interface BlockGraphControllerAccess {
    @NotNull SimpleBlockGraphController graphlib_getGraphController();
}
