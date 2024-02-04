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

package com.kneelawk.graphlib.impl.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class InternalEvents {
    public static final Event<AddUniverseSubcommands> ADD_UNIVERSE_SUBCOMMANDS = EventFactory.createArrayBacked(
        AddUniverseSubcommands.class, listeners -> universe -> {
            for (AddUniverseSubcommands listener : listeners) {
                listener.addUniverseSubcommands(universe);
            }
        });

    public interface AddUniverseSubcommands {
        void addUniverseSubcommands(RequiredArgumentBuilder<ServerCommandSource, Identifier> universe);
    }
}
