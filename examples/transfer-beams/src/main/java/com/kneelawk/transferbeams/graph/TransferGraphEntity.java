/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
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

package com.kneelawk.transferbeams.graph;

import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;

import com.kneelawk.graphlib.api.graph.GraphEntityContext;
import com.kneelawk.graphlib.api.graph.user.AbstractGraphEntity;
import com.kneelawk.graphlib.api.graph.user.GraphEntityType;
import com.kneelawk.graphlib.api.graph.user.NodeEntity;
import com.kneelawk.graphlib.syncing.api.graph.user.GraphEntitySyncing;

import static com.kneelawk.transferbeams.TransferBeamsMod.id;

public class TransferGraphEntity extends AbstractGraphEntity<TransferGraphEntity> {
    private static final int TICKS_PER_UPDATE = 4;

    public static final GraphEntityType<TransferGraphEntity> TYPE = GraphEntityType.of(id("transfer"), TransferGraphEntity::new);
    public static final GraphEntitySyncing<TransferGraphEntity> SYNCING = GraphEntitySyncing.ofNoOp(TransferGraphEntity::new);

    private int tickCounter = 0;

    @Override
    public @NotNull GraphEntityType<?> getType() {
        return TYPE;
    }

    @Override
    public @Nullable NbtElement toTag() {
        return null;
    }

    @Override
    public void merge(@NotNull TransferGraphEntity other) {
        // nothing to merge at the moment
    }

    @Override
    public void onTick() {
        GraphEntityContext ctx = getContext();

        if (!ctx.getBlockWorld().isClient) {
            if (++tickCounter >= TICKS_PER_UPDATE) {
                tickCounter = 0;

                Iterator<NodeEntity> iterator = ctx.getGraph().getNodeEntities().iterator();
                while (iterator.hasNext()) {
                    NodeEntity entity = iterator.next();
                    if (entity instanceof ItemTransferNodeEntity item) {

                    }
                }
            }
        }
    }
}
