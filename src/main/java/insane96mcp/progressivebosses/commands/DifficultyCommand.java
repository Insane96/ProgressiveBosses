package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TranslationTextComponent;

public class DifficultyCommand {

    private DifficultyCommand() {

    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("progressivebosses").requires(source -> source.hasPermissionLevel(2))
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
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.COMMON.wither.general.maxDifficulty.get()))
                            .executes(context -> setBossDifficulty(context.getSource(),EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount"))
                            )
                        )
                    )
                    .then(Commands.literal("dragon")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.COMMON.dragon.general.maxDifficulty.get()))
                            .executes(context -> setBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon", IntegerArgumentType.getInteger(context, "amount")
                            ))
                        )
                    )
                )
                .then(Commands.literal("add")
                    .then(Commands.literal("wither")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.COMMON.wither.general.maxDifficulty.get()))
                            .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "wither", IntegerArgumentType.getInteger(context, "amount")
                            ))
                        )
                    )
                    .then(Commands.literal("dragon")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.COMMON.dragon.general.maxDifficulty.get()))
                            .executes(context -> addBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"),"dragon", IntegerArgumentType.getInteger(context, "amount"))
                            )
                        )
                    )
                )
            )
        );
    }

    private static int setBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss, int amount) {
		CompoundNBT targetNBT = targetPlayer.getPersistentData();
        if (boss.equals("wither"))
            targetNBT.putInt(Strings.NBTTags.SPAWNED_WITHERS, amount);
        if (boss.equals("dragon"))
            targetNBT.putInt(Strings.NBTTags.KILLED_DRAGONS, amount);
        source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_SET_BOSS_DIFFICULTY, targetPlayer.getName(), boss, amount), true);
        return amount;
    }

    private static int addBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss, int amount) {
        CompoundNBT targetNBT = targetPlayer.getPersistentData();
        int currDifficulty;
        int difficulty = 0;
        if (boss.equals("wither")) {
            currDifficulty = targetNBT.getInt(Strings.NBTTags.SPAWNED_WITHERS);
            difficulty = currDifficulty + amount;
            difficulty = MathHelper.clamp(difficulty, 0, ModConfig.COMMON.wither.general.maxDifficulty.get());
            targetNBT.putInt(Strings.NBTTags.SPAWNED_WITHERS, difficulty);
        }
        else if (boss.equals("dragon")) {
            currDifficulty = targetNBT.getInt(Strings.NBTTags.KILLED_DRAGONS);
            difficulty = currDifficulty + amount;
            difficulty = MathHelper.clamp(difficulty, 0, ModConfig.COMMON.dragon.general.maxDifficulty.get());
            targetNBT.putInt(Strings.NBTTags.KILLED_DRAGONS, difficulty);
        }
        source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_ADD_BOSS_DIFFICULTY, amount, boss, targetPlayer.getName(), difficulty), true);
        return difficulty;
    }

    private static int getBossDifficulty(CommandSource source, ServerPlayerEntity targetPlayer, String boss) {
		CompoundNBT targetNBT = targetPlayer.getPersistentData();
        if (boss.equals("wither")) {
            source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.NBTTags.SPAWNED_WITHERS)), true);
            return targetNBT.getInt(Strings.NBTTags.SPAWNED_WITHERS);
        }
        else if (boss.equals("dragon")) {
            source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.NBTTags.KILLED_DRAGONS)), true);
            return targetNBT.getInt(Strings.NBTTags.KILLED_DRAGONS);
        }
        else {
            source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_WITHER_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.NBTTags.SPAWNED_WITHERS)), true);
            source.sendFeedback(new TranslationTextComponent(Strings.Translatable.PLAYER_GET_DRAGON_DIFFICULTY, targetPlayer.getName(), targetNBT.getInt(Strings.NBTTags.KILLED_DRAGONS)), true);
            return 1;
        }
    }
}
