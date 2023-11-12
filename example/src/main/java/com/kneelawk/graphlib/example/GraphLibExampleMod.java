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

package com.kneelawk.graphlib.example;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.user.SyncProfile;
import com.kneelawk.graphlib.net.api.graph.SyncedUniverse;

public class GraphLibExampleMod implements ModInitializer {
    public static final String MOD_ID = "graphlib_example";

    public static final GraphUniverse UNIVERSE = GraphUniverse.builder().build(id("universe"));
    public static final SyncedUniverse SYNCED_UNIVERSE =
        SyncedUniverse.builder().synchronizeToClient(SyncProfile.SYNC_EVERYTHING).build(UNIVERSE);

    @Override
    public void onInitialize() {
        UNIVERSE.register();
        SYNCED_UNIVERSE.register();
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
