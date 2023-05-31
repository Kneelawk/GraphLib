package com.kneelawk.graphlib.api.util;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.graph.NodeContext;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class LinkPosEqualityTests {
    private record StringBlockNode(String str) implements BlockNode {
        @Override
        public @NotNull Identifier getTypeId() {
            return new Identifier("test", "string");
        }

        @Override
        public @Nullable NbtElement toTag() {
            return NbtString.of(str);
        }

        @Override
        public @NotNull Collection<HalfLink> findConnections(@NotNull NodeContext ctx) {
            return List.of();
        }

        @Override
        public boolean canConnect(@NotNull NodeContext ctx, @NotNull HalfLink other) {
            return false;
        }

        @Override
        public void onConnectionsChanged(@NotNull NodeContext ctx) {}
    }

    private record StringLinkKey(String str) implements LinkKey {
        @Override
        public @NotNull Identifier getTypeId() {
            return new Identifier("test", "string");
        }

        @Override
        public @Nullable NbtElement toTag() {
            return NbtString.of(str);
        }
    }

    @Test
    public void testLinksEqualBothWays() {
        NodePos a = new NodePos(BlockPos.ORIGIN, new StringBlockNode("A"));
        NodePos b = new NodePos(BlockPos.ORIGIN, new StringBlockNode("B"));

        LinkPos aToB = new LinkPos(a, b, new StringLinkKey("C"));
        LinkPos bToA = new LinkPos(b, a, new StringLinkKey("C"));

        assertEquals("The link poses should be equal", aToB, bToA);
        assertEquals("The link poses' hashCodes should be equal", aToB.hashCode(), bToA.hashCode());
    }

    @Test
    public void testLinksWithDifferentKeys() {
        NodePos a = new NodePos(BlockPos.ORIGIN, new StringBlockNode("A"));
        NodePos b = new NodePos(BlockPos.ORIGIN, new StringBlockNode("B"));

        LinkPos cLink = new LinkPos(a, b, new StringLinkKey("C"));
        LinkPos dLink = new LinkPos(a, b, new StringLinkKey("D"));

        assertNotEquals("The links should not be equal", cLink, dLink);
    }
}
