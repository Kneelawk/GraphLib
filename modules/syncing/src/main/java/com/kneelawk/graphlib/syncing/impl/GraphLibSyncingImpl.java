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

package com.kneelawk.graphlib.syncing.impl;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.api.graph.GraphView;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.ServerGraphWorldImpl;
import com.kneelawk.graphlib.impl.graph.listener.WorldListener;
import com.kneelawk.graphlib.syncing.api.graph.SyncedUniverse;
import com.kneelawk.graphlib.syncing.impl.graph.WorldEncoder;
import com.kneelawk.graphlib.syncing.impl.graph.SyncedUniverseImpl;

public class GraphLibSyncingImpl {
    private static final Identifier SYNCED_UNIVERSE_IDENTIFIER = SyncedConstants.id("synced_universe");
    public static final RegistryKey<Registry<SyncedUniverseImpl>> SYNCED_UNIVERSE_KEY =
        RegistryKey.ofRegistry(SYNCED_UNIVERSE_IDENTIFIER);

    public static final Registry<SyncedUniverseImpl> SYNCED_UNIVERSE =
        new SimpleRegistry<>(SYNCED_UNIVERSE_KEY, Lifecycle.stable(), false);

    @SuppressWarnings("unchecked")
    static void register() {
        Registry.register((Registry<Registry<?>>) Registries.REGISTRY, SYNCED_UNIVERSE_IDENTIFIER, SYNCED_UNIVERSE);
    }

    public static void register(SyncedUniverseImpl universe) {
        if (!(universe.getUniverse() instanceof GraphUniverseImpl universeImpl)) throw new IllegalArgumentException(
            "Attempted to register a SyncedUniverse with a non-GraphLib GraphUniverse implementation: " +
                universe.getClass());

        Registry.register(SYNCED_UNIVERSE, universe.getId(), universe);
        universeImpl.addListener(SyncedConstants.LISTENER_KEY, universe);
    }

    public static WorldEncoder getEncoder(ServerGraphWorldImpl world) {
        WorldListener listener = world.getListener(SyncedConstants.LISTENER_KEY);
        if (!(listener instanceof WorldEncoder synced)) throw new IllegalStateException(
            "A SyncedUniverse was never registered with the universe: " + world.getUniverse().getId());

        return synced;
    }
}
