package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.data.DragonStatsReloadListener;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

@Label(name = "Ender Dragon Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DragonFeature extends Feature {
    public static final String LEVEL = ProgressiveBosses.RESOURCE_PREFIX + "level";

    public static final String EGGS_TO_DROP = ProgressiveBosses.RESOURCE_PREFIX + "eggs_to_drop";

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

        dropEgg(dragon);
    }

    private static void dropEgg(EnderDragon dragon) {
        if (!dragonEggPerPlayer
                || dragon.dragonDeathTime != 100)
            return;

        CompoundTag tags = dragon.getPersistentData();

        int eggsToDrop = tags.getInt(EGGS_TO_DROP);

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
