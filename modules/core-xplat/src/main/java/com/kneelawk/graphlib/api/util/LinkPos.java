package com.kneelawk.graphlib.api.util;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

/**
 * Represents a positioned unique link in a way that can be looked up.
 * <p>
 * Note: this type is non-directional. A link from 'A' to 'B' is the same as a link from 'B' to 'A'. The
 * {@link #equals(Object)} and {@link #hashCode()} methods reflect this.
 *
 * @param first  the first node in this link.
 * @param second the second node in this link.
 * @param key    the key of this link that makes it unique among all the links between the same two nodes.
 */
public record LinkPos(@NotNull NodePos first, @NotNull NodePos second, @NotNull LinkKey key) {
    /**
     * Map codec for link poses.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    public static final MapCodec<LinkPos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        NodePos.MAP_CODEC.fieldOf("first").forGetter(LinkPos::first),
        NodePos.MAP_CODEC.fieldOf("second").forGetter(LinkPos::second),
        LinkKey.MAP_CODEC.forGetter(LinkPos::key)
    ).apply(instance, LinkPos::new));

    /**
     * Map codec for link poses that provides its own universe.
     */
    public static final MapCodec<InUniverse<LinkPos>> IN_UNIVERSE_MAP_CODEC = InUniverse.mapCodec(MAP_CODEC);

    /**
     * Gets a link pos codec for link poses in the given universe.
     *
     * @param universe the universe to find link poses in.
     * @return a link pos codec for link poses in the given universe.
     */
    public static MapCodec<LinkPos> codec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingMapCodec(universe, MAP_CODEC);
    }

    /**
     * Creates a new link pos from raw positions, nodes, and the link key.
     *
     * @param firstPos   the block position of the first end of this link.
     * @param firstNode  the block node at the first end of this link.
     * @param secondPos  the block position of the second end of this link.
     * @param secondNode the block node at the second end of this link.
     * @param key        the key of this link.
     */
    public LinkPos(@NotNull BlockPos firstPos, @NotNull BlockNode firstNode, @NotNull BlockPos secondPos,
                   @NotNull BlockNode secondNode, @NotNull LinkKey key) {
        this(new NodePos(firstPos, firstNode), new NodePos(secondPos, secondNode), key);
    }

    /**
     * Gets the node pos at the opposite end of this link from the given pos.
     *
     * @param pos the pos to get the opposite end of the link from.
     * @return the pos at the opposite end of this link from the given pos.
     */
    public NodePos other(@NotNull NodePos pos) {
        if (first.equals(pos)) {
            return second;
        } else {
            return first;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkPos linkKey = (LinkPos) o;

        if (!key.equals(linkKey.key)) return false;

        if (first.equals(linkKey.first)) {
            return second.equals(linkKey.second);
        } else if (second.equals(linkKey.first)) {
            return first.equals(linkKey.second);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = result ^ second.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "(" + first + "<-" + key + "->" + second + ")";
    }
}
