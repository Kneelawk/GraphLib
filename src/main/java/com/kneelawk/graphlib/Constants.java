package com.kneelawk.graphlib;

import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Constants {
    private Constants() {
    }

    public static final String MOD_ID = "graphlib";
    public static final String DATA_DIRNAME = "data";
    public static final String GRAPHDATA_DIRNAME = "graphdata";
    public static final String REGION_DIRNAME = "region";
    public static final String GRAPHS_DIRNAME = "graphs";
    public static final String STATE_FILENAME = "state.dat";

    @Contract("_ -> new")
    public static @NotNull Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static TranslatableText tt(String prefix, String suffix, Object... args) {
        return new TranslatableText(prefix + "." + MOD_ID + "." + suffix, args);
    }

    public static TranslatableText command(String suffix, Object... args) {
        return tt("command", suffix, args);
    }
}
