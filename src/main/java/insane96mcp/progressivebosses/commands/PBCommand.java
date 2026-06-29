package insane96mcp.progressivebosses.commands;

import com.mojang.brigadier.CommandDispatcher;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PBCommand {

    private PBCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("pb").requires(source -> source.hasPermission(2))
                .then(Commands.literal("summon")
                        // Wither commands will be re-enabled when PBEntities and PBWither are ported (Phase 2+)
                        /*
                        .then(Commands.literal("wither")
                                .then(Commands.argument("lvl", IntegerArgumentType.integer())
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> summon(context.getSource(), PBEntities.WITHER.get(), IntegerArgumentType.getInteger(context, "lvl"), Vec3Argument.getVec3(context, "pos"))))
                                        .executes(context -> summon(context.getSource(), PBEntities.WITHER.get(), IntegerArgumentType.getInteger(context, "lvl")))))
                        .then(Commands.literal("wither_minion")
                                .then(Commands.argument("lvl", IntegerArgumentType.integer())
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(context -> summon(context.getSource(), PBEntities.WITHER_MINION.get(), IntegerArgumentType.getInteger(context, "lvl"), Vec3Argument.getVec3(context, "pos"))))
                                        .executes(context -> summon(context.getSource(), PBEntities.WITHER_MINION.get(), IntegerArgumentType.getInteger(context, "lvl")))))
                        */
                )
                // Wither set commands will be re-enabled when PBWither and WitherChargeAttackGoal are ported (Phase 2+)
                /*
                .then(Commands.literal("set")
                        .then(Commands.literal("wither")
                                .then(Commands.argument("targets", EntityArgument.entities())
                                        .then(Commands.literal("charging")
                                                .executes(context -> witherCharge(context.getSource(), EntityArgument.getEntities(context, "targets"))))
                                        .then(Commands.literal("barraging")
                                                .executes(context -> witherBarrage(context.getSource(), EntityArgument.getEntities(context, "targets")))))))
                */
        );
    }

    private static int summon(CommandSourceStack source, EntityType<? extends LivingEntity> entityType, int lvl) {
        return summon(source, entityType, lvl, source.getPosition());
    }

    private static int summon(CommandSourceStack source, EntityType<? extends LivingEntity> entityType, int lvl, Vec3 pos) {
        LivingEntity entity = entityType.create(source.getLevel());
        if (entity == null) {
            source.sendFailure(Component.translatable(ProgressiveBosses.lang("command.failed_to_summon"), entityType));
            return 0;
        }
        LvlHelper.setLvl(entity, lvl);
        entity.setPos(pos);
        if (source.getLevel().addFreshEntity(entity)) {
            source.sendSuccess(() -> Component.translatable(ProgressiveBosses.lang("command.summoned_entity"), entity.getDisplayName(), lvl), true);
            return 1;
        }
        source.sendFailure(Component.translatable(ProgressiveBosses.lang("command.failed_to_summon"), entityType));
        return 0;
    }
}
