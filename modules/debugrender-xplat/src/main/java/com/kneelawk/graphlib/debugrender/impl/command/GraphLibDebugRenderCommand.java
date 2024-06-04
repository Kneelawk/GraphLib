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

package com.kneelawk.graphlib.debugrender.impl.command;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.Listen;
import com.kneelawk.graphlib.api.GraphLib;
import com.kneelawk.graphlib.api.graph.GraphUniverse;
import com.kneelawk.graphlib.debugrender.impl.GLDebugNet;
import com.kneelawk.graphlib.impl.command.GraphLibCommand;
import com.kneelawk.graphlib.impl.event.InternalEvents;

import static net.minecraft.commands.Commands.literal;

public class GraphLibDebugRenderCommand {
    @Listen(InternalEvents.AddUniverseSubcommands.class)
    public static void addUniverseSubcommands(RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> universe) {
        universe.then(literal("debugrender")
            .then(literal("start")
                .executes(context -> startDebugRender(context.getSource(),
                    ResourceLocationArgument.getId(context, "universe")))
            )
            .then(literal("stop")
                .executes(context -> stopDebugRender(context.getSource(),
                    ResourceLocationArgument.getId(context, "universe")))
            )
        );
    }

    private static int startDebugRender(CommandSourceStack source, ResourceLocation universeId)
        throws CommandSyntaxException {
        if (!GraphLib.universeExists(universeId)) throw GraphLibCommand.UNKNOWN_UNIVERSE.create(universeId);

        GraphUniverse universe = GraphLib.getUniverse(universeId);

        GLDebugNet.startDebuggingPlayer(source.getPlayerOrException(), universe);
        return 15;
    }

    private static int stopDebugRender(CommandSourceStack source, ResourceLocation universeId)
        throws CommandSyntaxException {
        GLDebugNet.stopDebuggingPlayer(source.getPlayerOrException(), universeId);
        return 15;
    }
}
