package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.ai.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Collection;

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
                if (pbWither.initCharging(WitherChargeAttackGoal.ChargeType.ON_HIT))
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
}
