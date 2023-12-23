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

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.NodeEntityContext;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.kmodlib.client.overlay.RenderToOverlay;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.TransferBlockNode;
import com.kneelawk.transferbeams.graph.TransferNodeEntity;
import com.kneelawk.transferbeams.util.SelectedNode;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class BeamRenderer {
    private static final Identifier[] ITEM_NODE_MODELS = new Identifier[DyeColor.values().length];

    public static @Nullable SelectedNode selectedNode = null;

    static {
        for (DyeColor color : DyeColor.values()) {
            ITEM_NODE_MODELS[color.getId()] = id("block/" + color.getName() + "_item_transfer_node");
        }
    }

    public static void init() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(BeamRenderer::beforeBlockOutline);
        WorldRenderEvents.AFTER_ENTITIES.register(BeamRenderer::plainRender);
        RenderToOverlay.EVENT.register(BeamRenderer::overlayRender);

        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(ITEM_NODE_MODELS));
    }

    private static boolean shouldRenderSelection() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return false;

        for (ItemStack stack : player.getItemsHand()) {
            if (stack.isIn(TransferBeamsMod.NODE_SELECTORS)) {
                return true;
            }
        }

        return false;
    }

    private static boolean beforeBlockOutline(WorldRenderContext ctx, @Nullable HitResult hit) {
        return !shouldRenderSelection();
    }

    private static boolean shouldRenderToOverlay() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return false;

        for (ItemStack stack : player.getItemsHand()) {
            if (stack.isIn(TransferBeamsMod.NODE_VISUALIZERS)) {
                return true;
            }
        }

        return false;
    }

    private static void plainRender(WorldRenderContext ctx) {
        if (!shouldRenderToOverlay()) render(ctx);
    }

    private static void overlayRender(WorldRenderContext ctx) {
        if (shouldRenderToOverlay()) render(ctx);
    }

    private static void render(WorldRenderContext ctx) {
        GraphView view = TransferBeamsMod.SYNCED.getClientGraphView();
        if (view == null) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return;

        MatrixStack stack = ctx.matrixStack();
        ClientWorld world = ctx.world();
        VertexConsumerProvider provider = ctx.consumers();
        Vec3d cameraPos = ctx.camera().getPos();
        assert provider != null;

        // We want the nodes to always be positioned by color.
        Map<BlockPos, SortedEntities> sorted = sortNodeEntities(view);
        Map<BlockPos, EnumMap<DyeColor, Vec3d>> positions = new Object2ObjectLinkedOpenHashMap<>();

        for (SortedEntities entities : sorted.values()) {
            int index = 0;
            for (DyeColor color : DyeColor.values()) {
                // the array only holds elements for the colors that are actually in the given block
                TransferNodeEntity entity = entities.array[color.getId()];
                if (entity == null) continue;
                NodeEntityContext ectx = entity.getContext();
                BlockPos blockPos = ectx.getBlockPos();
                BlockState state = ectx.getBlockState();

                Vec3d position = getPositionForIndex(index, entities.count);

                // remember this position for when we render the links
                EnumMap<DyeColor, Vec3d> colorPositions =
                    positions.computeIfAbsent(blockPos, _pos -> new EnumMap<>(DyeColor.class));
                colorPositions.put(color, position);

                stack.push();

                // correctly position the stack
                stack.translate(blockPos.getX() - cameraPos.getX() + position.x,
                    blockPos.getY() - cameraPos.getY() + position.y, blockPos.getZ() - cameraPos.getZ() + position.z);
                stack.scale(0.25f, 0.25f, 0.25f);
                stack.translate(-0.5f, -0.5f, -0.5f);

                // actually render the model
                BakedModel model = RenderUtils.getBakedModel(ITEM_NODE_MODELS[color.getId()]);
                RenderUtils.renderModel(model, state, stack, provider.getBuffer(RenderLayer.getCutout()),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE);

                stack.pop();

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
            }
        }

        // Render the selection box around the selected node
        if (shouldRenderSelection()) {
            selectedNode = getSelectedNode(view, cameraPos, ctx.tickDelta(), positions);
            SelectedNode selected = selectedNode;

            if (selected != null) {
                stack.push();
                stack.translate(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ());

                // draw outline around the node itself
                VertexConsumer consumer = provider.getBuffer(RenderLayer.LINES);
                RenderUtils.drawBox(stack, consumer, selected.boundingBox(), 0xFFFFFFFF);

                // draw outline around the block its in
                NodeEntityContext ectx = selected.entity().getContext();
                BlockPos blockPos = ectx.getBlockPos();
                BlockState state = world.getBlockState(blockPos);
                VoxelShape outlineShape = state.getOutlineShape(world, blockPos, ShapeContext.of(player));
                outlineShape.forEachEdge(
                    (d, e, f, g, h, i) -> RenderUtils.drawLine(stack, consumer, (float) d + blockPos.getX(),
                        (float) e + blockPos.getY(), (float) f + blockPos.getZ(), (float) g + blockPos.getX(),
                        (float) h + blockPos.getY(), (float) i + blockPos.getZ(), 0xFFFFFFFF));

                stack.pop();
            }
        } else {
            selectedNode = null;
        }
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

    private static Vec3d getPositionForIndex(int index, int count) {
        return RenderUtils.distributedEndpoint(count, index, 1.0 / 4.0, 1.0 / 8.0);
    }

    private static @Nullable SelectedNode getSelectedNode(GraphView view, Vec3d cameraPos, float tickDelta,
                                                          Map<BlockPos, EnumMap<DyeColor, Vec3d>> positions) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) return null;
        Entity cameraEntity = client.cameraEntity;
        if (cameraEntity == null) return null;

        // max raycast distance
        double reachDistance = player.isCreative() ? 5.0 : 4.5;
        double maxReachDistanceSqr = (reachDistance + 1.0) * (reachDistance + 1.0);

        // find the raycast start and end
        Vec3d rayStart = cameraPos;
        Vec3d direction = cameraEntity.getRotationVec(tickDelta);
        Vec3d rayEnd = rayStart.add(direction.multiply(reachDistance));

        // raycast variables
        double closestDistanceSqr = reachDistance * reachDistance;
        TransferNodeEntity selectedEntity = null;
        Vec3d intersectPosition = null;
        Box selectedBox = null;

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
                if (blockPos.getSquaredDistanceToCenter(player.getPos()) < maxReachDistanceSqr) {
                    BlockNode node = ctx.getNode();
                    if (!(node instanceof TransferBlockNode transferNode)) continue;

                    // get the node's position within the block
                    EnumMap<DyeColor, Vec3d> colors = positions.get(blockPos);
                    if (colors == null) continue;
                    Vec3d offset = colors.get(transferNode.color());
                    if (offset == null) continue;

                    // see if our look ray goes through the nodes' bounding boxes
                    Box boundingBox = transferEntity.getBoundingBox()
                        .offset(blockPos.getX() + offset.x - 0.5 / 4.0, blockPos.getY() + offset.y - 0.5 / 4.0,
                            blockPos.getZ() + offset.z - 0.5 / 4.0);
                    Optional<Vec3d> raycast = boundingBox.raycast(rayStart, rayEnd);

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
                        Vec3d intersect = raycast.get();
                        double distanceSqr = rayStart.squaredDistanceTo(intersect);
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
