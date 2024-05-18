package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.Difficulty;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public class PBCommand {

    private PBCommand() {

    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pb").requires(source -> source.hasPermission(2))
                .then(Commands.literal("summon")
                        .then(Commands.literal("wither")
                                .then(Commands.argument("lvl", IntegerArgumentType.integer())
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> summon(context.getSource(), PBEntities.WITHER.get(), IntegerArgumentType.getInteger(context, "lvl"), Vec3Argument.getVec3(context, "pos"))))
                                        .executes(context -> summon(context.getSource(), PBEntities.WITHER.get(), IntegerArgumentType.getInteger(context, "lvl")))))
                        .then(Commands.literal("wither_minion")
                                .then(Commands.argument("lvl", IntegerArgumentType.integer())
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> summon(context.getSource(), PBEntities.WITHER_MINION.get(), IntegerArgumentType.getInteger(context, "lvl"), Vec3Argument.getVec3(context, "pos"))))
                                        .executes(context -> summon(context.getSource(), PBEntities.WITHER_MINION.get(), IntegerArgumentType.getInteger(context, "lvl"))))))
                .then(Commands.literal("set")
                        .then(Commands.literal("wither")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.literal("charging")
                                                .executes(context -> witherCharge(context.getSource(), EntityArgument.getEntities(context, "targets"))))
                                        .then(Commands.literal("barraging")
                                                .executes(context -> witherBarrage(context.getSource(), EntityArgument.getEntities(context, "targets"))))))));
        /*dispatcher.register(Commands.literal("progressivebosses").requires(source -> source.hasPermission(2))
            .then(Commands.literal("lvl")
                .then(Commands.argument("targetPlayer", EntityArgument.player())
                    .then(Commands.literal("get")
                        .then(Commands.literal("dragon")
                            .executes(context -> getBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon"))
                        )
                        .executes(context -> getBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), ""))
                    )
                    .then(Commands.literal("set")
                        .then(Commands.literal("dragon")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                                .executes(context -> setBossDifficulty(context.getSource(), EntityArgument.getPlayer(context, "targetPlayer"), "dragon", IntegerArgumentType.getInteger(context, "amount")))
                            )
                        )
                    )
                    .then(Commands.literal("add")
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
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0))
                        .executes(context -> summon(context.getSource(), Strings.Tags.WITHER_MINION, IntegerArgumentType.getInteger(context, "lvl"), context.getArgument("isPowered", Boolean.class)))
                    )
                )
                .then(Commands.literal(Strings.Tags.DRAGON_MINION)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_MINION, IntegerArgumentType.getInteger(context, "lvl"), false))
                    )
                )
                .then(Commands.literal(Strings.Tags.DRAGON_LARVA)
                    .then(Commands.argument("difficulty", IntegerArgumentType.integer(0, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty))
                        .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_LARVA, IntegerArgumentType.getInteger(context, "lvl"), false))
                    )
                )
                .then(Commands.literal(Strings.Tags.ELDER_MINION)
                    .executes(context -> summon(context.getSource(), Strings.Tags.DRAGON_LARVA, 0, false))
                )
            )
        );*/
    }

    private static int summon(CommandSourceStack source, EntityType<? extends LivingEntity> entityType, int lvl) {
        return summon(source, entityType, lvl, source.getPosition());
    }

    private static int summon(CommandSourceStack source, EntityType<? extends LivingEntity> entityType, int lvl, Vec3 pos) {
        LivingEntity entity = entityType.create(source.getLevel());
        if (entity == null) {
            source.sendFailure(Component.translatable(ProgressiveBosses.RESOURCE_PREFIX + "command.failed_to_summon", entityType));
            return 0;
        }
        LvlHelper.setLvl(entity, lvl);
        entity.setPos(pos);
        if (source.getLevel().addFreshEntity(entity)) {
            source.sendSuccess(() -> Component.translatable(ProgressiveBosses.RESOURCE_PREFIX + "command.summoned_entity", entity.getDisplayName(), lvl), true);
            return 1;
        }
        else {
            source.sendFailure(Component.translatable(ProgressiveBosses.RESOURCE_PREFIX + "command.failed_to_summon", entityType));
            return 0;
        }
    }

    private static int witherCharge(CommandSourceStack source, Collection<? extends Entity> pTargets) {
        int setToCharge = 0;
        for (Entity target : pTargets) {
            if (target instanceof PBWither pbWither) {
                if (pbWither.initCharging())
                    setToCharge++;
            }
        }
        return setToCharge;
    }

    private static int witherBarrage(CommandSourceStack source, Collection<? extends Entity> pTargets) {
        int setToBarrage = 0;
        for (Entity target : pTargets) {
            if (target instanceof PBWither pbWither) {
                if (pbWither.initBarrageChargeUp())
                    setToBarrage++;
            }
        }
        return setToBarrage;
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

    /*private static int summon(CommandSourceStack source, String entity, int lvl, boolean isPowered) {
        switch (entity) {
            case Strings.Tags.WITHER_MINION -> {
                WitherMinion.create(source.getLevel(), source.getPosition(), lvl, isPowered);
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), lvl), true);
                return 1;
            }
            case Strings.Tags.DRAGON_MINION -> {
                insane96mcp.progressivebosses.module.dragon.feature.MinionFeature.summonMinion(source.getLevel(), source.getPosition(), DifficultyHelper.getScalingDifficulty(lvl, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty));
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), lvl), true);
                return 1;
            }
            case Strings.Tags.DRAGON_LARVA -> {
                LarvaFeature.summonLarva(source.getLevel(), source.getPosition(), DifficultyHelper.getScalingDifficulty(lvl, insane96mcp.progressivebosses.module.dragon.feature.DifficultyFeature.maxDifficulty));
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), lvl), true);
                return 1;
            }
            case Strings.Tags.ELDER_MINION -> {
                insane96mcp.progressivebosses.module.elderguardian.feature.MinionFeature.summonMinion(source.getLevel(), source.getPosition());
                source.sendSuccess(() -> Component.translatable(Strings.Translatable.SUMMONED_ENTITY, Component.translatable(entity), lvl), true);
                return 1;
            }
            default -> {
                source.sendFailure(Component.translatable(Strings.Translatable.SUMMON_ENTITY_INVALID, entity));
                return 0;
            }
        }
    }*/
}
