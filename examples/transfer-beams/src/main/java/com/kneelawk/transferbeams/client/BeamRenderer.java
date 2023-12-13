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

import java.util.Iterator;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;

import com.kneelawk.graphlib.api.graph.BlockGraph;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.api.graph.user.LinkEntity;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.kmodlib.client.overlay.RenderToOverlay;
import com.kneelawk.transferbeams.TransferBeamsMod;

public class BeamRenderer {
    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(BeamRenderer::plainRender);
        RenderToOverlay.EVENT.register(BeamRenderer::overlayRender);
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

        Iterator<BlockGraph> graphIter = view.getAllGraphs().iterator();
        while (graphIter.hasNext()) {
            BlockGraph graph = graphIter.next();

            Iterator<NodeEntity> nodeIter = graph.getNodeEntities().iterator();
            while (nodeIter.hasNext()) {
                NodeEntity entity = nodeIter.next();
            }

            Iterator<LinkEntity> linkIter = graph.getLinkEntities().iterator();
            while (linkIter.hasNext()) {
                LinkEntity entity = linkIter.next();
            }
        }
    }
}
