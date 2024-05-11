package com.kneelawk.graphlib.api.graph.user;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.codextra.api.codec.CodecOrUnit;
import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.util.ObjectType;

/**
 * Holds info about a type of graph entity.
 *
 * @param <G> the type of graph entity this corresponds to.
 */
public final class GraphEntityType<G extends GraphEntity<G>> implements ObjectType {
    /**
     * {@link GraphEntityType} static codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    public static final Codec<GraphEntityType<?>> REF_CODEC =
        GraphUniverse.ATTACHMENT_KEY.retrieveWithCodecResult(ResourceLocation.CODEC, (universe, id) -> {
            GraphEntityType<?> type = universe.getGraphEntityType(id);
            if (type == null) return DataResult.error(
                () -> "Graph entity type '" + id + "' does not exist in universe '" + universe.getId() + "'");
            return DataResult.success(type);
        }, (_universe, type) -> DataResult.success(type.getId()));

    /**
     * {@link GraphEntityType} codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     *
     * @param <G> the type of graph entity.
     * @return the codec, but typed.
     */
    @SuppressWarnings("unchecked")
    public static <G extends GraphEntity<G>> Codec<GraphEntityType<G>> refCodec() {
        return (Codec<GraphEntityType<G>>) (Object) REF_CODEC;
    }

    /**
     * {@link GraphEntityType} codec getter.
     *
     * @param universe the universe the graph entity types to decode.
     * @return the codec associated with the given universe.
     */
    public static <G extends GraphEntity<G>> Codec<GraphEntityType<G>> refCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingCodec(universe, refCodec());
    }

    private final @NotNull ResourceLocation id;
    private final @NotNull GraphEntityFactory factory;
    private final @NotNull CodecOrUnit<G> codec;
    private final @NotNull GraphEntitySplitter<G> splitter;

    /**
     * @param id       the id of the graph entity type.
     * @param factory  a factory for creating new graph entities of this type.
     * @param codec    a decoder for decoding graph entities of this type.
     * @param splitter a splitter for splitting graph entities of this type apart.
     */
    private GraphEntityType(@NotNull ResourceLocation id, @NotNull GraphEntityFactory factory,
                            @NotNull CodecOrUnit<G> codec,
                            @NotNull GraphEntitySplitter<G> splitter) {
        this.id = id;
        this.factory = factory;
        this.codec = codec;
        this.splitter = splitter;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    @Override
    public @NotNull ResourceLocation getId() {return id;}

    /**
     * Gets this type's factory.
     *
     * @return this type's factory.
     */
    public @NotNull GraphEntityFactory getFactory() {return factory;}

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public @NotNull CodecOrUnit<G> getCodec() {return codec;}

    /**
     * Gets this type's splitter.
     *
     * @return this type's splitter.
     */
    public @NotNull GraphEntitySplitter<G> getSplitter() {return splitter;}

    /**
     * Used for merging one graph entity into another.
     * <p>
     * This does the necessary casts for the java compiler to be happy.
     *
     * @param into the graph entity that the other entity is being merged into.
     * @param from the graph entity that is being merged into the other entity.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public void merge(GraphEntity<?> into, GraphEntity<?> from) {
        ((G) into).merge((G) from);
    }

    /**
     * Used for calling the splitter with the correct arguments.
     *
     * @param original      the original graph entity.
     * @param originalGraph the graph the original graph entity is associated with.
     * @param newGraph      the graph of the new graph entity.
     * @return a newly split off graph entity.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public @NotNull GraphEntity<?> splitNew(@NotNull GraphEntity<?> original, @NotNull BlockGraph originalGraph,
                                            @NotNull BlockGraph newGraph) {
        return splitter.splitNew((G) original, originalGraph, newGraph);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GraphEntityType<?> that = (GraphEntityType<?>) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GraphEntityType[" + id + ']';
    }

    /**
     * Creates a new graph entity type.
     *
     * @param id       the id of the graph entity type.
     * @param factory  a factory for creating new graph entities of this type.
     * @param codec    a codec for decoding/encoding graph entities of this type.
     * @param splitter a splitter for splitting graph entities of this type apart.
     * @param <G>      The type of graph entity this type is for.
     * @return a new graph entity type.
     */
    @Contract(value = "_, _, _, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntityType<G> of(@NotNull ResourceLocation id,
                                                                            @NotNull GraphEntityFactory factory,
                                                                            @NotNull Codec<G> codec,
                                                                            @NotNull GraphEntitySplitter<G> splitter) {
        return new GraphEntityType<>(id, factory, CodecOrUnit.codec(codec), splitter);
    }

    /**
     * Creates a new graph entity type that just invokes a supplier.
     *
     * @param id       the id of the new type.
     * @param supplier a supplier for the new type.
     * @param <G>      The type of graph entity this type is for.
     * @return a new graph entity type.
     */
    @Contract(value = "_, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntityType<G> of(@NotNull ResourceLocation id,
                                                                            @NotNull Supplier<G> supplier) {
        return new GraphEntityType<>(id, supplier::get, CodecOrUnit.unit(supplier),
            (original, originalGraph, newGraph) -> supplier.get());
    }
}
