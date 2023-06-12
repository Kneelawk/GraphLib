package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Holds info about a type of graph entity.
 *
 * @param <G> the type of graph entity this corresponds to.
 */
public final class GraphEntityType<G extends GraphEntity<G>> {
    private final @NotNull Identifier id;
    private final @NotNull GraphEntityFactory<G> factory;
    private final @NotNull GraphEntityDecoder<G> decoder;
    private final @NotNull GraphEntitySplitter<G> splitter;
    private final @Nullable GraphEntityPacketDecoder<G> packetDecoder;

    /**
     * @param id            the id of the graph entity type.
     * @param factory       a factory for creating new graph entities of this type.
     * @param decoder       a decoder for decoding graph entities of this type.
     * @param splitter      a splitter for splitting graph entities of this type apart.
     * @param packetDecoder an optional packet decoder for decoding graph entities on the client.
     */
    private GraphEntityType(@NotNull Identifier id, @NotNull GraphEntityFactory<G> factory,
                            @NotNull GraphEntityDecoder<G> decoder,
                            @NotNull GraphEntitySplitter<G> splitter,
                            @Nullable GraphEntityPacketDecoder<G> packetDecoder) {
        this.id = id;
        this.factory = factory;
        this.decoder = decoder;
        this.splitter = splitter;
        this.packetDecoder = packetDecoder;
    }

    /**
     * Gets this type's id.
     *
     * @return this type's id.
     */
    public @NotNull Identifier getId() {return id;}

    /**
     * Gets this type's factory.
     *
     * @return this type's factory.
     */
    public @NotNull GraphEntityFactory<G> getFactory() {return factory;}

    /**
     * Gets this type's decoder.
     *
     * @return this type's decoder.
     */
    public @NotNull GraphEntityDecoder<G> getDecoder() {return decoder;}

    /**
     * Gets this type's splitter.
     *
     * @return this type's splitter.
     */
    public @NotNull GraphEntitySplitter<G> getSplitter() {return splitter;}

    /**
     * Gets this type's packet decoder.
     *
     * @return this type's packet decoder.
     */
    public GraphEntityPacketDecoder<G> getPacketDecoder() {
        return packetDecoder;
    }

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
     * @param ctx           the graph context for the new graph entity.
     * @return a newly split off graph entity.
     */
    @ApiStatus.Internal
    @SuppressWarnings("unchecked")
    public @NotNull GraphEntity<G> splitNew(@NotNull GraphEntity<?> original, @NotNull BlockGraph originalGraph,
                                            @NotNull GraphEntityContext ctx) {
        return splitter.splitNew((G) original, originalGraph, ctx);
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
        return "GraphEntityType{" +
            "id=" + id +
            '}';
    }

    /**
     * Creates a new graph entity type.
     *
     * @param id            the id of the graph entity type.
     * @param factory       a factory for creating new graph entities of this type.
     * @param decoder       a decoder for decoding graph entities of this type.
     * @param splitter      a splitter for splitting graph entities of this type apart.
     * @param packetDecoder an optional packet decoder for decoding graph entities on the client.
     * @return a new graph entity type.
     */
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    public static <G extends GraphEntity<G>> @NotNull GraphEntityType<G> of(@NotNull Identifier id,
                                                                            @NotNull GraphEntityFactory<G> factory,
                                                                            @NotNull GraphEntityDecoder<G> decoder,
                                                                            @NotNull GraphEntitySplitter<G> splitter,
                                                                            @Nullable GraphEntityPacketDecoder<G> packetDecoder) {
        return new GraphEntityType<>(id, factory, decoder, splitter, packetDecoder);
    }

    public static <G extends GraphEntity<G>> @NotNull GraphEntityType<G> of(@NotNull Identifier id,
                                                                            @NotNull GraphEntityFactory<G> factory,
                                                                            @NotNull GraphEntityDecoder<G> decoder,
                                                                            @NotNull GraphEntitySplitter<G> splitter) {
        return new GraphEntityType<>(id, factory, decoder, splitter, null);
    }
}
