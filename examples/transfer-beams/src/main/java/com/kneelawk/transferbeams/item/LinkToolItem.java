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

package com.kneelawk.transferbeams.item;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.graph.GraphWorld;
import com.kneelawk.graphlib.api.graph.LinkHolder;
import com.kneelawk.graphlib.api.graph.NodeHolder;
import com.kneelawk.graphlib.api.graph.user.BlockNode;
import com.kneelawk.graphlib.api.graph.user.LinkKey;
import com.kneelawk.graphlib.api.util.LinkPos;
import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.transferbeams.TransferBeamsMod;
import com.kneelawk.transferbeams.graph.TransferLinkKey;
import com.kneelawk.transferbeams.net.TBNet;
import com.kneelawk.transferbeams.proxy.CommonProxy;
import com.kneelawk.transferbeams.util.SelectedNode;

import static com.kneelawk.transferbeams.TransferBeamsMod.str;

public class LinkToolItem extends Item implements InteractionCancellerItem {
    private static final String NODE_POS_KEY = str("node_pos");

    private static boolean hasNodePos(ItemStack stack) {
        return stack.getSubNbt(NODE_POS_KEY) != null;
    }

    private static void setNodePos(ItemStack stack, NodePos pos) {
        stack.setSubNbt(NODE_POS_KEY, pos.toNbt());
    }

    private static @Nullable NodePos getNodePos(ItemStack stack) {
        NbtCompound nbt = stack.getSubNbt(NODE_POS_KEY);
        return nbt == null ? null : NodePos.fromNbt(nbt, TransferBeamsMod.UNIVERSE);
    }

    private static void removeNodePos(ItemStack stack) {
        stack.removeSubNbt(NODE_POS_KEY);
    }

    public static void onNodeClick(PlayerEntity player, GraphWorld world, NodePos pos) {
        ItemStack stack = player.getMainHandStack();

        if (stack.isOf(TransferBeamsMod.LINK_TOOL_ITEM)) {
            if (world.nodeExistsAt(pos)) {
                if (player.isSneaking()) {
                    // sift-right-click disconnects everything
                    NodeHolder<BlockNode> holder = world.getNodeAt(pos);
                    assert holder != null;
                    // copy connections so we don't do concurrent modification
                    List<LinkHolder<LinkKey>> connections = new ArrayList<>(holder.getConnections());
                    for (LinkHolder<LinkKey> connection : connections) {
                        world.disconnectNodes(connection.getPos());
                    }
                } else {
                    // normal right-click connects two nodes
                    NodePos prevPos = getNodePos(stack);
                    if (prevPos != null) {
                        if (!prevPos.equals(pos)) {
                            LinkPos linkPos = new LinkPos(prevPos, pos, TransferLinkKey.INSTANCE);
                            if (world.linkExistsAt(linkPos)) {
                                world.disconnectNodes(linkPos);
                            } else {
                                world.connectNodes(linkPos);
                            }
                        }

                        removeNodePos(stack);
                    } else {
                        setNodePos(stack, pos);
                    }
                }
            } else {
                TransferBeamsMod.LOG.warn("Received node click for node that does not exist: {}", pos);
            }
        }
    }

    public LinkToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult interceptBlockUse(ItemStack stack, PlayerEntity player, World world, Hand hand,
                                          BlockHitResult hitResult) {
        if (world.isClient()) {
            SelectedNode node = CommonProxy.INSTANCE.getClientSelectedNode();
            if (node != null) {
                TBNet.sendNodeLink(node.entity().getContext().getPos());

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        } else {
            // This should not be processed on the server, as node clicks will be handled via a separate packet.
            return ActionResult.FAIL;
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasNodePos(stack);
    }
}
