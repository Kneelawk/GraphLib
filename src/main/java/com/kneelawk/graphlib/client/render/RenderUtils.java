package com.kneelawk.graphlib.client.render;

import java.math.RoundingMode;
import java.util.Random;

import com.google.common.math.IntMath;

import org.jetbrains.annotations.NotNull;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RenderUtils {
    private static final Vec3d[][] PLANAR_VECTORS = {
        {new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 0.0, 1.0)},
        {new Vec3d(1.0, 0.0, 0.0), new Vec3d(0.0, 1.0, 0.0)},
        {new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 1.0, 0.0)}
    };

    public static void drawCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float width, float height, float depth, int color) {
        drawCube(stack, consumer, x, y, z, width, 0f, 0f, 0f, height, 0f, 0f, 0f, depth, color);
    }

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

    public static void drawRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX, float radY, Direction normal, int color) {
        Vec3d[] vecs = PLANAR_VECTORS[normal.ordinal() >> 1];
        drawRect(stack, consumer, x, y, z, (float) vecs[0].x * radX, (float) vecs[0].y * radX, (float) vecs[0].z * radX,
            (float) vecs[1].x * radY, (float) vecs[1].y * radY, (float) vecs[1].z * radY, color);
    }

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

    public static void drawLine(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x0, float y0,
                                float z0, float x1, float y1, float z1, int color) {
        Matrix4f model = stack.peek().getModel();
        Matrix3f normal = stack.peek().getNormal();

        float dx = x1 - x0;
        float dy = y1 - y0;
        float dz = z1 - z0;
        float fact = MathHelper.fastInverseSqrt(dx * dx + dy * dy + dz * dz);
        dx *= fact;
        dy *= fact;
        dz *= fact;

        Vector4f pos = model.transform(new Vector4f(x0, y0, z0, 1f));
        Vector3f norm = normal.transform(new Vector3f(dx, dy, dz));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).normal(norm.x, norm.y, norm.z).next();
        model.transform(pos.set(x1, y1, z1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).normal(norm.x, norm.y, norm.z).next();
    }

    public static void fillCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float width, float height, float depth, int color) {
        fillCube(stack, consumer, x, y, z, width, 0f, 0f, 0f, height, 0f, 0f, 0f, depth, color);
    }

    public static void fillCube(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                float radX2, float radY2, float radZ2, int color) {
        fillRect(stack, consumer, x - radX1, y - radY1, z - radZ1, radX0, radY0, radZ0, radX2, radY2, radZ2, color);
        fillRect(stack, consumer, x + radX1, y + radY1, z + radZ1, radX0, radY0, radZ0, -radX2, -radY2, -radZ2, color);
        fillRect(stack, consumer, x - radX2, y - radY2, z - radZ2, -radX0, -radY0, -radZ0, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x + radX2, y + radY2, z + radZ2, radX0, radY0, radZ0, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x - radX0, y - radY0, z - radZ0, radX2, radY2, radZ2, radX1, radY1, radZ1, color);
        fillRect(stack, consumer, x + radX0, y + radY0, z + radZ0, -radX2, -radY2, -radZ2, radX1, radY1, radZ1, color);
    }

    public static void fillRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX, float radY, Direction normal, int color) {
        Vec3d[] vecs = PLANAR_VECTORS[normal.ordinal() >> 1];
        fillRect(stack, consumer, x, y, z, (float) vecs[0].x * radX, (float) vecs[0].y * radX, (float) vecs[0].z * radX,
            (float) vecs[1].x * radY, (float) vecs[1].y * radY, (float) vecs[1].z * radY, color);
    }

    public static void fillRect(@NotNull MatrixStack stack, @NotNull VertexConsumer consumer, float x, float y, float z,
                                float radX0, float radY0, float radZ0, float radX1, float radY1, float radZ1,
                                int color) {
        Matrix4f model = stack.peek().getModel();

        Vector4f pos = model.transform(new Vector4f(x - radX0 + radX1, y - radY0 + radY1, z - radZ0 + radZ1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).next();
        model.transform(pos.set(x - radX0 - radX1, y - radY0 - radY1, z - radZ0 - radZ1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).next();
        model.transform(pos.set(x + radX0 - radX1, y + radY0 - radY1, z + radZ0 - radZ1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).next();
        model.transform(pos.set(x + radX0 + radX1, y + radY0 + radY1, z + radZ0 + radZ1, 1f));
        consumer.vertex(pos.x, pos.y, pos.z).color(color).next();
    }

    public static int graphColor(long graphId) {
        Random rand = new Random(graphId);
        return rand.nextInt() | 0xFF000000;
    }

    public static Vec3d distributedEndpoint(int nodesAtPos, int indexAmongNodes, Direction side, double verticalOffset,
                                            double spacing, double verticalSpacing) {
        Vec3d[] spacings = PLANAR_VECTORS[side.ordinal() >> 1];
        return distributedEndpoint(
            nodesAtPos, indexAmongNodes,
            0.5 + side.getOffsetX() * (0.5 - verticalOffset),
            0.5 + side.getOffsetY() * (0.5 - verticalOffset),
            0.5 + side.getOffsetZ() * (0.5 - verticalOffset),
            spacings[0].x * spacing, spacings[0].y * spacing, spacings[0].z * spacing,
            spacings[1].x * spacing, spacings[1].y * spacing, spacings[1].z * spacing,
            -side.getOffsetX() * verticalSpacing,
            -side.getOffsetY() * verticalSpacing,
            -side.getOffsetZ() * verticalSpacing
        );
    }

    public static Vec3d distributedEndpoint(int nodesAtPos, int indexAmongNodes, double spacing,
                                            double verticalSpacing) {
        return distributedEndpoint(nodesAtPos, indexAmongNodes, 0.5, 0.5, 0.5, spacing, 0.0, 0.0, 0.0, 0.0, spacing,
            0.0, verticalSpacing, 0.0);
    }

    public static Vec3d distributedEndpoint(int nodesAtPos, int indexAmongNodes, double centerX, double centerY,
                                            double centerZ, double spaceX0, double spaceY0, double spaceZ0,
                                            double spaceX1,
                                            double spaceY1, double spaceZ1, double offsetX, double offsetY,
                                            double offsetZ) {
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
