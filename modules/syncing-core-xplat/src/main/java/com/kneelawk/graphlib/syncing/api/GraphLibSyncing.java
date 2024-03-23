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

package com.kneelawk.graphlib.syncing.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.impl.GraphLibSyncingImpl;

/**
 * Graph Lib Synchronization public API. This class contains static methods and fields for interacting with GraphLib's
 * Synchronization API.
 */
public class GraphLibSyncing {
    private GraphLibSyncing() {}

    /**
     * Gets whether the given universe has syncing enabled.
     *
     * @param id the id of the universe to check.
     * @return whether the given universe has syncing enabled.
     */
    public static boolean syncingEnabled(@NotNull Identifier id) {
        return GraphLibSyncingImpl.SYNCED_UNIVERSE.containsKey(id);
    }

    /**
     * Gets whether the given universe has syncing enabled.
     *
     * @param universe the universe to check.
     * @return whether the given universe has syncing enabled.
     */
    public static boolean syncingEnabled(@NotNull GraphUniverse universe) {
        return syncingEnabled(universe.getId());
    }

    /**
     * Gets a registered {@link SyncedUniverse} by its id.
     *
     * @param id the id of the universe to look up.
     * @return the universe with the given id.
     */
    public static SyncedUniverse getUniverse(@NotNull Identifier id) {
        SyncedUniverse universe = GraphLibSyncingImpl.SYNCED_UNIVERSE.get(id);
        if (universe == null) throw new IllegalArgumentException("No synced universe exists with name " + id);

        return universe;
    }

    /**
     * Gets a registered {@link SyncedUniverse} by the universe it's associated with.
     *
     * @param universe the universe associated with the synced universe to look up.
     * @return the synced universe associated with the given universe.
     */
    public static SyncedUniverse getUniverse(@NotNull GraphUniverse universe) {
        return getUniverse(universe.getId());
    }

    /**
     * Gets a registered {@link SyncedUniverse} for a world in the universe the synced universe is associated with.
     *
     * @param view the world within the universe associated with the synced universe to look up.
     * @return the synced universe associated with the universe that the given view is within.
     */
    public static SyncedUniverse getUniverse(@NotNull GraphView view) {
        return getUniverse(view.getUniverse().getId());
    }
}
