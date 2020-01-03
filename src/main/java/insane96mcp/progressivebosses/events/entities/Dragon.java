package insane96mcp.progressivebosses.events.entities;

import insane96mcp.progressivebosses.events.entities.ai.DragonMinionAttackGoal;
import insane96mcp.progressivebosses.events.entities.ai.DragonMinionAttackNearestGoal;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.utils.MathRandom;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.ArrayList;
import java.util.List;

public class Dragon {
	public static void setStats(EntityJoinWorldEvent event) {
		if (event.getWorld().getDimension().getType() != DimensionType.THE_END)
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

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

		if (!ModConfig.Dragon.General.sumKilledDragonsDifficulty.get() && killedCount > 0)
			killedCount /= players.size();

		setHealth(dragon, killedCount);

		tags.putFloat("progressivebosses:difficulty", killedCount);
	}

	private static void setHealth(EnderDragonEntity dragon, float killedCount) {
		IAttributeInstance attribute = dragon.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
		attribute.setBaseValue(attribute.getBaseValue() + (killedCount * ModConfig.Dragon.Health.bonusPerDifficulty.get()));
		dragon.setHealth((float) attribute.getBaseValue());
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
				xp += (500 * ((ModConfig.Dragon.Rewards.bonusExperience.get() + 1) * difficulty / 100f) * (playersAround - playersFirstTime));

		} else {

			//Add bonus XP for the normal experience drop
			xp += (int) (500 * (ModConfig.Dragon.Rewards.bonusExperience.get() * difficulty / 100f));

			//Add XP per more than 1 player around
			if (playersAround > 1)
				xp += (500 * ((ModConfig.Dragon.Rewards.bonusExperience.get() + 1) * difficulty / 100f) * (playersAround - 1));
		}

		while (xp > 0) {
			int i = ExperienceOrbEntity.getXPSplit(xp);
			xp -= i;
			world.addEntity(new ExperienceOrbEntity(dragon.world, dragon.posX, dragon.posY, dragon.posZ, i));
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
			if (c >= ModConfig.Dragon.General.maxDifficulty.get())
				continue;
			playerTags.putInt("progressivebosses:killed_dragons", c + 1);
		}
	}


	public static void onPlayerDamage(LivingHurtEvent event) {

		if (!(event.getSource().getImmediateSource() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getSource().getImmediateSource();
		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		if (difficulty == 0)
			return;

		event.setAmount((float) (event.getAmount() * (1 + difficulty * (ModConfig.Dragon.Attack.bonusAttackDamage.get() / 100.0))));

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

		chargePlayer(dragon);
		spawnEndermites(dragon, world);
		spawnShulkers(dragon, world);
		heal(dragon, tags);
		dropEgg(dragon, world);
		dropMoreExperience(dragon, world);
	}

	private static void chargePlayer(EnderDragonEntity dragon) {
		CompoundNBT tags = dragon.getPersistentData();

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		double chance = (ModConfig.Dragon.Attack.chargePlayerBaseChance.get() / 100.0) / 24;
		chance *= difficulty;
		int crystalsAlive = dragon.getFightManager().getNumAliveCrystals() + 1;
		chance *= (1f / crystalsAlive);

		if (Math.random() < chance && dragon.getPhaseManager().getCurrentPhase().getType() == PhaseType.HOLDING_PATTERN) {
			AxisAlignedBB axisAlignedBB = new AxisAlignedBB(-128, -128, -128, 128, 128, 128);
			ServerPlayerEntity player = dragon.world.getClosestEntityWithinAABB(ServerPlayerEntity.class, new EntityPredicate().setDistance(128.0), dragon, dragon.posX, dragon.posY + (double) dragon.getEyeHeight(), dragon.posZ, axisAlignedBB);

			if (player != null) {
				dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
				(dragon.getPhaseManager().getPhase(PhaseType.CHARGING_PLAYER)).setTarget(new Vec3d(player.posX, player.posY, player.posZ));
			}
		}
	}

	private static void heal(EnderDragonEntity dragon, CompoundNBT tags) {
		if (ModConfig.Dragon.Health.maximumBonusRegen.get() == 0.0f)
			return;

		if (dragon.ticksExisted % 20 != 0)
			return;

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		if (difficulty == 0)
			return;

		double maxHeal = ModConfig.Dragon.Health.maximumBonusRegen.get();
		double heal = difficulty * ModConfig.Dragon.Health.bonusRegenPerSpawned.get();

		if (heal > maxHeal)
			heal = maxHeal;

		float health = dragon.getHealth();

		if (dragon.getHealth() < dragon.getMaxHealth() && dragon.getHealth() > 0.0f)
			dragon.setHealth((float) (health + heal));
	}

	private static void spawnEndermites(EnderDragonEntity dragon, World world) {
		if (ModConfig.Dragon.Larvae.maxSpawned.get() == 0)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		//Mobs Properties Randomness
		tags.putBoolean("mobspropertiesrandomness:checked", true);

		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < ModConfig.Dragon.Larvae.difficultyToSpawnOneMore.get())
			return;

		int cooldown = tags.getInt("progressivebosses:endermites_cooldown");
		if (cooldown > 0) {
			tags.putInt("progressivebosses:endermites_cooldown", cooldown - 1);
		} else {
			int cooldownReduction = (int) (difficulty * ModConfig.Dragon.Larvae.cooldownReduction.get());
			cooldown = MathRandom.getInt(world.rand, ModConfig.Dragon.Larvae.minCooldown.get() - cooldownReduction, ModConfig.Dragon.Larvae.maxCooldown.get() - cooldownReduction);
			tags.putInt("progressivebosses:endermites_cooldown", cooldown);
			for (int i = 1; i <= difficulty; i++) {
				if (i / ModConfig.Dragon.Larvae.difficultyToSpawnOneMore.get() > ModConfig.Dragon.Larvae.maxSpawned.get())
					break;

				if (i % ModConfig.Dragon.Larvae.difficultyToSpawnOneMore.get() == 0) {
					EndermiteEntity endermite = new EndermiteEntity(EntityType.ENDERMITE, world);
					CompoundNBT endermiteTags = endermite.getPersistentData();
					//Scaling Health
					endermiteTags.putShort("scalinghealth:difficulty", (short) -1);

					float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
					float x = (float) (Math.cos(angle) * 3.15f);
					float z = (float) (Math.sin(angle) * 3.15f);
					int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
					IAttributeInstance attribute = endermite.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					attribute.setBaseValue(attribute.getBaseValue() * 1.5f);
					attribute = endermite.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
					attribute.setBaseValue(96f);
					attribute = endermite.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
					attribute.setBaseValue(4);
					endermite.setHealth((float) attribute.getBaseValue());
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
		if (ModConfig.Dragon.Minion.difficultyToSpawn.get() <= 0)
			return;

		CompoundNBT tags = dragon.getPersistentData();

		//Mobs Properties Randomness
		tags.putBoolean("mpr:prevent_processing", true);

		float difficulty = tags.getFloat("progressivebosses:difficulty");
		if (difficulty < ModConfig.Dragon.Minion.difficultyToSpawn.get())
			return;

		int cooldown = tags.getInt("progressivebosses:shulkers_cooldown");
		if (cooldown > 0) {
			tags.putInt("progressivebosses:shulkers_cooldown", cooldown - 1);
		} else {
			int cooldownReduction = (int) (difficulty * ModConfig.Dragon.Minion.cooldownReduction.get());
			cooldown = MathRandom.getInt(world.rand, ModConfig.Dragon.Minion.minCooldown.get() - cooldownReduction, ModConfig.Dragon.Minion.maxCooldown.get() - cooldownReduction);
			tags.putInt("progressivebosses:shulkers_cooldown", cooldown);

			ShulkerEntity shulker = new ShulkerEntity(EntityType.SHULKER, world);
			CompoundNBT shulkerTags = shulker.getPersistentData();

			//Scaling Health
			shulkerTags.putShort("scalinghealth:difficulty", (short) -1);

			float angle = world.rand.nextFloat() * (float) Math.PI * 2f;
			float x = (float) (Math.cos(angle) * (MathRandom.getFloat(world.rand, 15f, 40f)));
			float z = (float) (Math.sin(angle) * (MathRandom.getFloat(world.rand, 15f, 40f)));
			float y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 255, z)).getY();
			IAttributeInstance followRange = shulker.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
			followRange.setBaseValue(64f);
			shulker.setPosition(x, y, z);
			CompoundNBT compound = shulker.serializeNBT();
			compound.putByte("Color", (byte) 15);
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
