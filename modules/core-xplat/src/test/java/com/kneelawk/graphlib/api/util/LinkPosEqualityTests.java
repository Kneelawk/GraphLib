package com.kneelawk.graphlib.api.util;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
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
        BlockNodeType.of(new ResourceLocation("test", "string"), nbt -> {
            if (nbt instanceof StringTag string)
                return new StringBlockNode(string.getAsString());
            return null;
        });

    private static final LinkKeyType STRING_LINK_TYPE = LinkKeyType.of(new ResourceLocation("test", "string"), nbt -> {
        if (nbt instanceof StringTag string)
            return new StringLinkKey(string.getAsString());
        return null;
    });

    private record StringBlockNode(String str) implements BlockNode {
        @Override
        public @NotNull BlockNodeType getType() {
            return STRING_NODE_TYPE;
        }

        @Override
        public @Nullable Tag toTag() {
            return StringTag.valueOf(str);
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
        @Override
        public @NotNull LinkKeyType getType() {
            return STRING_LINK_TYPE;
        }

        @Override
        public @Nullable Tag toTag() {
            return StringTag.valueOf(str);
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
