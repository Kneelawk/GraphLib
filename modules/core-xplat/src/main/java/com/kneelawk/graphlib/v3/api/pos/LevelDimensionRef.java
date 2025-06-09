package com.kneelawk.graphlib.v3.api.pos;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.ResourceLocation;

import static com.kneelawk.graphlib.v3.impl.GLConstants.rl;

/**
 * A reference to a specific {@link net.minecraft.world.level.Level}.
 *
 * @param level the name of the level, stored in the server.
 */
public record LevelDimensionRef(ResourceLocation level) implements DimensionRef {
    /**
     * LevelRef codec.
     */
    public static final MapCodec<LevelDimensionRef> MAP_CODEC =
        ResourceLocation.CODEC.fieldOf("level").xmap(LevelDimensionRef::new, LevelDimensionRef::level);
    /**
     * LevelRef type.
     */
    public static final Type<LevelDimensionRef> TYPE = new Type<>(rl("level"), MAP_CODEC);

    @Override
    public Type<? extends DimensionRef> getType() {
        return TYPE;
    }
}
