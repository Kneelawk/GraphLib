package com.kneelawk.graphlib;

import net.minecraft.util.Identifier;

public final class Constants {
    private Constants() {
    }

    public static final String MOD_ID = "graphlib";
    public static final String DATA_DIRNAME = "data";
    public static final String GRAPHDATA_DIRNAME = "graphdata";
    public static final String REGION_DIRNAME = "region";
    public static final String GRAPHS_DIRNAME = "graphs";
    public static final String STATE_FILENAME = "state.dat";

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
