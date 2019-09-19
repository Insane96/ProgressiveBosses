package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.setup.ModConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.TranslationTextComponent;

public class Command {

    private Command() {

    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("progressivebosses").requires(source -> source.hasPermissionLevel(2))
                .then(Commands.argument("targetPlayer", EntityArgument.player())
                        .then(Commands.literal("get").executes(context -> getBossData(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"))))
                        .then(Commands.literal("set")
                                .then(Commands.literal("wither")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.Wither.General.maxDifficulty.get()))
                                                .executes(context -> setBossData(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "targetPlayer"),
                                                        "wither",
                                                        IntegerArgumentType.getInteger(context, "amount")
                                                ))
                                        )
                                )
                                .then(Commands.literal("dragon")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, ModConfig.Dragon.General.maxDifficulty.get()))
                                                .executes(context -> setBossData(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "targetPlayer"),
                                                        "dragon",
                                                        IntegerArgumentType.getInteger(context, "amount")
                                                ))
                                        )
                                )
                        )
                ));

    }

    private static int setBossData(CommandSource source, ServerPlayerEntity targetPlayer, String boss, int amount) {
		CompoundNBT targetNBT = targetPlayer.getPersistentData();
        if (boss == "wither")
            targetNBT.putInt("progressivebosses:spawned_withers", amount);
        if (boss == "dragon")
            targetNBT.putInt("progressivebosses:killed_dragons", amount);
        source.sendFeedback(new TranslationTextComponent("command.set_player_boss_data", targetPlayer.getName(), boss, amount), true);
        return amount;
    }

    private static int getBossData(CommandSource source, ServerPlayerEntity targetPlayer) {
		CompoundNBT targetNBT = targetPlayer.getPersistentData();
        source.sendFeedback(new TranslationTextComponent("command.get_player_boss_data", targetPlayer.getName(), targetNBT.getInt("progressivebosses:spawned_withers"), targetNBT.getInt("progressivebosses:killed_dragons")), true);

        return 1;
    }


}
