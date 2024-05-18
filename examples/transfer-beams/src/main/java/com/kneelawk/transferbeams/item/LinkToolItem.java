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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
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
        return stack.getTagElement(NODE_POS_KEY) != null;
    }

    private static void setNodePos(ItemStack stack, NodePos pos) {
        stack.addTagElement(NODE_POS_KEY, pos.toNbt());
    }

    private static @Nullable NodePos getNodePos(ItemStack stack) {
        CompoundTag nbt = stack.getTagElement(NODE_POS_KEY);
        return nbt == null ? null : NodePos.fromNbt(nbt, TransferBeamsMod.UNIVERSE);
    }

    private static void removeNodePos(ItemStack stack) {
        stack.removeTagKey(NODE_POS_KEY);
    }

    public static void onNodeClick(Player player, GraphWorld world, NodePos pos) {
        ItemStack stack = player.getMainHandItem();

        if (stack.is(TransferBeamsMod.LINK_TOOL_ITEM)) {
            if (world.nodeExistsAt(pos)) {
                if (player.isShiftKeyDown()) {
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

    public LinkToolItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult interceptBlockUse(ItemStack stack, Player player, Level world, InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (world.isClientSide()) {
            SelectedNode node = CommonProxy.INSTANCE.getClientSelectedNode();
            if (node != null) {
                TBNet.sendNodeLink(node.entity().getContext().getPos());

                return InteractionResult.SUCCESS;
            } else {
                return InteractionResult.FAIL;
            }
        } else {
            // This should not be processed on the server, as node clicks will be handled via a separate packet.
            return InteractionResult.FAIL;
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasNodePos(stack);
    }
}
