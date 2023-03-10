package com.kneelawk.graphlib.impl;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.text.Text;
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
    public static final String UNIVERSE_PRE_BUILD = MOD_ID + ":universe_build";

    @Contract("_ -> new")
    public static @NotNull Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static Text tt(String prefix, String suffix, Object... args) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }

    public static Text command(String suffix, Object... args) {
        return tt("command", suffix, args);
    }
}
