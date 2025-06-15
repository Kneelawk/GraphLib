package com.kneelawk.graphlib.v3.api.graph.user;

import com.mojang.serialization.MapCodec;

import com.kneelawk.graphlib.v3.api.graph.GraphUniverse;

/**
 * The data stored in a link between nodes.
 */
public interface LinkKey {
    /**
     * {@link LinkKey} map codec.
     * <p>
     * <b>This requires the {@link GraphUniverse#ATTACHMENT_KEY} attachment.</b>
     */
    MapCodec<LinkKey> MAP_CODEC = LinkKeyType.REF_CODEC.dispatchMap(LinkKey::getType, LinkKeyType::getCodec);

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
     *
     * @return this link key's type id.
     */
    LinkKeyType getType();
}
