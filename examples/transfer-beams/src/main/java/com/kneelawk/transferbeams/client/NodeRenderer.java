/*
 * MIT License
 *
 * Copyright (c) 2023 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.transferbeams.client;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.LinkEntityContext;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.kmodlib.client.overlay.RenderToOverlay;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.graph.TransferLinkEntity;
import com.kneelawk.transferbeams.graph.TransferNodeEntity;
import com.kneelawk.transferbeams.util.SelectedNode;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class NodeRenderer {
    private static final ResourceLocation[] ITEM_NODE_MODELS = new ResourceLocation[DyeColor.values().length];

    public static @Nullable SelectedNode selectedNode = null;

    static {
        for (DyeColor color : DyeColor.values()) {
            ITEM_NODE_MODELS[color.getId()] = id("block/" + color.getName() + "_item_transfer_node");
        }
    }

    public static void init() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(NodeRenderer::beforeBlockOutline);
        WorldRenderEvents.AFTER_ENTITIES.register(NodeRenderer::plainRender);
        RenderToOverlay.EVENT.register(NodeRenderer::overlayRender);

        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(ITEM_NODE_MODELS));
    }

    private static boolean shouldRenderSelection() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return false;

        for (ItemStack stack : player.getHandSlots()) {
            if (stack.is(TransferBeamsMod.NODE_SELECTORS)) {
                return true;
            }
        }

        return false;
    }

    private static boolean beforeBlockOutline(WorldRenderContext ctx, @Nullable HitResult hit) {
        return !shouldRenderSelection();
    }

    private static boolean shouldRenderToOverlay() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return false;

        for (ItemStack stack : player.getHandSlots()) {
            if (stack.is(TransferBeamsMod.NODE_VISUALIZERS)) {
                return true;
            }
        }

        return false;
    }

    private static void plainRender(WorldRenderContext ctx) {
        if (!shouldRenderToOverlay()) render(ctx, false);
    }

    private static void overlayRender(WorldRenderContext ctx) {
        if (shouldRenderToOverlay()) render(ctx, true);
    }

    private static void render(WorldRenderContext ctx, boolean mulPos) {
        GraphView view = TransferBeamsMod.SYNCED.getClientGraphView();
        if (view == null) return;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        PoseStack stack = ctx.matrixStack();
        ClientLevel world = ctx.world();
        MultiBufferSource provider = ctx.consumers();
        Vec3 cameraPos = ctx.camera().getPosition();
        assert provider != null;
        assert stack != null;

        stack.pushPose();
        if (mulPos) stack.mulPose(ctx.positionMatrix());

        // We want the nodes to always be positioned by color.
        Map<BlockPos, SortedEntities> sorted = sortNodeEntities(view);
        Map<BlockPos, EnumMap<DyeColor, Vec3>> positions = new Object2ObjectLinkedOpenHashMap<>();

        for (SortedEntities entities : sorted.values()) {
            int index = 0;
            for (DyeColor color : DyeColor.values()) {
                // the array only holds elements for the colors that are actually in the given block
                TransferNodeEntity entity = entities.array[color.getId()];
                if (entity == null) continue;
                NodeEntityContext ectx = entity.getContext();
                BlockPos blockPos = ectx.getBlockPos();
                BlockState state = ectx.getBlockState();

                Vec3 position = getPositionForIndex(index, entities.count);

                // remember this position for when we render the links
                EnumMap<DyeColor, Vec3> colorPositions =
                    positions.computeIfAbsent(blockPos, _pos -> new EnumMap<>(DyeColor.class));
                colorPositions.put(color, position);

                stack.pushPose();

                // correctly position the stack
                stack.translate(blockPos.getX() - cameraPos.x() + position.x,
                    blockPos.getY() - cameraPos.y() + position.y, blockPos.getZ() - cameraPos.z() + position.z);
                stack.scale(0.25f, 0.25f, 0.25f);
                stack.translate(-0.5f, -0.5f, -0.5f);

                // actually render the model
                BakedModel model = RenderUtils.getBakedModel(ITEM_NODE_MODELS[color.getId()]);
                RenderUtils.renderModel(model, state, stack, provider.getBuffer(RenderType.cutout()),
                    LightTexture.FULL_BRIGHT);

                stack.popPose();

                index++;
            }
        }

        // Render links in any order
        Iterator<BlockGraph> graphIter = view.getAllGraphs().iterator();
        while (graphIter.hasNext()) {
            BlockGraph graph = graphIter.next();

            Iterator<LinkEntity> linkIter = graph.getLinkEntities().iterator();
            while (linkIter.hasNext()) {
                LinkEntity entity = linkIter.next();
                if (!(entity instanceof TransferLinkEntity)) continue;
                LinkEntityContext ectx = entity.getContext();
                NodeHolder<BlockNode> holder1 = ectx.getFirst();
                NodeHolder<BlockNode> holder2 = ectx.getSecond();

                if (!(holder1.getNode() instanceof TransferBlockNode node1)) continue;
                if (!(holder2.getNode() instanceof TransferBlockNode node2)) continue;

                BlockPos bpos1 = holder1.getBlockPos();
                BlockPos bpos2 = holder2.getBlockPos();

                EnumMap<DyeColor, Vec3> map1 = positions.get(bpos1);
                if (map1 == null) continue;
                EnumMap<DyeColor, Vec3> map2 = positions.get(bpos2);
                if (map2 == null) continue;
                Vec3 vpos1 = map1.get(node1.color());
                if (vpos1 == null) continue;
                Vec3 vpos2 = map2.get(node2.color());
                if (vpos2 == null) continue;

                stack.pushPose();
                stack.translate(bpos1.getX() + vpos1.x() - cameraPos.x(),
                    bpos1.getY() + vpos1.y() - cameraPos.y(), bpos1.getZ() + vpos1.z() - cameraPos.z());
                Vec3 offset = vpos2.subtract(vpos1).add(Vec3.atLowerCornerOf(bpos2.subtract(bpos1)));

                RenderUtils.renderBeam(stack, provider, offset, node2.color().getFireworkColor(),
                    node1.color().getFireworkColor(), world.getGameTime(), ctx.tickDelta());

                stack.popPose();
            }
        }

        // Render the selection box around the selected node
        if (shouldRenderSelection()) {
            selectedNode = getSelectedNode(view, cameraPos, ctx.tickDelta(), positions);
            SelectedNode selected = selectedNode;

            if (selected != null) {
                stack.pushPose();
                stack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());

                // draw outline around the node itself
                VertexConsumer consumer = provider.getBuffer(RenderType.LINES);
                RenderUtils.drawBox(stack, consumer, selected.boundingBox(), 0xFFFFFFFF);

                // draw outline around the block its in
                NodeEntityContext ectx = selected.entity().getContext();
                BlockPos blockPos = ectx.getBlockPos();
                BlockState state = world.getBlockState(blockPos);
                VoxelShape outlineShape = state.getShape(world, blockPos, CollisionContext.of(player));
                outlineShape.forAllEdges(
                    (d, e, f, g, h, i) -> RenderUtils.drawLine(stack, consumer, (float) d + blockPos.getX(),
                        (float) e + blockPos.getY(), (float) f + blockPos.getZ(), (float) g + blockPos.getX(),
                        (float) h + blockPos.getY(), (float) i + blockPos.getZ(), 0xFFFFFFFF));

                stack.popPose();
            }
        } else {
            selectedNode = null;
        }

        stack.popPose();
    }

    private static Map<BlockPos, SortedEntities> sortNodeEntities(GraphView view) {
        Map<BlockPos, SortedEntities> sorted = new Object2ObjectLinkedOpenHashMap<>();

        Iterator<BlockGraph> graphIter = view.getAllGraphs().iterator();
        while (graphIter.hasNext()) {
            BlockGraph graph = graphIter.next();
            Iterator<NodeEntity> nodeIter = graph.getNodeEntities().iterator();
            while (nodeIter.hasNext()) {
                NodeEntity entity = nodeIter.next();
                if (!(entity instanceof TransferNodeEntity transferEntity)) continue;
                NodeEntityContext ctx = entity.getContext();

                BlockNode node = ctx.getNode();
                if (!(node instanceof TransferBlockNode transferNode)) continue;

                // this array is like a hash map at home
                SortedEntities entities =
                    sorted.computeIfAbsent(ctx.getBlockPos(), _pos -> new SortedEntities());
                entities.array[transferNode.color().getId()] = transferEntity;
                entities.count++;
            }
        }

        return sorted;
    }

    private static Vec3 getPositionForIndex(int index, int count) {
        return RenderUtils.distributedEndpoint(count, index, 1.0 / 4.0, 1.0 / 8.0);
    }

    private static @Nullable SelectedNode getSelectedNode(GraphView view, Vec3 cameraPos, float tickDelta,
                                                          Map<BlockPos, EnumMap<DyeColor, Vec3>> positions) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return null;
        Entity cameraEntity = client.cameraEntity;
        if (cameraEntity == null) return null;

        // max raycast distance
        double reachDistance = player.isCreative() ? 5.0 : 4.5;
        double maxReachDistanceSqr = (reachDistance + 1.0) * (reachDistance + 1.0);

        // find the raycast start and end
        Vec3 rayStart = cameraPos;
        Vec3 direction = cameraEntity.getViewVector(tickDelta);
        Vec3 rayEnd = rayStart.add(direction.scale(reachDistance));

        // raycast variables
        double closestDistanceSqr = reachDistance * reachDistance;
        TransferNodeEntity selectedEntity = null;
        Vec3 intersectPosition = null;
        AABB selectedBox = null;

        // do the raycast
        Iterator<BlockGraph> graphIter = view.getAllGraphs().iterator();
        while (graphIter.hasNext()) {
            BlockGraph graph = graphIter.next();
            Iterator<NodeEntity> nodeIter = graph.getNodeEntities().iterator();
            while (nodeIter.hasNext()) {
                NodeEntity entity = nodeIter.next();
                if (!(entity instanceof TransferNodeEntity transferEntity)) continue;
                NodeEntityContext ctx = entity.getContext();
                BlockPos blockPos = ctx.getBlockPos();

                // we ignore everything outside our range as an optimization
                if (blockPos.distToCenterSqr(player.position()) < maxReachDistanceSqr) {
                    BlockNode node = ctx.getNode();
                    if (!(node instanceof TransferBlockNode transferNode)) continue;

                    // get the node's position within the block
                    EnumMap<DyeColor, Vec3> colors = positions.get(blockPos);
                    if (colors == null) continue;
                    Vec3 offset = colors.get(transferNode.color());
                    if (offset == null) continue;

                    // see if our look ray goes through the nodes' bounding boxes
                    AABB boundingBox = transferEntity.getBoundingBox()
                        .move(blockPos.getX() + offset.x - 0.5 / 4.0, blockPos.getY() + offset.y - 0.5 / 4.0,
                            blockPos.getZ() + offset.z - 0.5 / 4.0);
                    Optional<Vec3> raycast = boundingBox.clip(rayStart, rayEnd);

                    if (boundingBox.contains(rayStart)) {
                        // we're inside the node's bounding box, so just select it
                        if (closestDistanceSqr >= 0.0) {
                            selectedEntity = transferEntity;
                            intersectPosition = raycast.orElse(rayStart);
                            selectedBox = boundingBox;
                            closestDistanceSqr = 0.0;
                        }
                    } else if (raycast.isPresent()) {
                        // check to see if this is the closest node
                        Vec3 intersect = raycast.get();
                        double distanceSqr = rayStart.distanceToSqr(intersect);
                        if (distanceSqr < closestDistanceSqr) {
                            selectedEntity = transferEntity;
                            intersectPosition = intersect;
                            selectedBox = boundingBox;
                        }
                    }
                }
            }
        }

        return selectedEntity == null ? null : new SelectedNode(selectedEntity, intersectPosition, selectedBox);
    }

    private static class SortedEntities {
        final TransferNodeEntity[] array = new TransferNodeEntity[DyeColor.values().length];
        int count = 0;
    }
}
