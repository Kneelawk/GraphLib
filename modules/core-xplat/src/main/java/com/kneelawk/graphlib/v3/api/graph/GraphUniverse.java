package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.graphlib.v3.api.GraphLib;

/**
 * Manages all graphs and graph accessories.
 */
@ApiStatus.NonExtendable
public interface GraphUniverse {

    /**
     * Graph-Universe attachment key for use in codecs.
     */
    AttachmentKey<GraphUniverse> ATTACHMENT_KEY = AttachmentKey.ofStaticFieldName();

    /**
     * Codec for referencing a graph universe.
     */
    Codec<GraphUniverse> REF_CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
        if (!GraphLib.universeExists(id))
            return DataResult.error(() -> "The graph universe '" + id + "' does not exist");
        return DataResult.success(GraphLib.getUniverseOrThrow(id));
    }, GraphUniverse::getId);

    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    @NotNull
    ResourceLocation getId();
}
