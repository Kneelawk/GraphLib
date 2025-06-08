package com.kneelawk.graphlib.v3.impl;

import net.minecraft.resources.ResourceLocation;

public class GLConstants {
    public static final String MOD_ID = "graphlib_v3";

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
