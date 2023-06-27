package com.kneelawk.graphlib.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Holder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.net.GLDebugNet;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GraphLibCommand {
    private static final DynamicCommandExceptionType UNKNOWN_UNIVERSE =
        new DynamicCommandExceptionType(arg -> new LiteralMessage("Unknown universe: " + arg));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("graphlib")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("list").executes(context -> listUniverses(context.getSource())))
            .then(argument("universe", IdentifierArgumentType.identifier())
                .then(literal("updateblocks")
                    .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                            .executes(context -> updateBlocks(context.getSource(),
                                IdentifierArgumentType.getIdentifier(context, "universe"),
                                BlockPosArgumentType.getBlockPos(context, "from"),
                                BlockPosArgumentType.getBlockPos(context, "to")))
                        )
                    )
                )
                .then(literal("removeemptygraphs")
                    .executes(context -> removeEmptyGraphsCommand(context.getSource(),
                        IdentifierArgumentType.getIdentifier(context, "universe")))
                )
                .then(literal("debugrender")
                    .then(literal("start")
                        .executes(context -> startDebugRender(context.getSource(),
                            IdentifierArgumentType.getIdentifier(context, "universe")))
                    )
                    .then(literal("stop")
                        .executes(context -> stopDebugRender(context.getSource(),
                            IdentifierArgumentType.getIdentifier(context, "universe")))
                    )
                )
            )
        );
    }

    private static int listUniverses(ServerCommandSource source) {
        MutableText msg = Text.literal("Universes:");

        for (RegistryKey<GraphUniverseImpl> key : GraphLibImpl.UNIVERSE.getKeys()) {
            msg.append("\n");
            msg.append(Text.literal(key.getValue().toString()).styled(style -> style.withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key.getValue().toString()))));
        }

        source.sendFeedback(() -> msg, false);

        return GraphLibImpl.UNIVERSE.size();
    }

    private static int updateBlocks(ServerCommandSource source, Identifier universeId, BlockPos from, BlockPos to)
        throws CommandSyntaxException {
        source.sendFeedback(
            () -> Constants.command("graphlib.updateblocks.starting", blockPosText(from), blockPosText(to)),
            true);

        ServerWorld world = source.getWorld();

        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        universe.getServerGraphWorld(world).updateNodes(BlockPos.stream(from, to));

        source.sendFeedback(
            () -> Constants.command("graphlib.updateblocks.success", blockPosText(from), blockPosText(to)),
            true);

        return 15;
    }

    private static int removeEmptyGraphsCommand(ServerCommandSource source, Identifier universeId)
        throws CommandSyntaxException {
        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        int result = universe.getServerGraphWorld(source.getWorld()).removeEmptyGraphs();

        source.sendFeedback(() -> Constants.command("graphlib.removeemptygraphs.success", result), true);

        return result;
    }

    private static int startDebugRender(ServerCommandSource source, Identifier universeId)
        throws CommandSyntaxException {
        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        GLDebugNet.startDebuggingPlayer(source.getPlayerOrThrow(), universe);
        return 15;
    }

    private static int stopDebugRender(ServerCommandSource source, Identifier universeId)
        throws CommandSyntaxException {
        GLDebugNet.stopDebuggingPlayer(source.getPlayerOrThrow(), universeId);
        return 15;
    }

    private static MutableText blockPosText(BlockPos pos) {
        return Texts.bracketed(Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
            .styled(
                style -> style.withColor(Formatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Text.translatable("chat.coordinates.tooltip")))
            );
    }
}
