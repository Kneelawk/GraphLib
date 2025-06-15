package com.kneelawk.graphlib.v3.api.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import com.kneelawk.codextra.api.attach.AttachmentKey;
import com.kneelawk.graphlib.v3.api.GraphLib;
import com.kneelawk.graphlib.v3.api.graph.backend.DimensionManager;
import com.kneelawk.graphlib.v3.api.graph.backend.NodeSpace;
import com.kneelawk.graphlib.v3.api.graph.user.GraphNodeType;
import com.kneelawk.graphlib.v3.api.graph.user.LinkKeyType;
import com.kneelawk.graphlib.v3.api.pos.DimensionRef;
import com.kneelawk.graphlib.v3.api.pos.NodePos;

/**
 * Manages all graphs and graph accessories.
 */
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
    ResourceLocation getId();

    /**
     * {@return the graph world associated with the currently running minecraft server}
     * <p>
     * If no minecraft server is currently running (e.g. no game is running, this is a client connected to a remote
     * server), this will return {@code null}.
     */
    @Nullable GraphWorld getGraphWorld();

    /**
     * Gets the graph node type for the given type id.
     *
     * @param typeId the type id of the graph node type.
     * @return the graph node type for the given type id.
     */
    @Nullable GraphNodeType getNodeType(@NotNull ResourceLocation typeId);

    /**
     * Gets the link key type for the given type id.
     *
     * @param typeId the type id of the link key type.
     * @return the link key type for the given type id.
     */
    @Nullable LinkKeyType getLinkKeyType(@NotNull ResourceLocation typeId);

    /**
     * Gets the node space for the given type of node pos if it has been registered.
     *
     * @param type the type of node pos that the node space is associated with.
     * @return the node space associated with the given node pos type.
     */
    @Nullable NodeSpace getNodeSpace(NodePos.Type<?> type);

    /**
     * Gets the dimension manager for the given type of dimension ref if it has been registered.
     *
     * @param type the type of dimension ref that the dimension manager is for.
     * @param <R>  the type of dimension ref to get the dimension manager of.
     * @return the dimension manager for the given type of dimension ref.
     */
    @Nullable <R extends DimensionRef> DimensionManager<R> getDimensionManager(DimensionRef.Type<R> type);

    /**
     * Gets a {@link Level} for the given dimension ref, if one is available.
     *
     * @param dimensionRef the dimension reference that the level is associated with.
     * @return the level associated with the given dimension reference.
     */
    @SuppressWarnings("unchecked")
    default @Nullable Level getLevel(DimensionRef dimensionRef) {
        DimensionManager<?> manager = getDimensionManager(dimensionRef.getType());
        if (manager == null) return null;
        return ((DimensionManager<DimensionRef>) manager).getLevel(dimensionRef);
    }
}
