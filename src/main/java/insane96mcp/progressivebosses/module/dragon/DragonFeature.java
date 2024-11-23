package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseChangeEvent;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystal;
import insane96mcp.progressivebosses.module.dragon.data.*;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;

@Label(name = "Ender Dragon Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DragonFeature extends Feature {
    public static final String LEVEL = ProgressiveBosses.RESOURCE_PREFIX + "level";

    public static final String HAS_KILLED_DRAGON = ProgressiveBosses.RESOURCE_PREFIX + "has_killed_dragon";

    @Config
    @Label(name = "Explosion Immune Crystals", description = "Crystals can no longer be destroyed by other explosions.")
    public static Boolean explosionImmuneCrystals = true;

    @Config
    @Label(name = "Enable Fixes", description = """
            Enable some fixes for the Ender Dragon:
             - Dragon Head and Neck have been repositioned correctly
             - Dragon will now play the growl sound only 4 times/second when respawning and when at the center breathing instead of 20/second (so your ears shouldn't blow up anymore)
             - When the crystals that respawn the dragon in the center are destroyed, the fire is extinguished
             - Ender Dragon can now rise and fall faster (somewhere around 1.14 the multiplier for the y speed was reduced to 0.01 instead of 0.1 https://bugs.mojang.com/browse/MC-272431)
             - Dragon is set exactly at the center of the well""")
    public static Boolean enableFixes = true;

    @Config
    @Label(name = "Dragon Egg per Player", description = "If true, whenever a player that has never killed the dragon, kills the dragon, a Dragon Egg will drop. E.g. If 2 players kill the Dragon for the first time, she will drop 2 Dragon Eggs")
    public static Boolean dragonEggPerPlayer = true;

    public static byte dragonLvl = 0;

    public DragonFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
        super(module, enabledByDefault, canBeDisabled);
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!this.isEnabled()
                || event.getLevel().isClientSide)
            return;
        onDragonJoinLevel(event);
        DragonMinion.onShulkerSpawn(event);
        DragonAttack.setAcidBallSpeedMultiplier(event.getEntity());
    }

    public void onDragonJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)
                || dragon.getPersistentData().contains(ProgressiveBosses.RESOURCE_PREFIX + "processed"))
            return;

        if (!dragon.getPersistentData().contains(LEVEL)) {
            dragon.getPersistentData().putByte(LEVEL, dragonLvl);
        }

        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty()) {
            LogHelper.warn("Failed to get Dragon Stats for level %s", dragon.getPersistentData().getByte(LEVEL));
            return;
        }
        DragonStats.apply(dragon, stats.get());
        dragon.setCustomName(Component.translatable(Util.makeDescriptionId("entity", ForgeRegistries.ENTITY_TYPES.getKey(dragon.getType())) + "." + dragon.getPersistentData().getByte(LEVEL)));
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
        //This will 100% break if any other mod changes experience dropped
        if (event.getDroppedExperience() == Mth.floor(12000 * 0.08F)
                || event.getDroppedExperience() == Mth.floor(500 * 0.08F))
            event.setDroppedExperience(Mth.floor(stats.get().xpDropped * 0.08f));
        else if (event.getDroppedExperience() == Mth.floor(12000 * 0.2F)
                || event.getDroppedExperience() == Mth.floor(500 * 0.2F))
            event.setDroppedExperience(Mth.floor(stats.get().xpDropped * 0.2f));
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingTickEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon))
            return;

        DragonHealth.tryHeal(dragon);
        DragonLarva.tickLarva(dragon);
        DragonMinion.tickMinion(dragon);
        tryDropEgg(dragon);
    }

    private static void tryDropEgg(EnderDragon dragon) {
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

    @SubscribeEvent
    public void onSetPhase(DragonPhaseChangeEvent event) {
        if (!this.isEnabled())
            return;

        /*BlockPos centerPodium = event.getDragon().level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(64d);
        ServerPlayer player = (ServerPlayer) DragonAttack.getRandomPlayer(event.getDragon().level(), bb);

        if (player == null)
            return;

        event.setNewPhase(EnderDragonPhase.CHARGING_PLAYER);
        event.getDragon().getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());*/
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event) {
        if (!this.isEnabled())
            return;

        DragonMinion.onMinionHurt(event);
        onDragonHurt(event);
        DragonAttack.onHurtLiving(event);
    }

    public void onDragonHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon))
            return;

        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        DragonVulnerabilities.damageMultipliers(event, dragon, stats.get());

        DragonCrystal.tryRespawningCrystalPhase(dragon, stats.get());
    }

    public static Optional<DragonStats> getDragonStats(EnderDragon dragon) {
        int lvl = dragon.getPersistentData().getByte(LEVEL);
        return getDragonStats(lvl);
    }

    public static Optional<DragonStats> getDragonStats(int lvl) {
        return Optional.ofNullable(DragonStatsReloadListener.STATS_MAP.get(lvl));
    }

    public static byte getDragonLvl(List<EndCrystal> respawningCrystals) {
        for (EndCrystal crystal : respawningCrystals) {
            if (crystal instanceof CorruptedEndCrystal) {
                return 1;
            }
        }
        return 0;
    }

    public static float neckOffsetXZ() {
        return 3.8f;
    }

    public static float headOffsetXZ() {
        return 6f;
    }

    public static float headOffsetSittingY() {
        return 0f;
    }

    public static float headOffsetY(float original) {
        return original + 1.5f;
    }
}
