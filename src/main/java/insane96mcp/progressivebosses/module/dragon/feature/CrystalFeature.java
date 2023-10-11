package insane96mcp.progressivebosses.module.dragon.feature;

import com.google.common.collect.ImmutableList;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Stream;

@Label(name = "Crystals", description = "Makes more Crystal spawn and with more cages.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class CrystalFeature extends Feature {
	@Config(min = -1)
	@Label(name = "More Cages at Difficulty", description = "At this difficulty cages will start to appear around other crystals too. -1 will disable this feature.")
	public static Integer moreCagesAtDifficulty = 1;
	@Config(min = 0, max = 8)
	@Label(name = "Max Bonus Cages", description = "Max number of bonus cages that can spawn around the crystals. (Vanilla already has 2 cages)")
	public static Integer maxBonusCages = 6;
	@Config(min = -1)
	@Label(name = "More Crystals at Difficulty", description = "At this difficulty one crystal will start to appear inside obsidian towers. -1 will disable this feature.")
	public static Integer moreCrystalsAtDifficulty = 2;
	@Config(min = -1)
	@Label(name = "More Crystals Step", description = "Every how much difficulty one more crystal will be spawned inside towers")
	public static Integer moreCrystalsStep = 3;
	@Config(min = 0, max = 10)
	@Label(name = "More Crystals Max", description = "Max number of bonus crystals that can spawn inside the towers.")
	public static Integer moreCrystalsMax = 3;
	@Config
	@Label(name = "Enable crystal respawn", description = "Everytime the dragon is hit (when below 50% of health) there's a chance to to trigger a Crystal respawn Phase. The chance is 0% when health >=50% and 100% when health <=30%, the health threshold decreases by 20% every time the dragon respawns crystals.")
	public static Boolean enableCrystalRespawn = true;
	@Config(min = 0d, max = 10d)
	@Label(name = "Crystal Respawn Per Difficulty", description = "At max Difficulty how many crystals will the dragon respawn.")
	public static Double crystalsRespawned = 3d;
	@Config
	@Label(name = "Explosion Immune", description = "Crystals can no longer be destroyed by other explosions.")
	public static Boolean explosionImmune = true;

	public CrystalFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_CRYSTAL_RESPAWN_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.HOLDING_PATTERN, EnderDragonPhase.TAKEOFF, EnderDragonPhase.CHARGING_PLAYER, EnderDragonPhase.STRAFE_PLAYER);

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		if (!enableCrystalRespawn)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();

		if (!VALID_CRYSTAL_RESPAWN_PHASES.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
			return;

		float healthRatio = dragon.getHealth() / dragon.getMaxHealth();
		if (healthRatio >= 0.80d)
			return;

		byte crystalRespawn = dragonTags.getByte(Strings.Tags.CRYSTAL_RESPAWN);

		//The first time, the chance is 0% at >=80% health and 100% at <=60% health. The health threshold decreases by 35% every time the enderdragon respawns the crystals
		//On 0 Respawns: 0% chance at health >=  80% and 100% at health <=  20%
		//On 1 Respawn : 0% chance at health >=  45% and  75% at health =    0%
		//On 2 Respawns: 0% chance at health >=  10% and  17% at health =    0%
		float chance = getChanceAtValue(healthRatio, 0.80f - (crystalRespawn * 0.35f), 0.20f - (crystalRespawn * 0.35f));

		if (dragon.getRandom().nextFloat() > chance)
			return;

		dragonTags.putByte(Strings.Tags.CRYSTAL_RESPAWN, (byte) (crystalRespawn + 1));

		double crystalsToRespawn = Mth.clamp(crystalsRespawned * DifficultyHelper.getScalingDifficulty(dragon), 0, SpikeFeature.NUMBER_OF_SPIKES);
		crystalsToRespawn = MathHelper.getAmountWithDecimalChance(dragon.getRandom(), crystalsToRespawn);
		if (crystalsToRespawn == 0d)
			return;

		dragon.getPhaseManager().setPhase(CrystalRespawnPhase.getPhaseType());
		CrystalRespawnPhase phase = (CrystalRespawnPhase) dragon.getPhaseManager().getCurrentPhase();

		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel)dragon.level()));
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius).reversed());
		for (int i = 0; i < crystalsToRespawn; i++) {
			SpikeFeature.EndSpike targetSpike = spikes.get(i);
			phase.addCrystalRespawn(targetSpike);
		}
	}

	/**
	 * Returns a percentage value (0~1) based off a min and max value. when value >= max the chance is 0%, when value <= min the chance is 100%. In-between the threshold, chance scales accordingly
	 */
	private float getChanceAtValue(float value, float max, float min) {
		return Mth.clamp((max - min - (value - min)) / (max - min), 0f, 1f);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		crystalCages(dragon, difficulty);
		moreCrystals(dragon, difficulty);
	}

	private static void crystalCages(EnderDragon dragon, float difficulty) {
		if (moreCagesAtDifficulty == -1
				|| maxBonusCages == 0
				|| difficulty < moreCagesAtDifficulty)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.CRYSTAL_CAGES))
			return;

		dragonTags.putBoolean(Strings.Tags.CRYSTAL_CAGES, true);

		ServerLevel serverLevel = (ServerLevel) dragon.level();

		//Order from smaller towers to bigger ones
		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level()));
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));

		int crystalsInvolved = Math.round(difficulty - moreCagesAtDifficulty + 1);
		int cagesGenerated = 0;

		for (SpikeFeature.EndSpike spike : spikes) {
			Optional<EndCrystal> crystal = dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).stream().findAny();
			if (crystal.isEmpty())
				continue;

			if (spike.isGuarded())
				continue;

			crystal.get().discard();
			spike.guarded = true;
			RandomSource random = RandomSource.create(-1157087832721040245L); // Generates 0.0058419704 for Yung's Better End Island to generate guarded
			net.minecraft.world.level.levelgen.feature.Feature.END_SPIKE.place(new SpikeConfiguration(true, ImmutableList.of(spike), null), serverLevel, serverLevel.getChunkSource().getGenerator(), random, crystal.get().blockPosition());
			spike.guarded = false;

			cagesGenerated++;
			if (cagesGenerated == crystalsInvolved || cagesGenerated == maxBonusCages)
				break;
		}
	}

	private static void moreCrystals(EnderDragon dragon, float difficulty) {
		if (moreCrystalsAtDifficulty == -1
				|| moreCrystalsMax == 0
				|| difficulty < moreCrystalsAtDifficulty)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.MORE_CRYSTALS))
			return;

		dragonTags.putBoolean(Strings.Tags.MORE_CRYSTALS, true);

		List<EndCrystal> crystals = new ArrayList<>();

		//Order from smaller towers to bigger ones
		List<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level()));
		spikes.sort(Comparator.comparingInt(SpikeFeature.EndSpike::getRadius));

		for(SpikeFeature.EndSpike spike : spikes) {
			crystals.addAll(dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox(), EndCrystal::showsBottom));
		}

		int crystalsMax = (int) Math.ceil((difficulty + 1 - moreCrystalsAtDifficulty) / moreCrystalsStep);
		if (crystalsMax <= 0)
			return;
		int crystalSpawned = 0;

		for (EndCrystal crystal : crystals) {
			generateCrystalInTower(dragon.level(), crystal.getX(), crystal.getY(), crystal.getZ());

			crystalSpawned++;
			if (crystalSpawned == crystalsMax || crystalSpawned == moreCrystalsMax)
				break;
		}
	}

	public static boolean onDamageFromExplosion(DamageSource source) {
		if (!isEnabled(CrystalFeature.class)
				|| !explosionImmune)
			return false;

		return source.is(DamageTypeTags.IS_EXPLOSION);
	}

	private static final ResourceLocation ENDERGETIC_CRYSTAL_HOLDER = new ResourceLocation("endergetic:crystal_holder");

	public static void generateCrystalInTower(Level level, double x, double y, double z) {
		BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		while (!level.getBlockState(centerPodium).is(Blocks.BEDROCK) && centerPodium.getY() > level.getSeaLevel()) {
			centerPodium = centerPodium.below();
		}

		int spawnY = (int) (y - Mth.nextInt(level.getRandom(), 12, 24));
		if (spawnY < centerPodium.getY())
			spawnY = centerPodium.getY();
		BlockPos crystalPos = BlockPos.containing(x, spawnY, z);

		Stream<BlockPos> blocks = BlockPos.betweenClosedStream(crystalPos.offset(-1, -1, -1), crystalPos.offset(1, 1, 1));

		blocks.forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));

		BlockState baseBlockState = Blocks.BEDROCK.defaultBlockState();
		if (ModList.get().isLoaded("endergetic"))
			baseBlockState = ForgeRegistries.BLOCKS.getValue(ENDERGETIC_CRYSTAL_HOLDER).defaultBlockState();
		level.setBlockAndUpdate(crystalPos.offset(0, -1, 0), baseBlockState);

		level.explode(null, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Level.ExplosionInteraction.BLOCK);

		EndCrystal crystal = new EndCrystal(level, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
		level.addFreshEntity(crystal);
	}


	//TODO remove and regen the pillar
	public static void generateCage(Level world, BlockPos pos) {
		//Shamelessly copied from Vanilla Code
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		for(int k = -2; k <= 2; ++k) {
			for(int l = -2; l <= 2; ++l) {
				for(int i1 = 0; i1 <= 3; ++i1) {
					boolean flag = Mth.abs(k) == 2;
					boolean flag1 = Mth.abs(l) == 2;
					boolean flag2 = i1 == 3;
					if (flag || flag1 || flag2) {
						boolean flag3 = k == -2 || k == 2 || flag2;
						boolean flag4 = l == -2 || l == 2 || flag2;
						BlockState blockstate = Blocks.IRON_BARS.defaultBlockState().setValue(IronBarsBlock.NORTH, flag3 && l != -2).setValue(IronBarsBlock.SOUTH, flag3 && l != 2).setValue(IronBarsBlock.WEST, flag4 && k != -2).setValue(IronBarsBlock.EAST, flag4 && k != 2);
						world.setBlockAndUpdate(blockpos$mutable.set(pos.getX() + k, pos.getY() - 1 + i1, pos.getZ() + l), blockstate);
					}
				}
			}
		}
	}
}
