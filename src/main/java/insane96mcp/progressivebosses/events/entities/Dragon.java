package insane96mcp.progressivebosses.events.entities;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.events.entities.ai.DragonMinionAttackNearestGoal;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.utils.MathRandom;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.DimensionType;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Dragon {
	public static void setStats(EntityJoinWorldEvent event) {
		if (!event.getWorld().getDimensionKey().getLocation().equals(DimensionType.THE_END.getLocation()))
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		if (dragon.getFightManager() == null)
			return;

		CompoundNBT tags = dragon.getPersistentData();
		boolean alreadyProcessed = tags.getBoolean("progressivebosses:processed");

		if (alreadyProcessed)
			return;

		tags.putBoolean("progressivebosses:processed", true);

		int radius = 160;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = event.getWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		if (players.size() == 0)
			return;

		int c;
		int eggsToDrop = 0;

		float killedCount = 0;
		for (ServerPlayerEntity player : players) {
			CompoundNBT playerTags = player.getPersistentData();
			c = playerTags.getInt("progressivebosses:killed_dragons");
			if (c == 0) {
				dragon.getFightManager().previouslyKilled = false;

				eggsToDrop++;
			}
			killedCount += c;
		}

		tags.putInt("progressivebosses:eggs_to_drop", eggsToDrop);


		if (killedCount == 0)
			return;

		if (!ModConfig.COMMON.dragon.general.sumKilledDragonsDifficulty.get() && killedCount > 0)
			killedCount /= players.size();

		setHealth(dragon, killedCount);
		crystalCages(dragon, killedCount);
		moreCrystals(dragon, killedCount);

		tags.putFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty", killedCount);
	}

	private static void crystalCages(EnderDragonEntity dragon, float killedCount) {
		int moreCagesAtDifficulty = ModConfig.COMMON.dragon.crystal.moreCagesAtDifficulty.get();

		if (moreCagesAtDifficulty == -1)
			return;

		if (killedCount < moreCagesAtDifficulty)
			return;

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Remove all the crystals that already have cages around
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().up(2)).getBlock() == Blocks.IRON_BARS);
		//Order by the lowest crystal
		crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(killedCount - moreCagesAtDifficulty + 1);
		int cagesGenerated = 0;

		for (EnderCrystalEntity crystal : crystals) {

			//Shamelessly copied from MC Code
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
							crystal.world.setBlockState(blockpos$mutable.setPos(crystal.getPosX() + k, crystal.getPosY() - 1 + i1, crystal.getPosZ() + l), blockstate);
						}
					}
				}
			}

			cagesGenerated++;
			if (cagesGenerated == crystalsInvolved)
				break;
		}
	}

	private static void moreCrystals(EnderDragonEntity dragon, float killedCount) {
		int moreCrystalsAtDifficulty = ModConfig.COMMON.dragon.crystal.moreCrystalsAtDifficulty.get();

		if (moreCrystalsAtDifficulty == -1)
			return;

		if (killedCount < moreCrystalsAtDifficulty)
			return;

		Vector3d centerPodium = Vector3d.copyCenteredHorizontally(dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION));

		AxisAlignedBB bbCrystals = new AxisAlignedBB(centerPodium.add(-64, -16, -64), centerPodium.add(64, 64, 64));

		List<EnderCrystalEntity> crystals = dragon.world.getEntitiesWithinAABB(EnderCrystalEntity.class, bbCrystals);
		//Remove the 4 crystals at the center
		crystals.removeIf(c -> Math.sqrt(c.getDistanceSq(centerPodium)) <= 10d);
		//Remove all the crystals that aren't on bedrock (so any player placed crystal or leftovers from previous fight will not be counted)
		crystals.removeIf(c -> c.world.getBlockState(c.getPosition().down()).getBlock() != Blocks.BEDROCK);
		//Order by the lowest crystal
		crystals.sort(Comparator.comparingDouble(Entity::getPosY));

		int crystalsInvolved = Math.round(killedCount - moreCrystalsAtDifficulty + 1);
		int crystalSpawned = 0;

		for (EnderCrystalEntity crystal : crystals) {
			BlockPos crystalPos = new BlockPos(crystal.getPosX(), crystal.getPosY() - 16, crystal.getPosZ());
			if (crystalPos.getY() < centerPodium.getY())
				crystalPos = new BlockPos(crystalPos.getX(), centerPodium.getY(), crystalPos.getZ());

			Stream<BlockPos> blocks = BlockPos.getAllInBox(crystalPos.add(-1, -1, -1), crystalPos.add(1, 1, 1));

			blocks.forEach(pos -> dragon.world.setBlockState(pos, Blocks.AIR.getDefaultState()));
			dragon.world.setBlockState(crystalPos.add(0, -1, 0), Blocks.BEDROCK.getDefaultState());

			dragon.world.createExplosion(dragon, crystalPos.getX() + .5f, crystalPos.getY(), crystalPos.getZ() + .5, 5f, Explosion.Mode.DESTROY);

			EnderCrystalEntity newCrystal = new EnderCrystalEntity(dragon.world, crystalPos.getX() + .5, crystalPos.getY(), crystalPos.getZ() + .5);
			//newCrystal.setShowBottom(false);
			dragon.world.addEntity(newCrystal);

			crystalSpawned++;
			if (crystalSpawned == crystalsInvolved)
				break;
		}
	}

	private static void setHealth(EnderDragonEntity dragon, float killedCount) {
		ModifiableAttributeInstance attribute = dragon.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier healthModifier = new AttributeModifier(ProgressiveBosses.RESOURCE_PREFIX + "dragon_bonus_health", killedCount * ModConfig.COMMON.dragon.health.bonusPerDifficulty.get(), AttributeModifier.Operation.ADDITION);
		attribute.applyPersistentModifier(healthModifier);
		//attribute.setBaseValue(attribute.getBaseValue() + (killedCount * ModConfig.COMMON.dragon.health.bonusPerDifficulty.get()));
		dragon.setHealth((float) attribute.getValue());
	}

	private static void dropMoreExperience(EnderDragonEntity dragon, World world) {
		if (dragon.deathTicks != 150)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		int radius = 160;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		int playersAround = players.size();

		int playersFirstTime = tags.getInt("progressivebosses:eggs_to_drop");

		boolean previouslyKilledDragon = false;
		if (dragon.getFightManager() != null)
			previouslyKilledDragon = dragon.getFightManager().hasPreviouslyKilledDragon();

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		int xp = 0;

		if (!previouslyKilledDragon) {
			playersFirstTime--;

			//Add XP for players that killed the first time
			if (playersAround > 1)
				xp += 12000 * playersFirstTime;

			//Add XP per more than 1 player around
			if (playersAround > 1)
				xp += (500 * ((ModConfig.COMMON.dragon.rewards.bonusExperience.get() + 1) * difficulty / 100f) * (playersAround - playersFirstTime));

		} else {

			//Add bonus XP for the normal experience drop
			xp += (int) (500 * (ModConfig.COMMON.dragon.rewards.bonusExperience.get() * difficulty / 100f));

			//Add XP per more than 1 player around
			if (playersAround > 1)
				xp += (500 * ((ModConfig.COMMON.dragon.rewards.bonusExperience.get() + 1) * difficulty / 100f) * (playersAround - 1));
		}

		while (xp > 0) {
			int i = ExperienceOrbEntity.getXPSplit(xp);
			xp -= i;
			world.addEntity(new ExperienceOrbEntity(dragon.world, dragon.getPositionVec().getX(), dragon.getPositionVec().getY(), dragon.getPositionVec().getZ(), i));
		}
	}

	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT tags = dragon.getPersistentData();
		if (tags.getBoolean("progressivebosses:has_been_killed"))
			return;
		tags.putBoolean("progressivebosses:has_been_killed", true);

		int radius = 160;
		BlockPos pos1 = new BlockPos(-radius, -radius, -radius);
		BlockPos pos2 = new BlockPos(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = dragon.world.getEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		if (players.size() == 0)
			return;

		int c;
		for (ServerPlayerEntity player : players) {
			CompoundNBT playerTags = player.getPersistentData();
			c = playerTags.getInt("progressivebosses:killed_dragons");
			if (c >= ModConfig.COMMON.dragon.general.maxDifficulty.get())
				continue;
			playerTags.putInt("progressivebosses:killed_dragons", c + 1);
		}
	}


	public static void onPlayerDamage(LivingHurtEvent event) {
		onDirectDamage(event);
		onAcidDamage(event);
	}

	private static void onDirectDamage(LivingHurtEvent event) {
		if (!(event.getSource().getImmediateSource() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getSource().getImmediateSource();
		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty");

		if (difficulty == 0)
			return;

		event.setAmount((float) (event.getAmount() * (1 + difficulty * (ModConfig.COMMON.dragon.attack.bonusAttackDamage.get() / 100.0))));
	}

	private static void onAcidDamage(LivingHurtEvent event) {
		if (!(event.getSource().getTrueSource() instanceof EnderDragonEntity))
			return;

		if (!(event.getSource().getImmediateSource() instanceof AreaEffectCloudEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getSource().getTrueSource();
		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty");

		if (difficulty == 0)
			return;

		event.setAmount((float) (event.getAmount() * (1 + difficulty * (ModConfig.COMMON.dragon.attack.bonusAcidPoolDamage.get() / 100.0))));
	}

	private static void dropEgg(EnderDragonEntity dragon, World world) {
		if (dragon.deathTicks != 100)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		int eggsToDrop = tags.getInt("progressivebosses:eggs_to_drop");

		if (dragon.getFightManager() != null && !dragon.getFightManager().hasPreviouslyKilledDragon()) {
			eggsToDrop--;
		}

		for (int i = 0; i < eggsToDrop; i++) {
			world.setBlockState(new BlockPos(0, 255 - i, 0), Blocks.DRAGON_EGG.getDefaultState());
		}
	}


	public static void update(LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		World world = event.getEntity().world;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
		CompoundNBT tags = dragon.getPersistentData();

		spitFireball(dragon);
		chargePlayer(dragon);
		spawnEndermites(dragon, world);
		spawnShulkers(dragon, world);
		heal(dragon, tags);
		dropEgg(dragon, world);
		dropMoreExperience(dragon, world);
	}

	private static void spitFireball(EnderDragonEntity dragon) {
		if (dragon.getFightManager() == null)
			return;

		if (dragon.getPhaseManager().getCurrentPhase().getType() != PhaseType.HOLDING_PATTERN)
			return;

		double fireballMaxChance = ModConfig.COMMON.dragon.attack.fireballMaxChance.get();
		double maxChanceAtDifficulty = ModConfig.COMMON.dragon.attack.maxChanceAtDifficulty.get();

		if (fireballMaxChance == 0f)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty");

		double chance = (fireballMaxChance / 100d) / maxChanceAtDifficulty;
		chance *= difficulty;
		int crystalsAlive = dragon.getFightManager().getNumAliveCrystals() + 1;
		chance *= (1f / crystalsAlive);

		if (chance > fireballMaxChance)
			chance = fireballMaxChance;

		if (Math.random() < chance) {
			ServerPlayerEntity player = (ServerPlayerEntity) dragon.world.getClosestPlayer(new EntityPredicate().setDistance(128.0D), dragon, dragon.getPosX(), dragon.getPosX(), dragon.getPosX());

			if (player == null)
				return;

			dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
			dragon.getPhaseManager().getPhase(PhaseType.STRAFE_PLAYER).setTarget(player);
		}
	}

	private static void chargePlayer(EnderDragonEntity dragon) {
		if (dragon.getFightManager() == null)
			return;

		if (dragon.getPhaseManager().getCurrentPhase().getType() != PhaseType.HOLDING_PATTERN)
			return;

		double chargePlayerMaxChance = ModConfig.COMMON.dragon.attack.chargePlayerMaxChance.get();
		double maxChanceAtDifficulty = ModConfig.COMMON.dragon.attack.maxChanceAtDifficulty.get();

		if (chargePlayerMaxChance == 0f)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty");

		double chance = (chargePlayerMaxChance / 100d) / maxChanceAtDifficulty;
		chance *= difficulty;
		int crystalsAlive = dragon.getFightManager().getNumAliveCrystals() + 1;
		chance *= (1f / crystalsAlive);

		if (chance > chargePlayerMaxChance)
			chance = chargePlayerMaxChance;

		if (Math.random() < chance) {
			ServerPlayerEntity player = (ServerPlayerEntity) dragon.world.getClosestPlayer(new EntityPredicate().setDistance(128.0D), dragon, dragon.getPosX(), dragon.getPosX(), dragon.getPosX());

			if (player == null)
				return;

			dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
			dragon.getPhaseManager().getPhase(PhaseType.CHARGING_PLAYER).setTarget(player.getPositionVec());
		}
	}

	private static void heal(EnderDragonEntity dragon, CompoundNBT tags) {
		if (ModConfig.COMMON.dragon.health.maximumBonusRegen.get() == 0.0f)
			return;

		if (dragon.ticksExisted % 20 != 0)
			return;

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		if (difficulty == 0)
			return;

		double maxHeal = ModConfig.COMMON.dragon.health.maximumBonusRegen.get();
		double heal = difficulty * ModConfig.COMMON.dragon.health.bonusRegenPerSpawned.get();

		if (heal > maxHeal)
			heal = maxHeal;

		float health = dragon.getHealth();

		if (dragon.getHealth() < dragon.getMaxHealth() && dragon.getHealth() > 0.0f)
			dragon.setHealth((float) (health + heal));
	}

	private static void spawnEndermites(EnderDragonEntity dragon, World world) {
		if (ModConfig.COMMON.dragon.larvae.maxSpawned.get() == 0)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		//Mobs Properties Randomness
		//tags.putBoolean("mobspropertiesrandomness:checked", true);

		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < ModConfig.COMMON.dragon.larvae.difficultyToSpawnOneMore.get())
			return;

		int cooldown = tags.getInt("progressivebosses:endermites_cooldown");
		if (cooldown > 0) {
			tags.putInt("progressivebosses:endermites_cooldown", cooldown - 1);
		} else {
			int cooldownReduction = (int) (difficulty * ModConfig.COMMON.dragon.larvae.cooldownReduction.get());
			cooldown = MathRandom.getInt(world.rand, ModConfig.COMMON.dragon.larvae.minCooldown.get() - cooldownReduction, ModConfig.COMMON.dragon.larvae.maxCooldown.get() - cooldownReduction);
			tags.putInt("progressivebosses:endermites_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i / ModConfig.COMMON.dragon.larvae.difficultyToSpawnOneMore.get() > ModConfig.COMMON.dragon.larvae.maxSpawned.get())
					break;

				if (i % ModConfig.COMMON.dragon.larvae.difficultyToSpawnOneMore.get() == 0) {
					EndermiteEntity endermite = new EndermiteEntity(EntityType.ENDERMITE, world);
					CompoundNBT endermiteTags = endermite.getPersistentData();
					//Scaling Health
					endermiteTags.putShort("scalinghealth:difficulty", (short) -1);

					float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
					float x = (float) (Math.cos(angle) * 3.15f);
					float z = (float) (Math.sin(angle) * 3.15f);
					int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
					ModifiableAttributeInstance attribute = endermite.getAttribute(Attributes.MOVEMENT_SPEED);
					attribute.setBaseValue(attribute.getBaseValue() * 1.5f);
					attribute = endermite.getAttribute(Attributes.FOLLOW_RANGE);
					attribute.setBaseValue(96f);
					attribute = endermite.getAttribute(Attributes.MAX_HEALTH);
					attribute.setBaseValue(4);
					endermite.setHealth((float) attribute.getValue());
					endermite.setPosition(x, y, z);
					endermite.setCustomName(new TranslationTextComponent("dragon.larva"));

					//Stream<PrioritizedGoal> runningGoals = endermite.goalSelector.getRunningGoals();
					ArrayList<Goal> toRemove = new ArrayList<>();

					endermite.targetSelector.goals.forEach(goal -> {
						if (goal.getGoal() instanceof NearestAttackableTargetGoal)
							toRemove.add(goal.getGoal());
					});

					for (Goal goal : toRemove) {
						endermite.targetSelector.removeGoal(goal);
					}
					endermite.targetSelector.addGoal(2, new NearestAttackableTargetGoal(endermite, PlayerEntity.class, false));

					endermite.experienceValue = 1;

					world.addEntity(endermite);
				}
			}
		}
	}

	private static void spawnShulkers(EnderDragonEntity dragon, World world) {
		if (ModConfig.COMMON.dragon.minions.difficultyToSpawn.get() <= 0)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		//Mobs Properties Randomness
		//tags.putBoolean("mpr:prevent_processing", true);

		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < ModConfig.COMMON.dragon.minions.difficultyToSpawn.get())
			return;

		int cooldown = tags.getInt("progressivebosses:shulkers_cooldown");
		if (cooldown > 0) {
			tags.putInt("progressivebosses:shulkers_cooldown", cooldown - 1);
		} else {
			int cooldownReduction = (int) (difficulty * ModConfig.COMMON.dragon.minions.cooldownReduction.get());
			cooldown = MathRandom.getInt(world.rand, ModConfig.COMMON.dragon.minions.minCooldown.get() - cooldownReduction, ModConfig.COMMON.dragon.minions.maxCooldown.get() - cooldownReduction);
			tags.putInt("progressivebosses:shulkers_cooldown", cooldown);

			ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, world);
			CompoundNBT shulkerTags = shulker.getPersistentData();

			//Scaling Health
			shulkerTags.putShort("scalinghealth:difficulty", (short) -1);

			float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
			float x = (float) (Math.cos(angle) * (MathRandom.getFloat(world.rand, 15f, 40f)));
			float z = (float) (Math.sin(angle) * (MathRandom.getFloat(world.rand, 15f, 40f)));
			float y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
			ModifiableAttributeInstance followRange = shulker.getAttribute(Attributes.FOLLOW_RANGE);
			followRange.setBaseValue(64f);
			shulker.setPosition(x, y, z);
			CompoundNBT compound = shulker.serializeNBT();
			compound.putByte("Color", (byte) 10);
			shulker.deserializeNBT(compound);
			shulker.setCustomName(new TranslationTextComponent("dragon.minion"));

			ArrayList<Goal> toRemove = new ArrayList<>();

			shulker.goalSelector.goals.forEach(goal -> {
				if (goal.getGoal() instanceof ShulkerEntity.AttackGoal)
					toRemove.add(goal.getGoal());
			});
			for (Goal goal : toRemove) {
				shulker.goalSelector.removeGoal(goal);
			}
			toRemove.clear();

			shulker.targetSelector.goals.forEach(goal -> {
				if (goal.getGoal() instanceof NearestAttackableTargetGoal)
					toRemove.add(goal.getGoal());
			});
			for (Goal goal : toRemove) {
				shulker.goalSelector.removeGoal(goal);
			}
			toRemove.clear();

			shulker.goalSelector.addGoal(1, new DragonMinionAttackGoal(shulker));

			shulker.targetSelector.addGoal(2, new DragonMinionAttackNearestGoal(shulker));

			shulker.deathLootTable = LootTables.EMPTY;
			shulker.experienceValue = 2;

			world.addEntity(shulker);
		}
	}
}
