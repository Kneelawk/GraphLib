package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.graphlib.v3.api.GraphLib;
import com.kneelawk.graphlib.v3.api.graph.user.GraphNodeType;
import com.kneelawk.graphlib.v3.api.graph.user.LinkKeyType;

/**
 * Manages all graphs and graph accessories.
 */
public final class GraphUniverse {

    /**
     * Graph-Universe attachment key for use in codecs.
     */
    public static final AttachmentKey<GraphUniverse> ATTACHMENT_KEY = AttachmentKey.ofStaticFieldName();

    /**
     * Codec for referencing a graph universe.
     */
    public static final Codec<GraphUniverse> REF_CODEC = ResourceLocation.CODEC.comapFlatMap(id -> {
        if (!GraphLib.universeExists(id))
            return DataResult.error(() -> "The graph universe '" + id + "' does not exist");
        return DataResult.success(GraphLib.getUniverseOrThrow(id));
    }, GraphUniverse::getId);

    private final ResourceLocation id;

    private GraphUniverse(ResourceLocation id) {
        this.id = id;
    }

    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    public ResourceLocation getId() {
        return id;
    }

    /**
     * Gets the graph node type for the given type id.
     *
     * @param typeId the type id of the graph node type.
     * @return the graph node type for the given type id.
     */
    @Nullable
    public GraphNodeType getNodeType(@NotNull ResourceLocation typeId) {
        throw new AssertionError("Stub");
    }

    /**
     * Gets the link key type for the given type id.
     *
     * @param typeId the type id of the link key type.
     * @return the link key type for the given type id.
     */
    @Nullable
    public LinkKeyType getLinkKeyType(@NotNull ResourceLocation typeId) {
        throw new AssertionError("Stub");
    }
}
