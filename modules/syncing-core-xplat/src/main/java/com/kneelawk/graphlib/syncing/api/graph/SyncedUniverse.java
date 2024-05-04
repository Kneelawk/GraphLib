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

package com.kneelawk.graphlib.syncing.api.graph;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.syncing.api.graph.user.SyncProfile;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Manages server to client synchronization of a {@link GraphUniverse}
 */
public interface SyncedUniverse {
    /**
     * Gets the unique id of this universe.
     *
     * @return this universe's unique id.
     */
    @NotNull
    ResourceLocation getId();

    /**
     * Gets the graph universe that this handler synchronizes.
     *
     * @return the universe associated with this synchronization handler.
     */
    @NotNull
    GraphUniverse getUniverse();

    /**
     * Gets the {@link GraphView} for the given {@link Level}.
     * <p>
     * This works on both the logical server and the logical client, unlike
     * {@link GraphUniverse#getGraphWorld(ServerLevel)} which only works on the logical server.
     *
     * @param world the world to get the graph view associated with.
     * @return the graph view associated with the given world.
     */
    @Nullable
    GraphView getSidedGraphView(@NotNull Level world);

    /**
     * Gets the {@link GraphView} of this client, if this is indeed a physical client.
     *
     * @return the graph view of this client, if this is indeed a physical client, or <code>null</code> if this is a
     * physical server.
     */
    @Nullable
    GraphView getClientGraphView();

    /**
     * Registers this synchronization handler so that it can be found by its universe's id.
     * <p>
     * If this synchronization handler is not registered, it will not work.
     */
    void register();

    /**
     * Gets this universe's synchronization profile.
     *
     * @return this universe's synchronization profile.
     */
    @NotNull
    SyncProfile getSyncProfile();
}
