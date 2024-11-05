package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.data.DragonStatsReloadListener;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

@Label(name = "Ender Dragon Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DragonFeature extends Feature {
    public static final String LEVEL = ProgressiveBosses.RESOURCE_PREFIX + "level";

    public static final String HAS_KILLED_DRAGON = ProgressiveBosses.RESOURCE_PREFIX + "has_killed_dragon";

    @Config
    @Label(name = "Dragon Egg per Player", description = "If true whenever a player, that has never killed the dragon, kills the dragon a Dragon Egg will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
    public static Boolean dragonEggPerPlayer = true;

    public DragonFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide
                || !(event.getEntity() instanceof EnderDragon dragon)
                || dragon.getPersistentData().contains(ProgressiveBosses.RESOURCE_PREFIX + "processed"))
            return;

        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty()) {
            LogHelper.info("Failed to get Dragon Stats for level %s", dragon.getPersistentData().getByte(LEVEL));
            return;
        }
        DragonStats.apply(dragon, stats.get());
        dragon.getPersistentData().putBoolean(ProgressiveBosses.RESOURCE_PREFIX + "processed", true);
    }

    @SubscribeEvent
    public void onExpDrop(LivingExperienceDropEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon)
                || event.getDroppedExperience() == 0)
            return;

        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty())
            return;
        event.setDroppedExperience(stats.get().xpDropped);
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon))
            return;

        tryHeal(dragon);
        dropEgg(dragon);
    }

    private static void tryHeal(EnderDragon dragon) {
        if (!dragon.isAlive()
                || dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING
                || dragon.tickCount % 10 != 5)
            return;
        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        if (stats.get().health.regeneration == 0f)
            return;

        float heal = stats.get().health.regeneration;
        heal /= 2f;

        if (dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= stats.get().health.regenWhenHitDuration)
            heal *= stats.get().health.regenWhenHitRatio;

        dragon.heal(heal);
    }

    private static void dropEgg(EnderDragon dragon) {
        if (!dragonEggPerPlayer
                || dragon.dragonDeathTime != 100)
            return;

        int radius = 256;
        BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
        BlockPos pos2 = new BlockPos(radius, radius, radius);
        AABB bb = new AABB(pos1, pos2);

        List<ServerPlayer> players = dragon.level().getEntitiesOfClass(ServerPlayer.class, bb);

        int eggsToDrop = 0;
        for (ServerPlayer player : players) {
            if (MCUtils.getOrCreatePersistedData(player).contains(HAS_KILLED_DRAGON))
                continue;
            eggsToDrop++;
            MCUtils.getOrCreatePersistedData(player).putBoolean(HAS_KILLED_DRAGON, true);
        }

        if (dragon.getDragonFight() != null && !dragon.getDragonFight().hasPreviouslyKilledDragon()) {
            eggsToDrop--;
        }

        for (int i = 0; i < eggsToDrop; i++) {
            dragon.level().setBlockAndUpdate(new BlockPos(0, 255 - i, 0), Blocks.DRAGON_EGG.defaultBlockState());
        }
    }

    public static Optional<DragonStats> getDragonStats(EnderDragon dragon) {
        int lvl = dragon.getPersistentData().getByte(LEVEL);
        return Optional.ofNullable(DragonStatsReloadListener.STATS_MAP.get(lvl));
    }
}
