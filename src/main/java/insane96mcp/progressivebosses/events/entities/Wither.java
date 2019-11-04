package insane96mcp.progressivebosses.events.entities;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.ai.WitherMinionHurtByTargetGoal;
import insane96mcp.progressivebosses.items.ModItems;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.utils.MathRandom;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Wither {
	public static void setStats(EntityJoinWorldEvent event) {
		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();

		CompoundNBT tags = wither.getPersistentData();
		boolean alreadyProcessed = tags.getBoolean("progressivebosses:processed");

		if (alreadyProcessed)
			return;

		tags.putBoolean("progressivebosses:processed", true);

		int radius = ModConfig.Wither.General.spawnRadiusPlayerCheck.get();
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);

		List<ServerPlayerEntity> players = event.getWorld().getEntitiesWithinAABB(ServerPlayerEntity.class, bb);
		if (players.size() == 0)
			return;

		float spawnedCount = 1;
		for (ServerPlayerEntity player : players) {
			CompoundNBT playerTags = player.getPersistentData();
			int c = playerTags.getInt("progressivebosses:spawned_withers");
			spawnedCount += c;
			if (c >= ModConfig.Wither.General.maxDifficulty.get())
				continue;
			playerTags.putInt("progressivebosses:spawned_withers", c + 1);
		}

		if (spawnedCount == 1)
			return;

		if (!ModConfig.Wither.General.sumSpawnedWitherDifficulty.get())
			spawnedCount /= players.size();

		setHealth(wither, spawnedCount);
		setArmor(wither, spawnedCount);
		setExperience(wither, spawnedCount);

		tags.putFloat("progressivebosses:difficulty", spawnedCount);

		int cooldown = MathRandom.getInt(wither.world.rand, ModConfig.Wither.Minions.minCooldown.get(), ModConfig.Wither.Minions.maxCooldown.get());
		tags.putInt("progressivebosses:skeletons_cooldown", cooldown);
	}

	private static void setExperience(WitherEntity wither, float difficulty) {
		int xp = 50 + (int) (50 * (ModConfig.Wither.Rewards.bonusExperience.get() * difficulty / 100f));
		wither.experienceValue = xp;
	}

	private static void setHealth(WitherEntity wither, float spawnedCount) {
		IAttributeInstance health = wither.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
		health.setBaseValue(health.getBaseValue() + (spawnedCount * ModConfig.Wither.Health.bonusPerDifficulty.get()));
		wither.setHealth(Math.max(1, (float) health.getBaseValue() - 200));
	}

	private static void setArmor(WitherEntity wither, float killedCount) {
		IAttributeInstance attribute = wither.getAttribute(SharedMonsterAttributes.ARMOR);
		double armor = killedCount * ModConfig.Wither.Armor.bonusPerDifficulty.get();
		if (armor > ModConfig.Wither.Armor.maximum.get())
			armor = ModConfig.Wither.Armor.maximum.get();
		attribute.setBaseValue(armor);
	}


	public static void update(LivingEvent.LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof WitherEntity))
			return;

		World world = event.getEntity().world;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT tags = wither.getPersistentData();

		if (wither.getHealth() < 0)
			return;

		if (wither.getInvulTime() > 0) {
			wither.bossInfo.setPercent(wither.getHealth() / wither.getMaxHealth());
		} else {
			spawnSkeletons(wither, world);
			heal(wither, tags);
		}
	}

	private static void heal(WitherEntity wither, CompoundNBT tags) {
		if (ModConfig.Wither.Health.maximumBonusRegen.get() == 0.0f)
			return;

        /*if (wither.ticksExisted % 2 != 0)
            return;
*/
		float difficulty = tags.getFloat("progressivebosses:difficulty");

		if (difficulty <= 0)
			return;

		double maxHeal = ModConfig.Wither.Health.maximumBonusRegen.get();
		double heal = difficulty * ModConfig.Wither.Health.bonusRegenPerSpawned.get();

		if (heal > maxHeal)
			heal = maxHeal;

		heal /= 20.0;

		double health = wither.getHealth();

		if (wither.getHealth() < wither.getMaxHealth() && wither.getHealth() > 0.0f)
			wither.setHealth((float) (health + heal));
	}

	private static void spawnSkeletons(WitherEntity wither, World world) {

		if (ModConfig.Wither.Minions.maxSpawned.get() == 0)
			return;

		int radius = 32;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<ServerPlayerEntity> players = world.getEntitiesWithinAABB(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		List<WitherSkeletonEntity> minions = world.getEntitiesWithinAABB(WitherSkeletonEntity.class, bb);
		int minionsCount = minions.size();

		if (minionsCount >= ModConfig.Wither.Minions.maxAround.get())
			return;

		CompoundNBT witherTags = wither.getPersistentData();

		//Mobs Properties Randomness
		witherTags.putBoolean("mobsrandomizzation:preventProcessing", true);

		float difficulty = witherTags.getFloat("progressivebosses:difficulty");

		int cooldown = witherTags.getInt("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown - 1);
		} else {
			int minCooldown = (int) (ModConfig.Wither.Minions.minCooldown.get() * (wither.getHealth() / wither.getMaxHealth()));
			if (minCooldown < ModConfig.Wither.Minions.minCooldown.get() * 0.25)
				minCooldown = (int) (ModConfig.Wither.Minions.minCooldown.get() * 0.25);
			cooldown = MathRandom.getInt(world.rand, minCooldown, ModConfig.Wither.Minions.maxCooldown.get());
			ProgressiveBosses.LOGGER.info(cooldown);
			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = ModConfig.Wither.Minions.difficultyToSpawn.get(); i <= difficulty; i++) {
				if (minionsCount >= ModConfig.Wither.Minions.maxAround.get() && ModConfig.Wither.Minions.maxAround.get() > 0)
					return;

				int spawn = i - ModConfig.Wither.Minions.difficultyToSpawn.get();

				//Stops spawning if max count has reached
				if (spawn / ModConfig.Wither.Minions.difficultyToSpawnOneMore.get() >= ModConfig.Wither.Minions.maxSpawned.get())
					break;

				if (spawn % ModConfig.Wither.Minions.difficultyToSpawnOneMore.get() == 0) {
					WitherSkeletonEntity witherSkeleton = new WitherSkeletonEntity(EntityType.WITHER_SKELETON, world);
					CompoundNBT skellyTags = witherSkeleton.getPersistentData();
					//Scaling Health
					skellyTags.putShort("scalinghealth:difficulty", (short) -1);

					int x = 0;
					int y = 0;
					int z = 0;

					boolean shouldSpawn = false;
					//Try to spawn the wither skeleton up to 10 times
					for (int t = 0; t < 10; t++) {
						x = (int) (wither.posX + (MathRandom.getInt(world.rand, -3, 3)));
						y = (int) (wither.posY - 3);
						z = (int) (wither.posZ + (MathRandom.getInt(world.rand, -3, 3)));

						for (; y < wither.posY + 4; y++) {
							if (canSpawn(witherSkeleton, new BlockPos(x, y, z), world)) {
								shouldSpawn = true;
								break;
							}
						}
						if (shouldSpawn)
							break;
					}
					if (!shouldSpawn)
						continue;

					IAttributeInstance minionHealth = witherSkeleton.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
					float health = MathRandom.getFloat(world.rand, ModConfig.Wither.Minions.minHealth.get(), ModConfig.Wither.Minions.maxHealth.get());
					minionHealth.setBaseValue(health);

					IAttributeInstance speedAttibute = witherSkeleton.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
					float maxSpeedMultiplier = 1.2f;
					float speedMultiplier = difficulty / 100f + 1f;
					if (speedMultiplier > maxSpeedMultiplier)
						speedMultiplier = maxSpeedMultiplier;
					speedAttibute.setBaseValue(speedAttibute.getBaseValue() * speedMultiplier);

					witherSkeleton.setPosition(x + 0.5f, y + 0.5f, z + 0.5f);
					witherSkeleton.setCustomName(new TranslationTextComponent("wither.minion"));
					witherSkeleton.deathLootTable = LootTables.EMPTY;
					witherSkeleton.experienceValue = 1;

					ListNBT minionsList = witherTags.getList("minions", Constants.NBT.TAG_COMPOUND);
					CompoundNBT uuid = new CompoundNBT();
					uuid.putUniqueId("uuid", witherSkeleton.getUniqueID());
					minionsList.add(uuid);
					witherTags.put("minions", minionsList);

					Stream<PrioritizedGoal> runningGoals = witherSkeleton.goalSelector.getRunningGoals();
					ArrayList<Goal> toRemove = new ArrayList<>();

					runningGoals.forEach(goal -> {
						if (goal.getGoal() instanceof FleeSunGoal)
							toRemove.add(goal);

						if (goal.getGoal() instanceof RestrictSunGoal)
							toRemove.add(goal);

						if (goal.getGoal() instanceof AvoidEntityGoal)
							toRemove.add(goal);
					});

					for (Goal goal : toRemove) {
						witherSkeleton.goalSelector.removeGoal(goal);
					}
					toRemove.clear();


					Stream<PrioritizedGoal> targetSelectors = witherSkeleton.targetSelector.getRunningGoals();

					targetSelectors.forEach(goal -> {
						if (goal.getGoal() instanceof NearestAttackableTargetGoal)
							toRemove.add(goal);
						if (goal.getGoal() instanceof HurtByTargetGoal)
							toRemove.add(goal);
					});
					for (Goal goal : toRemove) {
						witherSkeleton.targetSelector.removeGoal(goal);
					}
					toRemove.clear();

					witherSkeleton.targetSelector.addGoal(1, new WitherMinionHurtByTargetGoal(witherSkeleton, WitherEntity.class));
					witherSkeleton.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witherSkeleton, PlayerEntity.class, true));
					witherSkeleton.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witherSkeleton, IronGolemEntity.class, true));
					witherSkeleton.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witherSkeleton, MobEntity.class, 0, false, false, NOT_UNDEAD));

					world.addEntity(witherSkeleton);

					minionsCount++;
				}
			}
		}
	}

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity instanceof LivingEntity && livingEntity.getCreatureAttribute() != CreatureAttribute.UNDEAD && livingEntity.attackable();

	/*
	 * Check if the mob has space to spawn and if sits on solid ground
	 */
	private static boolean canSpawn(MobEntity mob, BlockPos pos, World world) {
		int height = (int) Math.ceil(mob.getHeight());
		boolean canSpawn = true;
		for (int i = 0; i < height; i++) {
			if (world.getBlockState(pos.up(i)).getMaterial().blocksMovement()) {
				canSpawn = false;
				break;
			}
		}
		if (!world.getBlockState(pos.down()).getMaterial().blocksMovement()) {
			canSpawn = false;
		}

		return canSpawn;
	}

	public static void onDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		World world = wither.world;

		CompoundNBT tags = wither.getPersistentData();
		ListNBT minionsList = tags.getList("minions", Constants.NBT.TAG_COMPOUND);

		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(new BlockPos(wither.getPosition().add(-128, -128, -128)), wither.getPosition().add(128, 128, 128));
		List<WitherSkeletonEntity> witherSkeletons = world.getEntitiesWithinAABB(WitherSkeletonEntity.class, axisAlignedBB);
		for (int i = 0; i < minionsList.size(); i++) {
			UUID uuid = minionsList.getCompound(i).getUniqueId("uuid");

			for (WitherSkeletonEntity skeleton : witherSkeletons) {
				if (skeleton.getUniqueID().equals(uuid)) {
					skeleton.addPotionEffect(new EffectInstance(Effects.INSTANT_HEALTH, 10000, 0, true, false));
					break;
				}
			}
		}
	}

	public static void setDrops(LivingDropsEvent event) {
		if (!(event.getEntityLiving() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntityLiving();

		CompoundNBT tags = wither.getPersistentData();
		float difficulty = tags.getFloat("progressivebosses:difficulty");

		double chance = ModConfig.Wither.Rewards.shardPerDifficulty.get() * difficulty;
		if (chance > ModConfig.Wither.Rewards.shardMaxChance.get())
			chance = ModConfig.Wither.Rewards.shardMaxChance.get();

		int tries = (int) (difficulty / ModConfig.Wither.Rewards.shardDivider.get()) + 1;
		if (tries > ModConfig.Wither.Rewards.shardMaxCount.get())
			tries = ModConfig.Wither.Rewards.shardMaxCount.get();
		int count = 0;
		for (int i = 0; i < tries; i++) {
			if (wither.world.rand.nextFloat() >= chance / 100f)
				continue;
			count++;
		}
		if (count == 0)
			return;

		ItemEntity shard = new ItemEntity(wither.world, wither.posX, wither.posY, wither.posZ, new ItemStack(ModItems.NETHER_STAR_SHARD, count));

		event.getDrops().add(shard);
	}
}
