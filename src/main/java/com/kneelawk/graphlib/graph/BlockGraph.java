package com.kneelawk.graphlib.graph;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.kneelawk.graphlib.node.BlockNode;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Translated from 2xsaiko's HCTM-Base WireNetworkState code:
// https://github.com/2xsaiko/hctm-base/blob/119df440743543b8b4979b450452d73f2c3c4c47/src/main/kotlin/common/wire/WireNetworkState.kt

public class BlockGraph {
    public static BlockGraph fromTag(BlockGraphController controller, NbtCompound tag) {
        UUID id = tag.getUuid("id");
        BlockGraph graph = new BlockGraph(controller, id);

        NbtList nodesTag = tag.getList("nodes", NbtType.LIST);
        NbtList linksTag = tag.getList("links", NbtType.LIST);

        List<Node<BlockNodeWrapper<? extends BlockNode>>> nodes = new ArrayList<>();

        for (NbtElement nodeElement : nodesTag) {
            BlockNodeWrapper<BlockNode> node = BlockNodeWrapper.fromTag((NbtCompound) nodeElement);
        }
    }

    final BlockGraphController controller;
    final UUID id;

    private final Graph<BlockNodeWrapper<? extends BlockNode>> graph = new Graph<>();
    private final Multimap<BlockPos, Node<BlockNodeWrapper<? extends BlockNode>>> nodesInPos =
            LinkedHashMultimap.create();

    public BlockGraph(BlockGraphController controller, UUID id) {
        this.controller = controller;
        this.id = id;
    }

    public Node<BlockNodeWrapper<? extends BlockNode>> createNode(BlockPos pos, BlockNode node) {
        Node<BlockNodeWrapper<? extends BlockNode>> graphNode = graph.add(new BlockNodeWrapper<>(pos, node));
        nodesInPos.put(pos, graphNode);
    }
}
