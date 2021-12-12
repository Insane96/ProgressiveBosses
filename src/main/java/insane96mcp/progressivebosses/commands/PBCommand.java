package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.capability.DifficultyCapability;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.concurrent.atomic.AtomicInteger;

public class PBCommand {

    private PBCommand() {

    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
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
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, Modules.wither.difficulty.maxDifficulty))
                                .executes(context -> setBossDifficulty(context.getSource(),EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                        .then(Commands.literal("dragon")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, Modules.dragon.difficulty.maxDifficulty))
                                .executes(context -> setBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                    )
                    .then(Commands.literal("add")
                        .then(Commands.literal("wither")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, Modules.wither.difficulty.maxDifficulty))
                                .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                        .then(Commands.literal("dragon")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, Modules.dragon.difficulty.maxDifficulty))
                                .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"),"dragon", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                    )
                )
            )
            .then(Commands.literal("legacy_difficulty")
                .then(Commands.argument("targetPlayer", EntityArgument.player())
                    .then(Commands.literal("get")
                        .then(Commands.literal("wither")
                            .executes(context -> getBossDifficultyLegacy(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "wither"))
                        )
                        .then(Commands.literal("dragon")
                            .executes(context -> getBossDifficultyLegacy(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon"))
                        )
                        .executes(context -> getBossDifficultyLegacy(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), ""))
                    )
                )
            )
            .then(Commands.literal("summon")
                .then(Commands.literal(Strings.Tags.WITHER_MINION)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, Modules.wither.difficulty.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.WITHER_MINION, IntegerArgumentType.getInteger(context, "difficulty")))
                    )
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.WITHER_MINION))
                )
                .then(Commands.literal(Strings.Tags.DRAGON_MINION)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, Modules.dragon.difficulty.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_MINION, IntegerArgumentType.getInteger(context, "difficulty")))
                    )
                    .executes(context -> summon(context.getSource(), context.getSource().getPlayerOrException(), Strings.Tags.DRAGON_MINION))
                )
                .then(Commands.literal(Strings.Tags.DRAGON_LARVA)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, Modules.dragon.difficulty.maxDifficulty))
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

    private static int setBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss, int amount) {
        if (boss.equals("wither"))
            targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficulty -> difficulty.setSpawnedWithers(amount));
        if (boss.equals("dragon"))
            targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficulty -> difficulty.setKilledDragons(amount));
        source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_SET_BOSS_DIFFICULTY, targetPlayer.getName(), boss, amount), true);
        return amount;
    }

    private static int addBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss, int amount) {
        AtomicInteger difficulty = new AtomicInteger(0);
        if (boss.equals("wither")) {
            targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> {
                difficultyCap.addSpawnedWithers(amount);
                difficulty.set(difficultyCap.getSpawnedWithers());
            });

        }
        else if (boss.equals("dragon")) {
            targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> {
                difficultyCap.addKilledDragons(amount);
                difficulty.set(difficultyCap.getKilledDragons());
            });
        }
        source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_ADD_BOSS_DIFFICULTY, amount, boss, targetPlayer.getName(), difficulty.get()), true);
        return difficulty.get();
    }

    private static int getBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss) {
        AtomicInteger witherDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> witherDifficulty.set(difficultyCap.getSpawnedWithers()));
        AtomicInteger dragonDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> dragonDifficulty.set(difficultyCap.getKilledDragons()));
        if (boss.equals("wither")) {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), witherDifficulty), true);
            return witherDifficulty.get();
        }
        else if (boss.equals("dragon")) {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), dragonDifficulty), true);
            return dragonDifficulty.get();
        }
        else {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), witherDifficulty), true);
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), dragonDifficulty), true);
            return 1;
        }
    }

    private static int getBossDifficultyLegacy(CommandSource source, ServerPlayerEntity targetPlayer, String boss) {
        source.sendSuccess(new StringTextComponent("This difficulty is no longer used by the mod. The command is here to let you see your old difficulty and trasnfer it to the new system with /progressivebosses difficulty <player> set <wither/dragon> <amount>."), true);
        CompoundNBT targetNBT = targetPlayer.getPersistentData();
        if (boss.equals("wither")) {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.Tags.SPAWNED_WITHERS)), true);
            return targetNBT.getInt(Strings.Tags.SPAWNED_WITHERS);
        }
        else if (boss.equals("dragon")) {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.Tags.KILLED_DRAGONS)), true);
            return targetNBT.getInt(Strings.Tags.KILLED_DRAGONS);
        }
        else {
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.Tags.SPAWNED_WITHERS)), true);
            source.sendSuccess(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.Tags.KILLED_DRAGONS)), true);
            return 1;
        }
    }

    private static int summon(CommandSource source, String entity, int difficulty) {
        switch (entity) {
            case Strings.Tags.WITHER_MINION:
                Modules.wither.minion.summonMinion(source.getLevel(), source.getPosition(), difficulty, false);
                source.sendSuccess(new TranslationTextComponent(Strings.Translatable.SUMMONED_ENTITY, new TranslationTextComponent(entity), difficulty), true);
                return 1;

            case Strings.Tags.DRAGON_MINION:
                Modules.dragon.minion.summonMinion(source.getLevel(), source.getPosition(), difficulty);
                source.sendSuccess(new TranslationTextComponent(Strings.Translatable.SUMMONED_ENTITY, new TranslationTextComponent(entity), difficulty), true);
                return 1;

            case Strings.Tags.DRAGON_LARVA:
                Modules.dragon.larva.summonLarva(source.getLevel(), source.getPosition(), difficulty);
                source.sendSuccess(new TranslationTextComponent(Strings.Translatable.SUMMONED_ENTITY, new TranslationTextComponent(entity), difficulty), true);
                return 1;

            case Strings.Tags.ELDER_MINION:
                Modules.elderGuardian.minion.summonMinion(source.getLevel(), source.getPosition());
                source.sendSuccess(new TranslationTextComponent(Strings.Translatable.SUMMONED_ENTITY, new TranslationTextComponent(entity), difficulty), true);
                return 1;

            default:
                source.sendSuccess(new TranslationTextComponent(Strings.Translatable.SUMMON_ENTITY_INVALID, entity), true);
                return 0;
        }
    }

    private static int summon(CommandSource source, ServerPlayerEntity targetPlayer, String entity) {
        AtomicInteger witherDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> witherDifficulty.set(difficultyCap.getSpawnedWithers()));
        AtomicInteger dragonDifficulty = new AtomicInteger(0);
        targetPlayer.getCapability(DifficultyCapability.DIFFICULTY).ifPresent(difficultyCap -> dragonDifficulty.set(difficultyCap.getKilledDragons()));
        if (entity.contains("wither"))
            return summon(source, entity, witherDifficulty.get());
        else if (entity.contains("dragon"))
            return summon(source, entity, dragonDifficulty.get());
        else if (entity.contains("elder"))
            return summon(source, entity, 0);
        return 0;
    }
}
