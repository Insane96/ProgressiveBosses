package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@JsonAdapter(DragonCrystal.Serializer.class)
public class DragonCrystal {

    public static final String DRAGON_PHANTOM = ProgressiveBosses.RESOURCE_PREFIX + "dragon_phantom";
    public static final String PHANTOM_CRYSTAL = ProgressiveBosses.RESOURCE_PREFIX + "crystal";

    private static final ResourceLocation ENDERGETIC_CRYSTAL_HOLDER = new ResourceLocation("endergetic:crystal_holder");
    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_CRYSTAL_RESPAWN_PHASES = Arrays.asList(EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.HOLDING_PATTERN, EnderDragonPhase.TAKEOFF);
    public int cages;
    public int bonusCrystals;
    public int crystalsRespawned;
    public int timeToRespawn;
    public int phantomCount;
    public int phantomSize;
    public float respawnCagedChance;
    public float respawnCrystalsBelowHealth;
    public float maxRespawnChance;
    public float maxRespawnChanceAtHealth;

    public DragonCrystal(int cages, int bonusCrystals, int crystalsRespawned, int timeToRespawn, int phantomCount, int phantomSize, float respawnCagedChance, float respawnCrystalsBelowHealth, float maxRespawnChance, float maxRespawnChanceAtHealth) {
        this.cages = cages;
        this.bonusCrystals = bonusCrystals;
        this.crystalsRespawned = crystalsRespawned;
        this.timeToRespawn = timeToRespawn;
        this.phantomCount = phantomCount;
        this.phantomSize = phantomSize;
        this.respawnCagedChance = respawnCagedChance;
        this.respawnCrystalsBelowHealth = respawnCrystalsBelowHealth;
        this.maxRespawnChance = maxRespawnChance;
        this.maxRespawnChanceAtHealth = maxRespawnChanceAtHealth;
    }

    public static class Serializer implements JsonSerializer<DragonCrystal>, JsonDeserializer<DragonCrystal> {
        @Override
        public DragonCrystal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonCrystal(GsonHelper.getAsInt(json.getAsJsonObject(), "cages"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "bonus_crystals"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "crystals_respawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "time_to_respawn"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "phantom_count"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "phantom_size"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "respawn_caged_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "respawn_crystals_below_health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "max_respawn_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "max_respawn_chance_at_health"));
        }

        @Override
        public JsonElement serialize(DragonCrystal src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cages", src.cages);
            jsonObject.addProperty("bonus_crystals", src.bonusCrystals);
            jsonObject.addProperty("crystals_respawned", src.crystalsRespawned);
            jsonObject.addProperty("time_to_respawn", src.timeToRespawn);
            jsonObject.addProperty("phantom_count", src.phantomCount);
            jsonObject.addProperty("phantom_size", src.phantomSize);
            jsonObject.addProperty("respawn_caged_chance", src.respawnCagedChance);
            jsonObject.addProperty("respawn_crystals_below_health", src.respawnCrystalsBelowHealth);
            jsonObject.addProperty("max_respawn_chance", src.maxRespawnChance);
            jsonObject.addProperty("max_respawn_chance_at_health", src.maxRespawnChanceAtHealth);
            return jsonObject;
        }
    }

    public static void moreCrystals(EnderDragon dragon, DragonStats stats) {
        if (stats.crystal.bonusCrystals <= 0)
            return;
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

    /**
     * Returns true if the phase has been changed
     */
    public static boolean onPhaseChange(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        if (event.getOldPhase() != null
                && !VALID_CRYSTAL_RESPAWN_PHASES.contains(event.getOldPhase())
                && !dragon.isDeadOrDying())
            return false;

        float healthRatio = dragon.getHealth() / dragon.getMaxHealth();

        //0% when health >= 80%, 20% when health <= 0%
        float chance = getChanceAtValue(healthRatio, stats.crystal.respawnCrystalsBelowHealth, stats.crystal.maxRespawnChanceAtHealth, 0, stats.crystal.maxRespawnChance);
        if (event.getOldPhase() == EnderDragonPhase.SITTING_FLAMING)
            chance *= 2;

        if (dragon.getRandom().nextFloat() > chance)
            return false;

        //dragonTags.putByte(CRYSTAL_RESPAWN, (byte) (crystalRespawn + 1));

        event.setNewPhase(CrystalRespawnPhase.getPhaseType());
        return true;
    }

    public static void onPhaseBegin(DragonPhaseEvent.Begin event, EnderDragon dragon, DragonStats stats) {
        double crystalsToRespawn = stats.crystal.crystalsRespawned;
        crystalsToRespawn = MathHelper.getAmountWithDecimalChance(dragon.getRandom(), crystalsToRespawn);
        if (crystalsToRespawn == 0d)
            return;

        if (!(event.getPhaseInstance() instanceof CrystalRespawnPhase phase))
            return;
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

    public static boolean onCrystalDamagedByExplosion(DamageSource source) {
        if (!Feature.isEnabled(DragonFeature.class)
                || !DragonFeature.explosionImmuneCrystals)
            return false;

        return source.is(DamageTypeTags.IS_EXPLOSION);
    }

    /**
     * Returns a value (outputMin~outputMax) based off a min and max value. when value >= max the chance is outputMin. when value <= min the chance is outputMax. In-between the threshold, chance scales accordingly
     */
    private static float getChanceAtValue(float value, float max, float min, float outputMin, float outputMax) {
        float clampedValue = Mth.clamp((max - min - (value - min)) / (max - min), 0f, 1f);
        return outputMin + clampedValue * (outputMax - outputMin);
    }

    public static void onPhantomHurt(LivingHurtEvent event) {
        if (!event.getEntity().getPersistentData().contains(DRAGON_PHANTOM)
                || !(event.getSource().getEntity() instanceof EnderDragon))
            return;

        event.setAmount(event.getAmount() * 0.1f);
    }

    public static void tickCrystalPhantom(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide
                || event.getEntity().tickCount % 30 != 0
                || !event.getEntity().getPersistentData().contains(PHANTOM_CRYSTAL))
            return;

        Entity crystal = ((ServerLevel)event.getEntity().level()).getEntity(event.getEntity().getPersistentData().getUUID(PHANTOM_CRYSTAL));
        if (crystal == null || crystal.isRemoved()) {
            if (!event.getEntity().getPersistentData().contains("crystal_death")) {
                event.getEntity().getPersistentData().putLong("crystal_death", event.getEntity().level().getGameTime());
            }
            else if (event.getEntity().level().getGameTime() - event.getEntity().getPersistentData().getLong("crystal_death") > 20 * 30) {
                event.getEntity().kill();
            }
        }
    }
}
