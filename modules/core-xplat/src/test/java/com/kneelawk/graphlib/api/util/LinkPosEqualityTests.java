package com.kneelawk.graphlib.api.util;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.BlockNodeType;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.graph.user.LinkKeyType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LinkPosEqualityTests {
    private static final BlockNodeType STRING_NODE_TYPE =
        BlockNodeType.of(new ResourceLocation("test", "string"), StringBlockNode.CODEC);

    private static final LinkKeyType STRING_LINK_TYPE =
        LinkKeyType.of(new ResourceLocation("test", "string"), StringLinkKey.CODEC);

    private record StringBlockNode(String str) implements BlockNode {
        public static final Codec<StringBlockNode> CODEC =
            Codec.STRING.xmap(StringBlockNode::new, StringBlockNode::str);

        @Override
        public @NotNull BlockNodeType getType() {
            return STRING_NODE_TYPE;
        }

        @Override
        public @NotNull Collection<HalfLink> findConnections(@NotNull NodeHolder<BlockNode> self) {
            return List.of();
        }

        @Override
        public boolean canConnect(@NotNull NodeHolder<BlockNode> self, @NotNull HalfLink other) {
            return false;
        }

        @Override
        public void onConnectionsChanged(@NotNull NodeHolder<BlockNode> self) {}
    }

    private record StringLinkKey(String str) implements LinkKey {
        public static final Codec<StringLinkKey> CODEC = Codec.STRING.xmap(StringLinkKey::new, StringLinkKey::str);

        @Override
        public @NotNull LinkKeyType getType() {
            return STRING_LINK_TYPE;
        }
    }

    @Test
    public void testLinksEqualBothWays() {
        NodePos a = new NodePos(BlockPos.ZERO, new StringBlockNode("A"));
        NodePos b = new NodePos(BlockPos.ZERO, new StringBlockNode("B"));

        LinkPos aToB = new LinkPos(a, b, new StringLinkKey("C"));
        LinkPos bToA = new LinkPos(b, a, new StringLinkKey("C"));

        assertEquals("The link poses should be equal", aToB, bToA);
        assertEquals("The link poses' hashCodes should be equal", aToB.hashCode(), bToA.hashCode());
    }

    @Test
    public void testLinksWithDifferentKeys() {
        NodePos a = new NodePos(BlockPos.ZERO, new StringBlockNode("A"));
        NodePos b = new NodePos(BlockPos.ZERO, new StringBlockNode("B"));

        LinkPos cLink = new LinkPos(a, b, new StringLinkKey("C"));
        LinkPos dLink = new LinkPos(a, b, new StringLinkKey("D"));

        assertNotEquals("The links should not be equal", cLink, dLink);
    }
}
