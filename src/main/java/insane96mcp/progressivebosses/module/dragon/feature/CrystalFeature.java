package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Label(name = "Crystals", description = "Makes more Crystal spawn and with more cages.")
public class CrystalFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> moreCagesAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxBonusCagesConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> moreCrystalsAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxMoreCrystalsConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> enableCrystalRespawnConfig;
	private final ForgeConfigSpec.ConfigValue<Double> crystalRespawnMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Double> crystalRespawnInsideTowerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> explosionImmuneConfig;

	public int moreCagesAtDifficulty = 2;
	public int maxBonusCages = 4;
	public int moreCrystalsAtDifficulty = 8;
	public int maxMoreCrystals = 5;
	public boolean enableCrystalRespawn = true;
	public double crystalRespawnMultiplier = 0.6d;
	public double crystalRespawnInsideTowerChance = 0.005d;
	public boolean explosionImmune = true;

	public CrystalFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		moreCagesAtDifficultyConfig = Config.builder
				.comment("At this difficulty cages will start to appear around other crystals too. -1 will disable this feature.")
				.defineInRange("More Cages at Difficulty", moreCagesAtDifficulty, -1, Integer.MAX_VALUE);
		maxBonusCagesConfig = Config.builder
				.comment("Max number of bonus cages that can spawn around the crystals.")
				.defineInRange("Max Bonus Cages", maxBonusCages, 0, 8);
		moreCrystalsAtDifficultyConfig = Config.builder
				.comment("At this difficulty more crystals will start to appear inside obsidian towers. -1 will disable this feature.")
				.defineInRange("More Crystals at Difficulty", moreCrystalsAtDifficulty, -1, Integer.MAX_VALUE);
		maxMoreCrystalsConfig = Config.builder
				.comment("Max number of bonus crystals that can spawn inside the towers.")
				.defineInRange("Max Bonus Crystals", maxMoreCrystals, 0, 10);
		enableCrystalRespawnConfig = Config.builder
				.comment("Everytime the dragon is hit (when below 20% of health) there's a chance to to trigger a Crystal respawn Phase. The phase can only happen once. The chance is 0% when health >= 20% and 100% when health <= 5%.")
				.define("Enable crystal respawn", enableCrystalRespawn);
		crystalRespawnMultiplierConfig = Config.builder
				.comment("Difficulty multiplied by this number will output how many tries will the dragon take to respawn crystals. Tries are capped between 1 and 100.")
				.defineInRange("Crystal Respawn Multiplier", crystalRespawnMultiplier, 0d, 100d);
		crystalRespawnInsideTowerChanceConfig = Config.builder
				.comment("When respawning Crystals, chance for the crystal to be spawned inside a Tower instead of on top")
				.defineInRange("Crystal Respawn Inside Tower Chance", crystalRespawnInsideTowerChance, 0d, 1d);
		explosionImmuneConfig = Config.builder
				.comment("Crystals can no longer be destroyed by other explosions.")
				.define("Explosion Immune", explosionImmune);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.moreCagesAtDifficulty = this.moreCagesAtDifficultyConfig.get();
		this.maxBonusCages = this.maxBonusCagesConfig.get();
		this.moreCrystalsAtDifficulty = this.moreCrystalsAtDifficultyConfig.get();
		this.maxMoreCrystals = this.maxMoreCrystalsConfig.get();
		this.enableCrystalRespawn = this.enableCrystalRespawnConfig.get();
		this.crystalRespawnMultiplier = this.crystalRespawnMultiplierConfig.get();
		this.crystalRespawnInsideTowerChance = this.crystalRespawnInsideTowerChanceConfig.get();
		this.explosionImmune = this.explosionImmuneConfig.get();
	}

	private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_CRYSTAL_RESPAWN_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.HOLDING_PATTERN, EnderDragonPhase.TAKEOFF);

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon))
			return;

		if (!this.enableCrystalRespawn)
			return;

		EnderDragon dragon = (EnderDragon) event.getEntity();

		CompoundTag dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		if (dragonTags.getBoolean(Strings.Tags.CRYSTAL_RESPAWN))
			return;

		if (!VALID_CRYSTAL_RESPAWN_PHASES.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
			return;

		double healthRatio = dragon.getHealth() / dragon.getMaxHealth();
		if (healthRatio >= 0.2d)
			return;
		//Chance is 0% at >= 20% health and 100% at <= 5% health
		//0.15 - (0.20 - 0.05) = 0.00 / 0.15 =	0%
		//0.15 - (0.13 - 0.05) = 0.08 / 0.15 =~	46.7%
		//0.15 - (0.05 - 0.05) = 0.15 / 0.15 =	100%
		double chance = (0.15d - (healthRatio - 0.05d)) / 0.15d;

		if (dragon.getRandom().nextFloat() < chance)
			return;

		dragon.getPhaseManager().setPhase(CrystalRespawnPhase.getPhaseType());
		CrystalRespawnPhase phase = (CrystalRespawnPhase) dragon.getPhaseManager().getCurrentPhase();

		ArrayList<SpikeFeature.EndSpike> spikes = new ArrayList<>(SpikeFeature.getSpikesForLevel((ServerLevel)dragon.level));
		int maxTries = (int) Mth.clamp(difficulty * this.crystalRespawnMultiplier, 1, 100);
		for (int i = 0; i < maxTries; i++) {
			SpikeFeature.EndSpike targetSpike = spikes.get(RandomHelper.getInt(dragon.getRandom(), 0, spikes.size()));
			phase.addCrystalRespawn(targetSpike);
		}
		dragonTags.putBoolean(Strings.Tags.CRYSTAL_RESPAWN, true);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon))
			return;

		EnderDragon dragon = (EnderDragon) event.getEntity();
		CompoundTag dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		crystalCages(dragon, difficulty);
		moreCrystals(dragon, difficulty);
	}

	private void crystalCages(EnderDragon dragon, float difficulty) {
		if (this.moreCagesAtDifficulty == -1 || this.maxBonusCages == 0)
			return;

		if (difficulty < moreCagesAtDifficulty)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.CRYSTAL_CAGES))
			return;

		dragonTags.putBoolean(Strings.Tags.CRYSTAL_CAGES, true);

		List<EndCrystal> crystals = new ArrayList<>();

		for(SpikeFeature.EndSpike endspikefeature$endspike : SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level)) {
			crystals.addAll(dragon.level.getEntitiesOfClass(EndCrystal.class, endspikefeature$endspike.getTopBoundingBox()));
		}

		//Remove all the crystals that already have cages around
		crystals.removeIf(c -> c.level.getBlockState(c.blockPosition().above(2)).getBlock() == Blocks.IRON_BARS);
		//Shuffle the list
		Collections.shuffle(crystals);

		int crystalsInvolved = Math.round(difficulty - this.moreCagesAtDifficulty + 1);
		int cagesGenerated = 0;

		for (EndCrystal crystal : crystals) {
			generateCage(crystal.level, crystal.blockPosition());

			cagesGenerated++;
			if (cagesGenerated == crystalsInvolved || cagesGenerated == this.maxBonusCages)
				break;
		}
	}

	private void moreCrystals(EnderDragon dragon, float difficulty) {
		if (this.moreCrystalsAtDifficulty == -1 || this.maxMoreCrystals == 0)
			return;

		if (difficulty < this.moreCrystalsAtDifficulty)
			return;

		CompoundTag dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.MORE_CRYSTALS))
			return;

		dragonTags.putBoolean(Strings.Tags.MORE_CRYSTALS, true);

		List<EndCrystal> crystals = new ArrayList<>();

		for(SpikeFeature.EndSpike endspikefeature$endspike : SpikeFeature.getSpikesForLevel((ServerLevel) dragon.level)) {
			crystals.addAll(dragon.level.getEntitiesOfClass(EndCrystal.class, endspikefeature$endspike.getTopBoundingBox()));
		}

		//Shuffle the list
		Collections.shuffle(crystals);

		int crystalsInvolved = Math.round(difficulty - this.moreCrystalsAtDifficulty + 1);
		int crystalSpawned = 0;

		for (EndCrystal crystal : crystals) {
			generateCrystalInTower(dragon.level, crystal.getX(), crystal.getY(), crystal.getZ());

			crystalSpawned++;
			if (crystalSpawned == crystalsInvolved || crystalSpawned == this.maxMoreCrystals)
				break;
		}
	}

	public boolean onDamageFromExplosion(EndCrystal enderCrystalEntity, DamageSource source) {
		if (!this.isEnabled())
			return false;

		if (!this.explosionImmune)
			return false;

		return source.isExplosion();
	}

	private static final ResourceLocation ENDERGETIC_CRYSTAL_HOLDER_RL = new ResourceLocation("endergetic:crystal_holder");

	public static EndCrystal generateCrystalInTower(Level world, double x, double y, double z) {
		Vec3 centerPodium = Vec3.atBottomCenterOf(world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		int spawnY = (int) (y - RandomHelper.getInt(world.getRandom(), 12, 24));
		if (spawnY < centerPodium.y())
			spawnY = (int) centerPodium.y();
		BlockPos crystalPos = new BlockPos(x, spawnY, z);

		Stream<BlockPos> blocks = BlockPos.betweenClosedStream(crystalPos.offset(-1, -1, -1), crystalPos.offset(1, 1, 1));

		blocks.forEach(pos -> world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));

		BlockState baseBlockState = Blocks.BEDROCK.defaultBlockState();
		if (ModList.get().isLoaded("endergetic"))
			if (ForgeRegistries.BLOCKS.containsKey(ENDERGETIC_CRYSTAL_HOLDER_RL))
				baseBlockState = ForgeRegistries.BLOCKS.getValue(ENDERGETIC_CRYSTAL_HOLDER_RL).defaultBlockState();
			else
				LogHelper.warn("The Endergetic Expansion is loaded but the %s block was not registered", ENDERGETIC_CRYSTAL_HOLDER_RL);
		world.setBlockAndUpdate(crystalPos.offset(0, -1, 0), baseBlockState);

		world.explode(null, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Explosion.BlockInteraction.DESTROY);

		EndCrystal crystal = new EndCrystal(world, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
		world.addFreshEntity(crystal);

		return crystal;
	}

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
