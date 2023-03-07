package com.kneelawk.graphlib.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.graphlib.api.v1.GraphLib;
import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibCommonNetworking;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GraphLibCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("graphlib")
            .requires(source -> source.hasPermissionLevel(2))
            .then(literal("updateblocks")
                .then(argument("from", BlockPosArgumentType.blockPos())
                    .then(argument("to", BlockPosArgumentType.blockPos())
                        .executes(context -> updateBlocks(context.getSource(),
                            BlockPosArgumentType.getBlockPos(context, "from"),
                            BlockPosArgumentType.getBlockPos(context, "to")))
                    )
                )
            )
            .then(literal("removeemptygraphs").executes(context -> removeEmptyGraphsCommand(context.getSource())))
            .then(literal("debugrender")
                .then(literal("start")
                    .executes(context -> startDebugRender(context.getSource()))
                )
                .then(literal("stop")
                    .executes(context -> stopDebugRender(context.getSource()))
                )
            )
        );
    }

    private static int updateBlocks(ServerCommandSource source, BlockPos from, BlockPos to) {
        source.sendFeedback(Constants.command("graphlib.updateblocks.starting", blockPosText(from), blockPosText(to)),
            true);

        ServerWorld world = source.getWorld();
        GraphLib.getGraphWorld(world).updateNodes(BlockPos.stream(from, to));

        source.sendFeedback(Constants.command("graphlib.updateblocks.success", blockPosText(from), blockPosText(to)),
            true);

        return 15;
    }

    private static int removeEmptyGraphsCommand(ServerCommandSource source) {
        int result = GraphLib.getGraphWorld(source.getWorld()).removeEmptyGraphs();

        source.sendFeedback(Constants.command("graphlib.removeemptygraphs.success", result), true);

        return result;
    }

    private static int startDebugRender(ServerCommandSource source) throws CommandSyntaxException {
        GraphLibCommonNetworking.startDebuggingPlayer(source.getPlayer());
        return 15;
    }

    private static int stopDebugRender(ServerCommandSource source) throws CommandSyntaxException {
        GraphLibCommonNetworking.stopDebuggingPlayer(source.getPlayer());
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
