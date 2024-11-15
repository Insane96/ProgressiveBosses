package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
    /**
     * How many times has the dragon respawned crystals
     */
    public static final String CRYSTAL_RESPAWN = ProgressiveBosses.RESOURCE_PREFIX + "crystal_respawn";
    private static final ResourceLocation ENDERGETIC_CRYSTAL_HOLDER = new ResourceLocation("endergetic:crystal_holder");
    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_CRYSTAL_RESPAWN_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.HOLDING_PATTERN, EnderDragonPhase.TAKEOFF);
    public int cages;
    public int bonusCrystals;
    public int crystalsRespawned;

    public DragonCrystal(int cages, int bonusCrystals, int crystalsRespawned) {
        this.cages = cages;
        this.bonusCrystals = bonusCrystals;
        this.crystalsRespawned = crystalsRespawned;
    }

    public static class Serializer implements JsonSerializer<DragonCrystal>, JsonDeserializer<DragonCrystal> {
        @Override
        public DragonCrystal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonCrystal(GsonHelper.getAsInt(json.getAsJsonObject(), "cages"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "bonus_crystals"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "crystals_respawned"));
        }

        @Override
        public JsonElement serialize(DragonCrystal src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cages", src.cages);
            jsonObject.addProperty("bonus_crystals", src.bonusCrystals);
            jsonObject.addProperty("crystals_respawned", src.crystalsRespawned);
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

    public static boolean onCrystalDamagedByExplosion(DamageSource source) {
        if (!Feature.isEnabled(DragonFeature.class)
                || !DragonFeature.explosionImmuneCrystals)
            return false;

        return source.is(DamageTypeTags.IS_EXPLOSION);
    }

    /**
     * Returns a percentage value (0~1) based off a min and max value. when value >= max the chance is 0%, when value <= min the chance is 100%. In-between the threshold, chance scales accordingly
     */
    private static float getChanceAtValue(float value, float max, float min) {
        return Mth.clamp((max - min - (value - min)) / (max - min), 0f, 1f);
    }
}
