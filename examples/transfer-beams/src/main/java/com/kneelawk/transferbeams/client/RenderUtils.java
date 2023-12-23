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

import java.math.RoundingMode;

import com.google.common.math.IntMath;

import org.jetbrains.annotations.NotNull;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockRenderView;

public class RenderUtils {
    private static final MinecraftClient MC = MinecraftClient.getInstance();

    public static BakedModel getBakedModel(Identifier id) {
        BakedModelManager manager = MC.getBakedModelManager();
        return manager.getModel(id);
    }

    public static void renderModel(BakedModel model, BlockState state, MatrixStack stack, VertexConsumer consumer, int light) {
        BlockModelRenderer renderer = MC.getBlockRenderManager().getModelRenderer();
        renderer.render(stack.peek(), consumer, state, model, 1f, 1f, 1f, light, OverlayTexture.DEFAULT_UV);
    }

    public static void drawBox(MatrixStack stack, VertexConsumer consumer, Box box, int color) {
        Vec3d center = box.getCenter();
        drawCube(stack, consumer, (float) center.x, (float) center.y, (float) center.z,
            (float) (box.maxX - box.minX) / 2f, (float) (box.maxY - box.minY) / 2f, (float) (box.maxZ - box.minZ) / 2f,
            color);
    }

    // ripped from the debug-renderer module

    /**
     * Draws a cuboid made of lines.
     *
     * @param stack    the matrix stack.
     * @param consumer the vertex consumer.
     * @param x        the x position of the center.
     * @param y        the y position of the center.
     * @param z        the z position of the center.
     * @param width    the x-dimension radius of the cube.
     * @param height   the y-dimension radius of the cube.
     * @param depth    the z-dimension radius of the cube.
     * @param color    the color of the cube as an ARGB integer.
     */
    public static void drawCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float width, float height, float depth, int color) {
        drawCube(stack, consumer, x, y, z, width, 0f, 0f, 0f, height, 0f, 0f, 0f, depth, color);
    }

    /**
     * Draws a parallelepiped made of lines.
     *
     * @param stack    the matrix stack.
     * @param consumer the vertex consumer.
     * @param x        the x position of the center.
     * @param y        the y position of the center.
     * @param z        the z position of the center.
     * @param radX0    the x-component of the first radius vector.
     * @param radY0    the y-component of the first radius vector.
     * @param radZ0    the z-component of the first radius vector.
     * @param radX1    the x-component of the second radius vector.
     * @param radY1    the y-component of the second radius vector.
     * @param radZ1    the z-component of the second radius vector.
     * @param radX2    the x-component of the third radius vector.
     * @param radY2    the y-component of the third radius vector.
     * @param radZ2    the z-component of the third radius vector.
     * @param color    the color as an ARGB integer.
     */
    public static void drawCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                float radX2, float radY2, float radZ2, int color) {
        drawRect(stack, consumer, x - radX2, y - radY2, z - radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        drawRect(stack, consumer, x + radX2, y + radY2, z + radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        drawLine(stack, consumer, x - radX0 - radX1 - radX2, y - radY0 - radY1 - radY2, z - radZ0 - radZ1 - radZ2,
            x - radX0 - radX1 + radX2, y - radY0 - radY1 + radY2, z - radZ0 - radZ1 + radZ2, color);
        drawLine(stack, consumer, x - radX0 + radX1 - radX2, y - radY0 + radY1 - radY2, z - radZ0 + radZ1 - radZ2,
            x - radX0 + radX1 + radX2, y - radY0 + radY1 + radY2, z - radZ0 + radZ1 + radZ2, color);
        drawLine(stack, consumer, x + radX0 + radX1 - radX2, y + radY0 + radY1 - radY2, z + radZ0 + radZ1 - radZ2,
            x + radX0 + radX1 + radX2, y + radY0 + radY1 + radY2, z + radZ0 + radZ1 + radZ2, color);
        drawLine(stack, consumer, x + radX0 - radX1 - radX2, y + radY0 - radY1 - radY2, z + radZ0 - radZ1 - radZ2,
            x + radX0 - radX1 + radX2, y + radY0 - radY1 + radY2, z + radZ0 - radZ1 + radZ2, color);
    }

    /**
     * Draws a parallelogram made of lines in 3D space.
     *
     * @param stack    the matrix stack.
     * @param consumer the vertex consumer.
     * @param x        the x position of the center.
     * @param y        the y position of the center.
     * @param z        the z-position of the center.
     * @param radX0    the x-component of the first radius vector.
     * @param radY0    the y-component of the first radius vector.
     * @param radZ0    the z-component of the first radius vector.
     * @param radX1    the x-component of the second radius vector.
     * @param radY1    the y-component of the second radius vector.
     * @param radZ1    the z-component of the second radius vector.
     * @param color    the color as an ARGB integer.
     */
    public static void drawRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                int color) {
        drawLine(stack, consumer, x - radX0 - radX1, y - radY0 - radY1, z - radZ0 - radZ1, x - radX0 + radX1,
            y - radY0 + radY1, z - radZ0 + radZ1, color);
        drawLine(stack, consumer, x - radX0 + radX1, y - radY0 + radY1, z - radZ0 + radZ1, x + radX0 + radX1,
            y + radY0 + radY1, z + radZ0 + radZ1, color);
        drawLine(stack, consumer, x + radX0 + radX1, y + radY0 + radY1, z + radZ0 + radZ1, x + radX0 - radX1,
            y + radY0 - radY1, z + radZ0 - radZ1, color);
        drawLine(stack, consumer, x + radX0 - radX1, y + radY0 - radY1, z + radZ0 - radZ1, x - radX0 - radX1,
            y - radY0 - radY1, z - radZ0 - radZ1, color);
    }

    /**
     * Draws a line in 3D space.
     *
     * @param stack    the matrix stack.
     * @param consumer the vertex consumer.
     * @param x0       the x position of the first endpoint.
     * @param y0       the y position of the first endpoint.
     * @param z0       the z position of the first endpoint.
     * @param x1       the x position of the second endpoint.
     * @param y1       the y position of the second endpoint.
     * @param z1       the z position of the second endpoint.
     * @param color    the color as an ARGB integer.
     */
    public static void drawLine(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x0, float y0,
                                float z0, float x1, float y1, float z1, int color) {
        Matrix4f model = stack.peek().getModel();
        Matrix3f normal = stack.peek().getNormal();

        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float fact = MathHelper.inverseSqrt(dx * dx + dy * dy + dz * dz);
        dx *= fact;
        dy *= fact;
        dz *= fact;

        Vector4f pos = model.transform(new Vector4f(x0, y0, z0, 1f));
        Vector3f norm = normal.transform(new Vector3f(dx, dy, dz));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).normal(norm.x, norm.y, norm.z).next();
        model.transform(pos.set(x1, y1, z1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).normal(norm.x, norm.y, norm.z).next();
    }

    /**
     * Calculates the visual position of a block-node in the center of its block.
     *
     * @param nodesAtPos      the total number of nodes that are at the center of the given block.
     * @param indexAmongNodes the index of the current node among the nodes at the given position.
     * @param spacing         the horizontal distance between nodes.
     * @param verticalSpacing the distance that nodes of different rows are pushed vertically to avoid connections
     *                        clipping through nodes in other rows.
     * @return the visual position of the block-node.
     */
    public static Vec3d distributedEndpoint(int nodesAtPos, int indexAmongNodes, double spacing,
                                            double verticalSpacing) {
        return distributedEndpoint(nodesAtPos, indexAmongNodes, 0.5, 0.5, 0.5, spacing, 0.0, 0.0, 0.0, 0.0, spacing,
            0.0, verticalSpacing, 0.0);
    }

    /**
     * Calculates the visual position of a block-node.
     *
     * @param nodesAtPos      the total number of nodes that are at the same position as this node.
     * @param indexAmongNodes the index of the current node among the nodes at the given position.
     * @param centerX         the x position of the center of the cluster of nodes.
     * @param centerY         the y position of the center of the cluster of nodes.
     * @param centerZ         the z position of the center of the cluster of nodes.
     * @param spaceX0         the x-component of the first spacing vector.
     * @param spaceY0         the y-component of the first spacing vector.
     * @param spaceZ0         the z-component of the first spacing vector.
     * @param spaceX1         the x-component of the second spacing vector.
     * @param spaceY1         the y-component of the second spacing vector.
     * @param spaceZ1         the z-component of the second spacing vector.
     * @param offsetX         the x-component of the vertical spacing between rows.
     * @param offsetY         the y-component of the vertical spacing between rows.
     * @param offsetZ         the z-component of the vertical spacing between rows.
     * @return the visual position of the block-node.
     */
    public static Vec3d distributedEndpoint(int nodesAtPos, int indexAmongNodes, double centerX, double centerY,
                                            double centerZ, double spaceX0, double spaceY0, double spaceZ0,
                                            double spaceX1, double spaceY1, double spaceZ1, double offsetX,
                                            double offsetY, double offsetZ) {
        if (nodesAtPos < 2) {
            return new Vec3d(centerX, centerY, centerZ);
        }

        int width = IntMath.sqrt(nodesAtPos, RoundingMode.CEILING);
        int indexX = indexAmongNodes % width;
        int indexY = indexAmongNodes / width;

        double posX = (double) indexX - (double) (width - 1) / 2.0;
        double posY = (double) indexY - (double) (width - 1) / 2.0;
        double posZ = (double) indexX + (double) indexY - (double) (width - 1);

        return new Vec3d(centerX + posX * spaceX0 + posY * spaceX1 + posZ * offsetX,
            centerY + posX * spaceY0 + posY * spaceY1 + posZ * offsetY,
            centerZ + posX * spaceZ0 + posY * spaceZ1 + posZ * offsetZ);
    }
}
