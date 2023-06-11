package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.module.dragon.feature.LarvaFeature;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicInteger;

public class PBCommand {

    private PBCommand() {

    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("progressivebosses").requires(source -> source.hasPermission(2))
            .then(Commands.literal("difficulty")
                .then(Commands.argument("targetPlayer", EntityArgument.player())
                    .then(Commands.literal("get")
                        .then(Commands.literal("wither")
                            .executes(context -> getBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "wither"))
                        )
                        .then(Commands.literal("dragon")
                            .executes(context -> getBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon"))
                        )
                        .executes(context -> getBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), ""))
                    )
                    .then(Commands.literal("set")
                        .then(Commands.literal("wither")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.wither.feature.DifficultyFeature.maxDifficulty))
                                .executes(context -> setBossDifficulty(context.getSource(),EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                        .then(Commands.literal("dragon")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                                .executes(context -> setBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                    )
                    .then(Commands.literal("add")
                        .then(Commands.literal("wither")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.wither.feature.DifficultyFeature.maxDifficulty))
                                .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                        .then(Commands.literal("dragon")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                                .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"),"dragon", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("summon")
                .then(Commands.literal(Strings.Tags.WITHER_MINION)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.wither.feature.DifficultyFeature.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.WITHER_MINION, IntegerArgumentType.getInteger(context, "difficulty")))
                    )
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.WITHER_MINION))
                )
                .then(Commands.literal(Strings.Tags.DRAGON_MINION)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_MINION, IntegerArgumentType.getInteger(context, "difficulty")))
                    )
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.DRAGON_MINION))
                )
                .then(Commands.literal(Strings.Tags.DRAGON_LARVA)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_LARVA, IntegerArgumentType.getInteger(context, "difficulty")))
                    )
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.DRAGON_LARVA))
                )
                .then(Commands.literal(Strings.Tags.ELDER_MINION)
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.ELDER_MINION))
                )
            )
        );
    }

    private static int setBossDifficulty(CommandSourceStack source, ServerPlayer targetPlayer, String boss, int amount) {
        if (boss.equals("wither"))
            targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> difficulty.setSpawnedWithers(amount));
        if (boss.equals("dragon"))
            targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficulty -> difficulty.setKilledDragons(amount));
        source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_SET_BOSS_DIFFICULTY, targetPlayer.getName(), boss, amount), true);
        return amount;
    }

    private static int addBossDifficulty(CommandSourceStack source, ServerPlayer targetPlayer, String boss, int amount) {
        AtomicInteger difficulty = new AtomicInteger(0);
        if (boss.equals("wither")) {
            targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> {
                difficultyCap.addSpawnedWithers(amount);
                difficulty.set(difficultyCap.getSpawnedWithers());
            });

        }
        else if (boss.equals("dragon")) {
            targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> {
                difficultyCap.addKilledDragons(amount);
                difficulty.set(difficultyCap.getKilledDragons());
            });
        }
        source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_ADD_BOSS_DIFFICULTY, amount, boss, targetPlayer.getName(), difficulty.get()), true);
        return difficulty.get();
    }

    private static int getBossDifficulty(CommandSourceStack source, ServerPlayer targetPlayer, String boss) {
        AtomicInteger witherDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> witherDifficulty.set(difficultyCap.getSpawnedWithers()));
        AtomicInteger dragonDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> dragonDifficulty.set(difficultyCap.getKilledDragons()));

        switch (boss) {
            case "wither" -> {
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), witherDifficulty), true);
                return witherDifficulty.get();
            }
            case "dragon" -> {
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), dragonDifficulty), true);
                return dragonDifficulty.get();
            }
            default -> {
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), witherDifficulty), true);
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), dragonDifficulty), true);
                return 1;
            }
        }
    }

    private static int summon(CommandSourceStack source, String entity, int difficulty) {
        switch (entity) {
            case Strings.Tags.WITHER_MINION -> {
                insane96mcp.progressivebosses.module.wither.feature.MinionFeature.summonMinion(source.getLevel(), source.getPosition(), DifficultyHelper.getScalingDifficulty(difficulty, insane96mcp.progressivebosses.module.wither.feature.DifficultyFeature.maxDifficulty), false);
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), difficulty), true);
                return 1;
            }
            case Strings.Tags.DRAGON_MINION -> {
                insane96mcp.progressivebosses.module.dragon.feature.MinionFeature.summonMinion(source.getLevel(), source.getPosition(), DifficultyHelper.getScalingDifficulty(difficulty, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty));
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), difficulty), true);
                return 1;
            }
            case Strings.Tags.DRAGON_LARVA -> {
                LarvaFeature.summonLarva(source.getLevel(), source.getPosition(), DifficultyHelper.getScalingDifficulty(difficulty, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty));
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), difficulty), true);
                return 1;
            }
            case Strings.Tags.ELDER_MINION -> {
                insane96mcp.progressivebosses.module.elderguardian.feature.MinionFeature.summonMinion(source.getLevel(), source.getPosition());
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), difficulty), true);
                return 1;
            }
            default -> {
                source.sendFailure(Component.translatable(Strings.Translatable.SUMMON_ENTITY_INVALID, entity));
                return 0;
            }
        }
    }

    private static int summon(CommandSourceStack source, ServerPlayer targetPlayer, String entity) {
        AtomicInteger witherDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> witherDifficulty.set(difficultyCap.getSpawnedWithers()));
        AtomicInteger dragonDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(Difficulty.INSTANCE).ifPresent(difficultyCap -> dragonDifficulty.set(difficultyCap.getKilledDragons()));
        if (entity.contains("wither"))
            return summon(source, entity, witherDifficulty.get());
        else if (entity.contains("dragon"))
            return summon(source, entity, dragonDifficulty.get());
        else if (entity.contains("elder"))
            return summon(source, entity, 0);
        return 0;
    }
}
