package com.kneelawk.graphlib.impl.command;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.event.InternalEvents;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.RebuildChunksListener;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class GraphLibCommand {
    public static final DynamicCommandExceptionType UNKNOWN_UNIVERSE =
        new DynamicCommandExceptionType(arg -> new LiteralMessage("Unknown universe: " + arg));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> universeBuilder =
            argument("universe", ResourceLocationArgument.id())
                .suggests((context, builder) -> {
                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                    SharedSuggestionProvider.filterResources(GraphLibImpl.UNIVERSE.keySet(), remaining,
                        Function.identity(), id -> builder.suggest(id.toString()));
                    return builder.buildFuture();
                })
                .then(literal("updateblocks")
                    .then(argument("from", BlockPosArgument.blockPos())
                        .then(argument("to", BlockPosArgument.blockPos())
                            .executes(context -> updateBlocks(context.getSource(),
                                ResourceLocationArgument.getId(context, "universe"),
                                BlockPosArgument.getBlockPos(context, "from"),
                                BlockPosArgument.getBlockPos(context, "to")))
                        )
                    )
                )
                .then(literal("removeemptygraphs")
                    .executes(context -> removeEmptyGraphsCommand(context.getSource(),
                        ResourceLocationArgument.getId(context, "universe")))
                )
                .then(literal("rebuildchunks")
                    .then(argument("from", BlockPosArgument.blockPos())
                        .then(argument("to", BlockPosArgument.blockPos())
                            .executes(context -> rebuildChunks(context.getSource(),
                                ResourceLocationArgument.getId(context, "universe"),
                                BlockPosArgument.getBlockPos(context, "from"),
                                BlockPosArgument.getBlockPos(context, "to")))
                        )
                    )
                );

        InternalEvents.ADD_UNIVERSE_SUBCOMMANDS.invoker().addUniverseSubcommands(universeBuilder);

        dispatcher.register(literal("graphlib")
            .requires(source -> source.hasPermission(2))
            .then(literal("list").executes(context -> listUniverses(context.getSource())))
            .then(universeBuilder)
        );
    }

    private static int listUniverses(CommandSourceStack source) {
        MutableComponent msg = Component.literal("Universes:");

        for (ResourceLocation key : GraphLibImpl.UNIVERSE.keySet()) {
            msg.append("\n");
            msg.append(Component.literal(key.toString()).withStyle(style -> style.withColor(ChatFormatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key.toString()))));
        }

        source.sendSuccess(() -> msg, false);

        return GraphLibImpl.UNIVERSE.size();
    }

    private static int updateBlocks(CommandSourceStack source, ResourceLocation universeId, BlockPos from, BlockPos to)
        throws CommandSyntaxException {
        source.sendSuccess(
            () -> Constants.command("graphlib.updateblocks.starting", blockPosText(from), blockPosText(to)),
            true);

        ServerLevel world = source.getLevel();

        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        universe.getGraphWorld(world).updateNodes(BlockPos.betweenClosedStream(from, to));

        source.sendSuccess(
            () -> Constants.command("graphlib.updateblocks.success", blockPosText(from), blockPosText(to)),
            true);

        return 15;
    }

    private static int removeEmptyGraphsCommand(CommandSourceStack source, ResourceLocation universeId)
        throws CommandSyntaxException {
        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        int result = universe.getGraphWorld(source.getLevel()).removeEmptyGraphs();

        source.sendSuccess(() -> Constants.command("graphlib.removeemptygraphs.success", result), true);

        return result;
    }

    private static int rebuildChunks(CommandSourceStack source, ResourceLocation universeId, BlockPos from, BlockPos to)
        throws CommandSyntaxException {
        ServerLevel world = source.getLevel();

        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        SectionPos fromSection = SectionPos.of(from);
        SectionPos toSection = SectionPos.of(to);

        List<SectionPos> toRebuild =
            SectionPos.betweenClosedStream(fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(),
                toSection.getY(), toSection.getZ()).toList();

        universe.getGraphWorld(world).rebuildChunks(toRebuild, new RebuildChunksListener() {
            @Override
            public void onAlreadyRunning(double progress, int graphCount, int chunkCount) {
                source.sendSuccess(
                    () -> Constants.command("graphlib.rebuildchunks.alreadyrunning", progress, universeId, graphCount,
                        fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                        toSection.getZ(), chunkCount), false);
            }

            @Override
            public void onBegin(int graphCount, int chunkCount) {
                source.sendSuccess(
                    () -> Constants.command("graphlib.rebuildchunks.begin", universeId, graphCount, fromSection.getX(),
                        fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(), toSection.getZ(),
                        chunkCount), true);
            }

            @Override
            public void onProgress(double progress, int graphCount, int chunkCount) {
                source.sendSuccess(
                    () -> Constants.command("graphlib.rebuildchunks.progress", progress, universeId, graphCount,
                        fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                        toSection.getZ(), chunkCount), true);
            }

            @Override
            public void onComplete(int graphCount, int chunkCount) {
                source.sendSuccess(() -> Constants.command("graphlib.rebuildchunks.complete", universeId, graphCount,
                    fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                    toSection.getZ(), chunkCount), true);
            }
        });

        return toRebuild.size();
    }

    private static MutableComponent blockPosText(BlockPos pos) {
        return ComponentUtils.wrapInSquareBrackets(
                Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ()))
            .withStyle(
                style -> style.withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("chat.coordinates.tooltip")))
            );
    }
}
