package com.kneelawk.graphlib.impl.command;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
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
import net.minecraft.util.math.ChunkSectionPos;

import com.kneelawk.graphlib.impl.Constants;
import com.kneelawk.graphlib.impl.GraphLibImpl;
import com.kneelawk.graphlib.impl.graph.GraphUniverseImpl;
import com.kneelawk.graphlib.impl.graph.RebuildChunksListener;
import com.kneelawk.graphlib.impl.platform.GraphLibPlatform;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class GraphLibCommand {
    public static final DynamicCommandExceptionType UNKNOWN_UNIVERSE =
        new DynamicCommandExceptionType(arg -> new LiteralMessage("Unknown universe: " + arg));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext) {
        RequiredArgumentBuilder<ServerCommandSource, Identifier> universeBuilder =
            argument("universe", IdentifierArgumentType.identifier())
                .suggests((context, builder) -> {
                    String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
                    CommandSource.forEachMatching(GraphLibImpl.UNIVERSE.keySet(), remaining,
                        Function.identity(), id -> builder.suggest(id.toString()));
                    return builder.buildFuture();
                })
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
                .then(literal("rebuildchunks")
                    .then(argument("from", BlockPosArgumentType.blockPos())
                        .then(argument("to", BlockPosArgumentType.blockPos())
                            .executes(context -> rebuildChunks(context.getSource(),
                                IdentifierArgumentType.getIdentifier(context, "universe"),
                                BlockPosArgumentType.getBlockPos(context, "from"),
                                BlockPosArgumentType.getBlockPos(context, "to")))
                        )
                    )
                );

        GraphLibPlatform.INSTANCE.fireAddUniverseSubcommands(universeBuilder);

        dispatcher.register(literal("graphlib")
            .requires(source -> source.hasPermission(2))
            .then(literal("list").executes(context -> listUniverses(context.getSource())))
            .then(universeBuilder)
        );
    }

    private static int listUniverses(ServerCommandSource source) {
        MutableText msg = Text.literal("Universes:");

        for (Identifier key : GraphLibImpl.UNIVERSE.keySet()) {
            msg.append("\n");
            msg.append(Text.literal(key.toString()).styled(style -> style.withColor(Formatting.AQUA)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, key.toString()))));
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

    private static int rebuildChunks(ServerCommandSource source, Identifier universeId, BlockPos from, BlockPos to)
        throws CommandSyntaxException {
        ServerWorld world = source.getWorld();

        GraphUniverseImpl universe = GraphLibImpl.UNIVERSE.get(universeId);
        if (universe == null) throw UNKNOWN_UNIVERSE.create(universeId);

        ChunkSectionPos fromSection = ChunkSectionPos.from(from);
        ChunkSectionPos toSection = ChunkSectionPos.from(to);

        List<ChunkSectionPos> toRebuild =
            ChunkSectionPos.stream(fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(),
                toSection.getY(), toSection.getZ()).toList();

        universe.getServerGraphWorld(world).rebuildChunks(toRebuild, new RebuildChunksListener() {
            @Override
            public void onAlreadyRunning(double progress, int graphCount, int chunkCount) {
                source.sendFeedback(
                    () -> Constants.command("graphlib.rebuildchunks.alreadyrunning", progress, universeId, graphCount,
                        fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                        toSection.getZ(), chunkCount), false);
            }

            @Override
            public void onBegin(int graphCount, int chunkCount) {
                source.sendFeedback(
                    () -> Constants.command("graphlib.rebuildchunks.begin", universeId, graphCount, fromSection.getX(),
                        fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(), toSection.getZ(),
                        chunkCount), true);
            }

            @Override
            public void onProgress(double progress, int graphCount, int chunkCount) {
                source.sendFeedback(
                    () -> Constants.command("graphlib.rebuildchunks.progress", progress, universeId, graphCount,
                        fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                        toSection.getZ(), chunkCount), true);
            }

            @Override
            public void onComplete(int graphCount, int chunkCount) {
                source.sendFeedback(() -> Constants.command("graphlib.rebuildchunks.complete", universeId, graphCount,
                    fromSection.getX(), fromSection.getY(), fromSection.getZ(), toSection.getX(), toSection.getY(),
                    toSection.getZ(), chunkCount), true);
            }
        });

        return toRebuild.size();
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
