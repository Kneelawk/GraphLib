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

package com.kneelawk.graphlib.api.graph.user;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.kneelawk.graphlib.api.util.CacheCategory;

/**
 * Describes if and how a universe should be synchronized from the server to the client.
 */
public class SyncProfile {
    private final boolean synchronize;
    private final @NotNull PlayerSyncFilter playerFilter;
    private final @Nullable CacheCategory<?> nodeFilter;

    private SyncProfile(boolean synchronize, @NotNull PlayerSyncFilter playerFilter,
                        @Nullable CacheCategory<?> nodeFilter) {
        this.synchronize = synchronize;
        this.playerFilter = playerFilter;
        this.nodeFilter = nodeFilter;
    }

    /**
     * Gets whether the universe should be synchronized at all.
     *
     * @return <code>true</code> if the universe should be synchronized at all.
     */
    public boolean isSynchronized() {
        return synchronize;
    }

    /**
     * Gets the player filter.
     *
     * @return the player filter.
     */
    public PlayerSyncFilter getPlayerFilter() {
        return playerFilter;
    }

    /**
     * Gets the node filter.
     *
     * @return the node filter.
     */
    public CacheCategory<?> getNodeFilter() {
        return nodeFilter;
    }

    /**
     * A sync profile that does not synchronize anything to the client.
     */
    public static final @NotNull SyncProfile SYNC_NOTHING = new SyncProfile(false, player -> false, null);

    /**
     * A sync profile that synchronizes everything to the client.
     */
    public static final @NotNull SyncProfile SYNC_EVERYTHING = new SyncProfile(true, player -> true, null);

    /**
     * Creates a sync profile that synchronizes some things to some clients.
     *
     * @param playerFilter the filter for which clients to synchronize to.
     * @param nodeFilter   the filter for which nodes to synchronize.
     * @return a new sync profile with the given parameters.
     */
    public static @NotNull SyncProfile of(@NotNull PlayerSyncFilter playerFilter,
                                          @NotNull CacheCategory<?> nodeFilter) {
        return new SyncProfile(true, playerFilter, nodeFilter);
    }

    /**
     * Creates a sync profile that synchronizes to some clients.
     *
     * @param playerFilter the filter for which clients to synchronize to.
     * @return a new sync profile with the given parameters.
     */
    public static @NotNull SyncProfile of(@NotNull PlayerSyncFilter playerFilter) {
        return new SyncProfile(true, playerFilter, null);
    }

    /**
     * Creates a new sync profile that synchronizes some things to the client.
     *
     * @param nodeFilter the filter for which nodes to synchronize.
     * @return a new sync profile with the given parameters.
     */
    public static @NotNull SyncProfile of(@NotNull CacheCategory<?> nodeFilter) {
        return new SyncProfile(true, player -> true, nodeFilter);
    }
}
