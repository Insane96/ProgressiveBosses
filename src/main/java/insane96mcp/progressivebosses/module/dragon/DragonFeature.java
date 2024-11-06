package insane96mcp.progressivebosses.module.dragon;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.LoadFeature;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.data.DragonStatsReloadListener;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Stream;

@Label(name = "Ender Dragon Feature")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon", canBeDisabled = false)
public class DragonFeature extends Feature {
    public static final String LEVEL = ProgressiveBosses.RESOURCE_PREFIX + "level";

    public static final String HAS_KILLED_DRAGON = ProgressiveBosses.RESOURCE_PREFIX + "has_killed_dragon";
    /**
     * How many times has the dragon respawned crystals
     */
    public static final String CRYSTAL_RESPAWN = ProgressiveBosses.RESOURCE_PREFIX + "crystal_respawn";

    public static final String DRAGON_LARVA_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva_cooldown";

    @Config
    @Label(name = "Explosion Immune", description = "Crystals can no longer be destroyed by other explosions.")
    public static Boolean explosionImmune = true;

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

        moreCrystals(dragon, stats.get());
        setupLarvaCooldown(dragon, stats.get());
    }

    //region Crystal inside pillars
    private static void moreCrystals(EnderDragon dragon, DragonStats stats) {
        List<EndCrystal> crystals = new ArrayList<>();

        //Order from smaller towers to bigger ones
        List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level()));
        spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));

        for(SpikeFeature.EndSpike spike : spikes) {
            crystals.addAll(dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox(), EndCrystal::showsBottom));
        }

        int crystalSpawned = 0;

        for (EndCrystal crystal : crystals) {
            generateCrystalInTower(dragon.level(), crystal.getBlockX(), crystal.getBlockY(), crystal.getBlockZ());

            if (++crystalSpawned >= stats.crystal.bonusCrystals)
                break;
        }
    }

    private static final ResourceLocation ENDERGETIC_CRYSTAL_HOLDER = new ResourceLocation("endergetic:crystal_holder");

    public static void generateCrystalInTower(Level level, int x, int y, int z) {
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        while (!level.getBlockState(centerPodium).is(Blocks.BEDROCK) && centerPodium.getY() > level.getSeaLevel()) {
            centerPodium = centerPodium.below();
        }

        int spawnY = y - 16;
        if (spawnY < centerPodium.getY())
            spawnY = centerPodium.getY();
        BlockPos crystalPos = new BlockPos(x, spawnY, z);

        Stream<BlockPos> blocks = BlockPos.betweenClosedStream(crystalPos.offset(-1, -1, -1), crystalPos.offset(1, 1, 1));

        blocks.forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));

        BlockState baseBlockState = Blocks.BEDROCK.defaultBlockState();
        if (ModList.get().isLoaded("endergetic"))
            baseBlockState = ForgeRegistries.BLOCKS.getValue(ENDERGETIC_CRYSTAL_HOLDER).defaultBlockState();
        level.setBlockAndUpdate(crystalPos.offset(0, -1, 0), baseBlockState);

        //level.explode(null, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Level.ExplosionInteraction.BLOCK);

        EndCrystal crystal = new EndCrystal(level, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
        level.addFreshEntity(crystal);
    }
    //endregion

    public static void setupLarvaCooldown(EnderDragon dragon, DragonStats stats) {
        int cooldown = (int) (Mth.nextInt(dragon.getRandom(), stats.larva.minCooldown, stats.larva.maxCooldown) * 0.5d);
        dragon.getPersistentData().putInt(DRAGON_LARVA_COOLDOWN, cooldown);
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
        tickLarva(dragon);
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

    public static void tickLarva(EnderDragon dragon) {
        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        CompoundTag dragonTags = dragon.getPersistentData();
        if (dragon.getHealth() <= 0)
            return;

        int cooldown = dragonTags.getInt(DragonFeature.DRAGON_LARVA_COOLDOWN);
        if (cooldown > 0) {
            dragonTags.putInt(DragonFeature.DRAGON_LARVA_COOLDOWN, cooldown - 1);
            return;
        }

        //If there is no player on the main island don't spawn larvae
        Level level = dragon.level();
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(64d);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bb);

        if (players.isEmpty())
            return;

        cooldown = Mth.nextInt(level.random, stats.get().larva.minCooldown, stats.get().larva.maxCooldown);
        dragonTags.putInt(DragonFeature.DRAGON_LARVA_COOLDOWN, cooldown - 1);

        for (int i = 0; i < stats.get().larva.spawned; i++) {
            float angle = level.random.nextFloat() * (float) Math.PI * 2f;
            float x = (float) Math.floor(Math.cos(angle) * 3.33f);
            float z = (float) Math.floor(Math.sin(angle) * 3.33f);
            int y = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(x, 255, z)).getY();
            summonLarva(level, new Vec3(x + 0.5, y, z + 0.5), stats.get());
        }
    }

    public static void summonLarva(Level level, Vec3 pos, DragonStats stats) {
        Larva larva = new Larva(PBEntities.LARVA.get(), level);
        CompoundTag minionTags = larva.getPersistentData();

        minionTags.putBoolean("mobspropertiesrandomness:processed", true);
        //TODO Scaling health

        larva.setPos(pos.x, pos.y, pos.z);
        larva.setPersistenceRequired();

        //MCUtils.applyModifier(larva, Attributes.ATTACK_DAMAGE, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS_UUID, Strings.AttributeModifiers.ATTACK_DAMAGE_BONUS, 0.35, AttributeModifier.Operation.ADDITION);
        MCUtils.applyModifier(larva, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2.5d, AttributeModifier.Operation.MULTIPLY_BASE);
        larva.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.larva.health);
        larva.setHealth(stats.larva.health);

        level.addFreshEntity(larva);
    }

    @SubscribeEvent
    public void onDragonDamage(LivingDamageEvent event) {
        if (!this.isEnabled()
                || !(event.getEntity() instanceof EnderDragon dragon))
            return;

        Optional<DragonStats> stats = getDragonStats(dragon);
        if (stats.isEmpty())
            return;
        meleeDamageMultiplier(event, dragon, stats.get());
        rangedDamageMultiplier(event, dragon, stats.get());
        explosionDamageMultiplier(event, dragon, stats.get());

        tryRespawningCrystalPhase(dragon, stats.get());
    }

    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> sittingPhases = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.TAKEOFF);

    private static void meleeDamageMultiplier(LivingDamageEvent event, EnderDragon dragon, DragonStats stats) {
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity))
            return;
        if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
            event.setAmount(event.getAmount() * stats.vulnerabilities.meleeDamageMultiplierWhenSitting);
        else
            event.setAmount(event.getAmount() * stats.vulnerabilities.meleeDamageMultiplierWhenNotSitting);
    }

    private static void rangedDamageMultiplier(LivingDamageEvent event, EnderDragon dragon, DragonStats stats) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile))
            return;
        event.setAmount(event.getAmount() * stats.vulnerabilities.rangedDamageMultiplier);
    }

    private static void explosionDamageMultiplier(LivingDamageEvent event, EnderDragon dragon, DragonStats stats) {
        if (!(event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypes.FIREWORKS)))
            return;
        event.setAmount(event.getAmount() * stats.vulnerabilities.explosionDamageMultiplier);
    }

    private static void respawningCrystalDamageMultiplier(LivingDamageEvent event, EnderDragon dragon, DragonStats stats) {
        if (!dragon.getPhaseManager().getCurrentPhase().isSitting() ||
                !CrystalRespawnPhase.isInThisPhase(dragon))
            return;
        event.setAmount(event.getAmount() * stats.vulnerabilities.respawningCrystalDamageMultiplier);
    }

    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_CRYSTAL_RESPAWN_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.HOLDING_PATTERN, EnderDragonPhase.TAKEOFF);

    public static void tryRespawningCrystalPhase(EnderDragon dragon, DragonStats stats) {
        CompoundTag dragonTags = dragon.getPersistentData();

        if (!VALID_CRYSTAL_RESPAWN_PHASES.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
            return;

        float healthRatio = dragon.getHealth() / dragon.getMaxHealth();
        if (healthRatio >= 0.80d)
            return;

        byte crystalRespawn = dragonTags.getByte(CRYSTAL_RESPAWN);

        //The first time, the chance is 0% at >=80% health and 100% at <=60% health. The health threshold decreases by 35% every time the enderdragon respawns the crystals
        //On 0 Respawns: 0% chance at health >=  80% and 100% at health <=  20%
        //On 1 Respawn : 0% chance at health >=  45% and  75% at health =    0%
        //On 2 Respawns: 0% chance at health >=  10% and  17% at health =    0%
        float chance = getChanceAtValue(healthRatio, 0.80f - (crystalRespawn * 0.35f), 0.20f - (crystalRespawn * 0.35f));

        if (dragon.getRandom().nextFloat() > chance)
            return;

        dragonTags.putByte(CRYSTAL_RESPAWN, (byte) (crystalRespawn + 1));

        double crystalsToRespawn = stats.crystal.crystalsRespawned;
        crystalsToRespawn = MathHelper.getAmountWithDecimalChance(dragon.getRandom(), crystalsToRespawn);
        if (crystalsToRespawn == 0d)
            return;

        dragon.getPhaseManager().setPhase(CrystalRespawnPhase.getPhaseType());
        CrystalRespawnPhase phase = (CrystalRespawnPhase) dragon.getPhaseManager().getCurrentPhase();

        List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel)dragon.level()));
        spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));
        int spawned = 0;
        for (SpikeFeature.EndSpike spike : spikes) {
            if (!dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox(), EndCrystal::showsBottom).isEmpty())
                continue;
            phase.addCrystalRespawn(spike);
            if (++spawned >= crystalsToRespawn)
                break;
        }
    }

    /**
     * Returns a percentage value (0~1) based off a min and max value. when value >= max the chance is 0%, when value <= min the chance is 100%. In-between the threshold, chance scales accordingly
     */
    private static float getChanceAtValue(float value, float max, float min) {
        return Mth.clamp((max - min - (value - min)) / (max - min), 0f, 1f);
    }

    public static boolean onCrystalDamagedByExplosion(DamageSource source) {
        if (!isEnabled(DragonFeature.class)
                || !explosionImmune)
            return false;

        return source.is(DamageTypeTags.IS_EXPLOSION);
    }

    public static Optional<DragonStats> getDragonStats(EnderDragon dragon) {
        int lvl = dragon.getPersistentData().getByte(LEVEL);
        return Optional.ofNullable(DragonStatsReloadListener.STATS_MAP.get(lvl));
    }
}
