package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import com.kneelawk.codextra.api.Codextra;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.util.HalfLink;
import com.kneelawk.graphlib.api.util.NodePos;

/**
 * The data stored in a link between nodes.
 */
public interface LinkKey {
    /**
     * {@link LinkKey} map codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     * <p>
     * This uses the {@code keyType} and {@code key} map keys.
     */
    MapCodec<LinkKey> MAP_CODEC = LinkKeyType.REF_CODEC.dispatchMap("keyType", LinkKey::getType,
        type -> Codextra.unitHandlingFieldOf("key", type.getCodec()));

    /**
     * {@link #MAP_CODEC} with universe attached.
     *
     * @param universe the universe to attach.
     * @return the map codec.
     */
    static MapCodec<LinkKey> mapCodec(GraphUniverse universe) {
        return GraphUniverse.ATTACHMENT_KEY.attachingMapCodec(universe, MAP_CODEC);
    }

    /**
     * Gets the type id of this link key.
     * <p>
     * Note: this is the same type id as is used in registering link key decoders,
     * {@link com.kneelawk.graphlib.api.graph.GraphUniverse#addLinkKeyType(LinkKeyType)}.
     *
     * @return this link key's type id.
     */
    @NotNull
    LinkKeyType getType();

    /**
     * Checks whether this specific link should have a link entity associated with it.
     * <p>
     * Note: if this returns <code>true</code> and
     * {@link com.kneelawk.graphlib.api.graph.GraphWorld#connectNodes(NodePos, NodePos, LinkKey, LinkEntity)} is called
     * with a <code>null</code> link entity, then {@link #createLinkEntity(LinkHolder)} is called to create a new link
     * entity.
     *
     * @param holder the link holder for this link.
     * @return <code>true</code> if this link should have a link entity associated with it.
     */
    default boolean shouldHaveLinkEntity(@NotNull LinkHolder<LinkKey> holder) {
        return false;
    }

    /**
     * Creates a new link entity that will be associated with this link.
     *
     * @param holder the link holder for this link.
     * @return a newly created link entity, or <code>null</code> if a link entity could not be created.
     */
    default @Nullable LinkEntity createLinkEntity(@NotNull LinkHolder<LinkKey> holder) {
        return null;
    }

    /**
     * Checks whether this link should be automatically removed if either node doesn't want it anymore.
     * <p>
     * Automatic removal follows the rules:
     * <ul>
     *     <li>If a link does not appear in the result of {@link BlockNode#findConnections(com.kneelawk.graphlib.api.graph.NodeHolder)}, then the link
     *     is removed.</li>
     *     <li>If either end's {@link BlockNode#canConnect(com.kneelawk.graphlib.api.graph.NodeHolder, HalfLink)} returns <code>false</code>, the the
     *     link is removed.</li>
     * </ul>
     * <p>
     * Note: links are still automatically removed if the node at one end doesn't exist anymore.
     *
     * @param holder the link holder for this link.
     * @return <code>true</code> if this node should be automatically removed.
     */
    default boolean isAutomaticRemoval(@NotNull LinkHolder<LinkKey> holder) {
        return true;
    }

    /**
     * Link keys are compared based on their hash-code and equals functions.
     * <p>
     * Link keys must always implement consistent hash-code and equals functions, as this allows the graph world to be
     * able to correctly evaluate if links need to be removed or added at a given position.
     *
     * @return the hash-code of this link key's data.
     */
    @Override
    int hashCode();

    /**
     * Link keys are compared based on their hash-code and equals functions.
     * <p>
     * Link keys must always implement consistent hash-code and equals functions, as this allows the graph world to be
     * able to correctly evaluate if links need to be removed or added at a given position.
     *
     * @param o the other link key to compare this link key to.
     * @return <code>true</code> if these two link keys hold the same data, <code>false</code> otherwise.
     */
    @Override
    boolean equals(@Nullable Object o);
}
