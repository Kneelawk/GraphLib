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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import com.kneelawk.graphlib.api.util.NodePos;
import com.kneelawk.transferbeams.net.TBNet;
import com.kneelawk.transferbeams.proxy.CommonProxy;
import com.kneelawk.transferbeams.util.SelectedNode;

public class ConfigToolItem extends Item implements InteractionCancellerItem {
    public ConfigToolItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult interceptBlockUse(ItemStack stack, PlayerEntity player, World world, Hand hand,
                                          BlockHitResult hitResult) {
        if (world.isClient()) {
            SelectedNode node = CommonProxy.INSTANCE.getClientSelectedNode();
            if (node != null) {
                // sneaking means we remove the node
                NodePos pos = node.entity().getContext().getPos();
                if (player.isSneaking()) {
                    TBNet.sendNodeRemove(pos);
                } else {
                    TBNet.sendNodeActivate(pos);
                }

                return ActionResult.SUCCESS;
            } else {
                return ActionResult.FAIL;
            }
        } else {
            // This should not be processed on the server, as node clicks will be handled via a separate packet.
            return ActionResult.FAIL;
        }
    }
}
