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

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.api.util.CacheCategory;

/**
 * Manages server to client synchronization of a {@link GraphUniverse}
 */
public interface SyncedUniverse {

    /**
     * Registers this synchronization handler so that it can be found by its universe's id.
     * <p>
     * If this synchronization handler is not registered, it will not work.
     */
    void register();

    /**
     * Creates a new SyncedUniverse builder.
     *
     * @return a new builder for building a SyncedUniverse.
     */
    static @NotNull Builder builder() {
        // TODO
        throw new AssertionError("Not yet implemented");
    }

    interface Builder {
        /**
         * Builds a universe synchronization handler for the given universe.
         * <p>
         * <b>Note: This does not register synchronization handlers. Registration should be performed with the
         * {@link SyncedUniverse#register()} method.</b>
         *
         * @param universe the universe that this synchronization handler is to synchronize.
         * @return a new universe synchronization handler.
         */
        @NotNull SyncedUniverse build(@NotNull GraphUniverse universe);

        /**
         * Sets whether this graph universe should be synchronized to the client.
         * <p>
         * This is set to {@link SyncProfile#SYNC_NOTHING} by default.
         * <p>
         * The {@link CacheCategory} in the given sync profile will be automatically registered on universe creation.
         *
         * @param profile a profile describing whether and how this graph universe should be synchronized to the client.
         * @return this builder for call chaining.
         */
        @NotNull Builder synchronizeToClient(@NotNull SyncProfile profile);
    }
}
