package com.kneelawk.graphlib.impl.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.registry.Holder;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibCommonNetworking;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GraphLibCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(literal("graphlib")
            .requires(source -> source.hasPermissionLevel(2))
            .then(argument("universe", RegistryEntryArgumentType.registryEntry(buildContext, GraphLibImpl.UNIVERSE_KEY))
                .then(literal("updateblocks")
                    .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                            .executes(context -> updateBlocks(context.getSource(),
                                RegistryEntryArgumentType.getRegistryEntry(context, "universe",
                                    GraphLibImpl.UNIVERSE_KEY),
                                BlockPosArgumentType.getBlockPos(context, "from"),
                                BlockPosArgumentType.getBlockPos(context, "to")))
                        )
                    )
                )
                .then(literal("removeemptygraphs")
                    .executes(context -> removeEmptyGraphsCommand(context.getSource(),
                        RegistryEntryArgumentType.getRegistryEntry(context, "universe", GraphLibImpl.UNIVERSE_KEY)))
                )
                .then(literal("debugrender")
                    .then(literal("start")
                        .executes(context -> startDebugRender(context.getSource(),
                            RegistryEntryArgumentType.getRegistryEntry(context, "universe", GraphLibImpl.UNIVERSE_KEY)))
                    )
                    .then(literal("stop")
                        .executes(context -> stopDebugRender(context.getSource(),
                            RegistryEntryArgumentType.getRegistryEntry(context, "universe", GraphLibImpl.UNIVERSE_KEY)))
                    )
                )
                .then(literal("rebuildindexchunks")
                    .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                            .executes(context -> rebuildIndexChunks(context.getSource(),
                                RegistryEntryArgumentType.getRegistryEntry(context, "universe",
                                    GraphLibImpl.UNIVERSE_KEY),
                                BlockPosArgumentType.getBlockPos(context, "from"),
                                BlockPosArgumentType.getBlockPos(context, "to")))
                        )
                    )
                )
            )
        );
    }

    private static int updateBlocks(ServerCommandSource source, Holder.Reference<GraphUniverseImpl> universe,
                                    BlockPos from, BlockPos to) {
        source.sendFeedback(
            () -> Constants.command("graphlib.updateblocks.starting", blockPosText(from), blockPosText(to)),
            true);

        ServerWorld world = source.getWorld();
        universe.value().getGraphWorld(world).updateNodes(BlockPos.stream(from, to));

        source.sendFeedback(
            () -> Constants.command("graphlib.updateblocks.success", blockPosText(from), blockPosText(to)),
            true);

        return 15;
    }

    private static int removeEmptyGraphsCommand(ServerCommandSource source,
                                                Holder.Reference<GraphUniverseImpl> universe) {
        int result = universe.value().getGraphWorld(source.getWorld()).removeEmptyGraphs();

        source.sendFeedback(() -> Constants.command("graphlib.removeemptygraphs.success", result), true);

        return result;
    }

    private static int startDebugRender(ServerCommandSource source, Holder.Reference<GraphUniverseImpl> universe)
        throws CommandSyntaxException {
        GraphLibCommonNetworking.startDebuggingPlayer(source.getPlayerOrThrow(), universe.value());
        return 15;
    }

    private static int stopDebugRender(ServerCommandSource source, Holder.Reference<GraphUniverseImpl> universe)
        throws CommandSyntaxException {
        GraphLibCommonNetworking.stopDebuggingPlayer(source.getPlayerOrThrow(), universe.getRegistryKey().getValue());
        return 15;
    }

    private static int rebuildIndexChunks(ServerCommandSource source, Holder.Reference<GraphUniverseImpl> universe,
                                          BlockPos from, BlockPos to) {
        source.sendFeedback(
            () -> Constants.command("graphlib.rebuildindexchunks.starting", blockPosText(from), blockPosText(to)),
            true);

        ServerWorld world = source.getWorld();
        ChunkSectionPos start = ChunkSectionPos.from(from);
        ChunkSectionPos end = ChunkSectionPos.from(to);
        universe.value().getGraphWorld(world).rebuildIndexChunks(
            ChunkSectionPos.stream(min(start.getSectionX(), end.getSectionX()),
                min(start.getSectionY(), end.getSectionY()), min(start.getSectionZ(), end.getSectionZ()),
                max(start.getSectionX(), end.getSectionX()), max(start.getSectionY(), end.getSectionY()),
                max(start.getSectionZ(), start.getSectionZ())), () -> source.sendFeedback(
                () -> Constants.command("graphlib.rebuildindexchunks.success", blockPosText(from),
                    blockPosText(to)),
                true));

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
