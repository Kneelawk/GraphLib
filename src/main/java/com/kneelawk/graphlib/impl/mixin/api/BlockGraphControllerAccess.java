package com.kneelawk.graphlib.impl.mixin.api;

import com.kneelawk.graphlib.impl.graph.simple.SimpleBlockGraphController;
import org.jetbrains.annotations.NotNull;

public interface BlockGraphControllerAccess {
    @NotNull SimpleBlockGraphController graphlib_getGraphController();
}
