package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphEntityContext;

/**
 * Holds info about a type of graph entity.
 *
 * @param id       the id of the graph entity type.
 * @param factory  a factory for creating new graph entities of this type.
 * @param decoder  a decoder for decoding graph entities of this type.
 * @param splitter a splitter for splitting graph entities of this type apart.
 * @param <G>      the type of graph entity this corresponds to.
 */
public record GraphEntityType<G extends GraphEntity<G>>(@NotNull Identifier id, @NotNull GraphEntityFactory<G> factory,
                                                        @NotNull GraphEntityDecoder<G> decoder,
                                                        @NotNull GraphEntitySplitter<G> splitter) {
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
}
