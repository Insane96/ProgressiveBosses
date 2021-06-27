package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.HoverPhase;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraft.world.gen.feature.EndSpikeFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

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
	private final ForgeConfigSpec.ConfigValue<Double> crystalRespawnChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> crystalRespawnMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Double> crystalRespawnInsideTowerChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> explosionImmuneConfig;

	public int moreCagesAtDifficulty = 1;
	public int maxBonusCages = 4;
	public int moreCrystalsAtDifficulty = 8;
	public int maxMoreCrystals = 5;
	public double crystalRespawnChance = 0.05d;
	public double crystalRespawnMultiplier = 0.15d;
	public double crystalRespawnInsideTowerChance = 0.005d;
	public boolean explosionImmune = true;

	public CrystalFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
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
		crystalRespawnChanceConfig = Config.builder
				.comment("Chance everytime the dragon is hit (when below 15% of health) to trigger a Crystal respawn Phase. The phase can only happen once. 1 means that the dragon will respawn crystals as soon as she's hit when below 15% health.")
				.defineInRange("Crystal Respawn Chance", crystalRespawnChance, 0d, 1d);
		crystalRespawnMultiplierConfig = Config.builder
				.comment("Difficulty multiplied by this number will output how many tries will the dragon take to respawn crystals. Tries are capped between 1 and 100.")
				.defineInRange("Crystal Respawn Multiplier", crystalRespawnMultiplier, 0d, 1d);
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
		this.crystalRespawnChance = this.crystalRespawnChanceConfig.get();
		this.crystalRespawnMultiplier = this.crystalRespawnMultiplierConfig.get();
		this.crystalRespawnInsideTowerChance = this.crystalRespawnInsideTowerChanceConfig.get();
		this.explosionImmune = this.explosionImmuneConfig.get();
	}

	private final ArrayList<EndSpikeFeature.EndSpike> spikesToRespawn = new ArrayList<>();
	private boolean engaged = false;
	private int tick = 0;

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntityLiving().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT dragonTags = dragon.getPersistentData();
		if (!dragonTags.getBoolean(Strings.Tags.CRYSTAL_RESPAWN))
			return;

		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		if (dragon.getPhaseManager().getCurrentPhase().getType().equals(PhaseType.HOVER)) {
			int tickSpawnCystal = (int) (50 - (difficulty / 4));
			HoverPhase phase = (HoverPhase) dragon.getPhaseManager().getCurrentPhase();
			if (spikesToRespawn.isEmpty()) {
				dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
				return;
			}
			if (!engaged) {
				double d0 = phase.targetLocation == null ? 0.0D : phase.targetLocation.squareDistanceTo(dragon.getPosX(), dragon.getPosY(), dragon.getPosZ());
				if (d0 < 16d) {
					dragon.setMotion(Vector3d.ZERO);
					engaged = true;
				}
			}
			else {
				tick++;
				dragon.setMotion(Vector3d.ZERO);
				if (tick <= 25)
					dragon.playSound(SoundEvents.ENTITY_ENDER_DRAGON_GROWL, 4F, 1.0F);
				if (tick >= tickSpawnCystal) {
					if (dragon.getHealth() < 10f) {
						dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
						return;
					}
					EnderCrystalEntity crystal;
					double x = spikesToRespawn.get(0).getCenterX();
					double y = spikesToRespawn.get(0).getHeight();
					double z = spikesToRespawn.get(0).getCenterZ();
					if (dragon.getRNG().nextDouble() < this.crystalRespawnInsideTowerChance * difficulty)
						crystal = generateCrystalInTower(dragon.world, x + 0.5, y + 1, z + 0.5);
					else {
						crystal = new EnderCrystalEntity(dragon.world, x + 0.5, y + 1, z + 0.5);
						crystal.setShowBottom(true);
						crystal.world.createExplosion(dragon, x + 0.5, y + 1.5, z + 0.5, 5f, Explosion.Mode.NONE);
						dragon.world.addEntity(crystal);
						generateCage(crystal.world, crystal.getPosition());
					}
					dragon.attackEntityPartFrom(dragon.dragonPartHead, DamageSource.causeExplosionDamage(dragon), 10f);
					spikesToRespawn.remove(0);
					if (spikesToRespawn.isEmpty()) {
						dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
						return;
					}
					tick = 0;
					engaged = false;
					x = spikesToRespawn.get(0).getCenterX();
					y = spikesToRespawn.get(0).getHeight();
					z = spikesToRespawn.get(0).getCenterZ();
					phase.targetLocation = new Vector3d(x + 0.5, y + 5.5, z + 0.5);
				}
			}
		}
	}

	private static final List<PhaseType<? extends IPhase>> validPhases = Arrays.asList(PhaseType.SITTING_SCANNING, PhaseType.HOLDING_PATTERN, PhaseType.TAKEOFF);

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		if (dragon.getHealth() > dragon.getMaxHealth() * 0.15d)
			return;

		CompoundNBT dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		if (dragonTags.getBoolean(Strings.Tags.CRYSTAL_RESPAWN))
			return;

		if (dragon.getFightManager() != null && dragon.getFightManager().getNumAliveCrystals() != 0)
			return;

		if (!validPhases.contains(dragon.getPhaseManager().getCurrentPhase().getType()))
			return;

		if (dragon.getRNG().nextFloat() < this.crystalRespawnChance)
			return;

		spikesToRespawn.clear();
		ArrayList<EndSpikeFeature.EndSpike> spikes = new ArrayList<>(EndSpikeFeature.func_236356_a_((ServerWorld)dragon.world));
		int maxTries = (int) MathHelper.clamp(difficulty * this.crystalRespawnMultiplier, 1, 100);
		for (int i = 0; i < maxTries; i++) {
			EndSpikeFeature.EndSpike targetSpike = spikes.get(RandomHelper.getInt(dragon.getRNG(), 0, spikes.size()));
			if (spikesToRespawn.contains(targetSpike))
				continue;
			spikesToRespawn.add(targetSpike);
		}
		dragon.getPhaseManager().setPhase(PhaseType.HOVER);
		HoverPhase hover = (HoverPhase) dragon.getPhaseManager().getCurrentPhase();
		hover.targetLocation = new Vector3d(spikesToRespawn.get(0).getCenterX() + 0.5, spikesToRespawn.get(0).getHeight() + 5, spikesToRespawn.get(0).getCenterZ() + 0.5);
		dragonTags.putBoolean(Strings.Tags.CRYSTAL_RESPAWN, true);
		tick = 0;
		engaged = false;
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT dragonTags = dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		crystalCages(dragon, difficulty);
		moreCrystals(dragon, difficulty);
	}

	private void crystalCages(EnderDragonEntity dragon, float difficulty) {
		if (this.moreCagesAtDifficulty == -1 || this.maxBonusCages == 0)
			return;

		if (difficulty < moreCagesAtDifficulty)
			return;

		CompoundNBT dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.CRYSTAL_CAGES))
			return;

		dragonTags.putBoolean(Strings.Tags.CRYSTAL_CAGES, true);

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getLoadedEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Remove all the crystals that already have cages around
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().up(2)).getBlock() == Blocks.IRON_BARS);
		//Shuffle the list
		Collections.shuffle(crystals);
		//Order by the lowest crystal
		//crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(difficulty - this.moreCagesAtDifficulty + 1);
		int cagesGenerated = 0;

		for (EnderCrystalEntity crystal : crystals) {
			generateCage(crystal.world, crystal.getPosition());

			cagesGenerated++;
			if (cagesGenerated == crystalsInvolved || cagesGenerated == this.maxBonusCages)
				break;
		}
	}

	private void moreCrystals(EnderDragonEntity dragon, float difficulty) {
		if (this.moreCrystalsAtDifficulty == -1 || this.maxMoreCrystals == 0)
			return;

		if (difficulty < this.moreCrystalsAtDifficulty)
			return;

		CompoundNBT dragonTags = dragon.getPersistentData();
		if (dragonTags.contains(Strings.Tags.MORE_CRYSTALS))
			return;

		dragonTags.putBoolean(Strings.Tags.MORE_CRYSTALS, true);

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getLoadedEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Shuffle the list
		Collections.shuffle(crystals);
		//Order by the lowest crystal
		//crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(difficulty - this.moreCrystalsAtDifficulty + 1);
		int crystalSpawned = 0;

		for (EnderCrystalEntity crystal : crystals) {
			generateCrystalInTower(dragon.world, crystal.getPosX(), crystal.getPosY(), crystal.getPosZ());

			crystalSpawned++;
			if (crystalSpawned == crystalsInvolved || crystalSpawned == this.maxMoreCrystals)
				break;
		}
	}

	public boolean onDamageFromExplosion(EnderCrystalEntity enderCrystalEntity, DamageSource source) {
		if (!this.isEnabled())
			return false;

		if (!this.explosionImmune)
			return false;

		return source.isExplosion();
	}

	private static EnderCrystalEntity generateCrystalInTower(World world, double x, double y, double z) {
		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		int spawnY = (int) (y - RandomHelper.getInt(world.getRandom(), 12, 24));
		if (spawnY < centerPodium.getY())
			spawnY = (int) centerPodium.getY();
		BlockPos crystalPos = new BlockPos(x, spawnY, z);

		Stream<BlockPos> blocks = BlockPos.getAllInBox(crystalPos.add(-1, -1, -1), crystalPos.add(1, 1, 1));

		blocks.forEach(pos -> world.setBlockState(pos, Blocks.AIR.getDefaultState()));
		world.setBlockState(crystalPos.add(0, -1, 0), Blocks.BEDROCK.getDefaultState());

		world.createExplosion(null, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Explosion.Mode.DESTROY);

		EnderCrystalEntity crystal = new EnderCrystalEntity(world, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
		world.addEntity(crystal);

		return crystal;
	}

	private static void generateCage(World world, BlockPos pos) {
		//Shamelessly copied from Vanilla Code
		BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
		for(int k = -2; k <= 2; ++k) {
			for(int l = -2; l <= 2; ++l) {
				for(int i1 = 0; i1 <= 3; ++i1) {
					boolean flag = MathHelper.abs(k) == 2;
					boolean flag1 = MathHelper.abs(l) == 2;
					boolean flag2 = i1 == 3;
					if (flag || flag1 || flag2) {
						boolean flag3 = k == -2 || k == 2 || flag2;
						boolean flag4 = l == -2 || l == 2 || flag2;
						BlockState blockstate = Blocks.IRON_BARS.getDefaultState().with(PaneBlock.NORTH, flag3 && l != -2).with(PaneBlock.SOUTH, flag3 && l != 2).with(PaneBlock.WEST, flag4 && k != -2).with(PaneBlock.EAST, flag4 && k != 2);
						world.setBlockState(blockpos$mutable.setPos(pos.getX() + k, pos.getY() - 1 + i1, pos.getZ() + l), blockstate);
					}
				}
			}
		}
	}
}
