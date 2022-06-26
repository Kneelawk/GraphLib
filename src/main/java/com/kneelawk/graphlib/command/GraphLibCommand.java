package com.kneelawk.graphlib.command;

import com.kneelawk.graphlib.Constants;
import com.kneelawk.graphlib.GraphLib;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

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
        );
    }

    private static int updateBlocks(ServerCommandSource source, BlockPos from, BlockPos to) {
        source.sendFeedback(Constants.command("graphlib.updateblocks.starting", blockPosText(from), blockPosText(to)),
                true);

        ServerWorld world = source.getWorld();
        GraphLib.getController(world).updateNodes(BlockPos.stream(from, to));

        source.sendFeedback(Constants.command("graphlib.updateblocks.success", blockPosText(from), blockPosText(to)),
                true);

        return 15;
    }

    private static int removeEmptyGraphsCommand(ServerCommandSource source) {
        int result = GraphLib.getController(source.getWorld()).removeEmptyGraphs();

        source.sendFeedback(Constants.command("graphlib.removeemptygraphs.success", result), true);

        return result;
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
