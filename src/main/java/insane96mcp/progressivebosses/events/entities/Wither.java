package insane96mcp.progressivebosses.events.entities;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.ai.WitherMinionHurtByTargetGoal;
import insane96mcp.progressivebosses.items.ModItems;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.utils.MathRandom;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTables;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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

		int radius = ModConfig.COMMON.wither.general.spawnRadiusPlayerCheck.get();
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
			if (c >= ModConfig.COMMON.wither.general.maxDifficulty.get())
				continue;
			playerTags.putInt("progressivebosses:spawned_withers", c + 1);
		}

		if (spawnedCount == 1)
			return;

		if (!ModConfig.COMMON.wither.general.sumSpawnedWitherDifficulty.get())
			spawnedCount /= players.size();

		setHealth(wither, spawnedCount);
		setArmor(wither, spawnedCount);
		setExperience(wither, spawnedCount);

		tags.putFloat("progressivebosses:difficulty", spawnedCount);

		int cooldown = MathRandom.getInt(wither.world.rand, ModConfig.COMMON.wither.minions.minCooldown.get(), ModConfig.COMMON.wither.minions.maxCooldown.get());
		tags.putInt("progressivebosses:skeletons_cooldown", cooldown);
	}

	private static void setExperience(WitherEntity wither, float difficulty) {
		wither.experienceValue = 50 + (int) (50 * (ModConfig.COMMON.wither.rewards.bonusExperience.get() * difficulty / 100f));
	}

	private static void setHealth(WitherEntity wither, float spawnedCount) {
		ModifiableAttributeInstance health = wither.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier modifier = new AttributeModifier(ProgressiveBosses.RESOURCE_PREFIX + "wither_bonus_health", spawnedCount * ModConfig.COMMON.wither.health.bonusPerDifficulty.get(), AttributeModifier.Operation.ADDITION);
		health.applyPersistentModifier(modifier);

		boolean hasInvulTicks = wither.getInvulTime() > 0;

		if (hasInvulTicks)
			wither.setHealth(Math.max(1, (float) health.getValue() - 200));
		else
			wither.setHealth((float) health.getValue());
	}

	private static void setArmor(WitherEntity wither, float killedCount) {
		ModifiableAttributeInstance attribute = wither.getAttribute(Attributes.ARMOR);
		double armor = killedCount * ModConfig.COMMON.wither.armor.bonusPerDifficulty.get();
		if (armor > ModConfig.COMMON.wither.armor.maximum.get())
			armor = ModConfig.COMMON.wither.armor.maximum.get();

		AttributeModifier modifier = new AttributeModifier(ProgressiveBosses.RESOURCE_PREFIX + "wither_bonus_armor", armor, AttributeModifier.Operation.ADDITION);

		attribute.applyPersistentModifier(modifier);
	}

	public static void update(LivingEvent.LivingUpdateEvent event) {
		if (!(event.getEntity() instanceof WitherEntity))
			return;

		World world = event.getEntity().world;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT tags = wither.getPersistentData();

		if (wither.getHealth() < 0)
			return;

		if (wither.getInvulTime() == 1){
			explode(wither);
		}

		if (wither.getInvulTime() > 0) {
			wither.bossInfo.setPercent(wither.getHealth() / wither.getMaxHealth());
		} else {
			spawnSkeletons(wither, world);
			heal(wither, tags);
		}
	}

	private static void explode(WitherEntity wither) {

		if (ModConfig.COMMON.wither.misc.explosionCausesFireAtDifficulty.get() == -1 &&
				ModConfig.COMMON.wither.misc.explosionPowerBonus.get() == 0d)
			return;

		float difficulty = wither.getPersistentData().getFloat(ProgressiveBosses.RESOURCE_PREFIX + "difficulty");

		if (difficulty <= 0)
			return;

		float explosionPower = (float) (7.0f + (ModConfig.COMMON.wither.misc.explosionPowerBonus.get() * difficulty));

		if (explosionPower > 13f)
			explosionPower = 13f;

		boolean fireExplosion = difficulty >= ModConfig.COMMON.wither.misc.explosionCausesFireAtDifficulty.get();

		if (ModConfig.COMMON.wither.misc.explosionCausesFireAtDifficulty.get() == -1)
			fireExplosion = false;

		Explosion.Mode explosion$mode = net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(wither.world, wither) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
		wither.world.createExplosion(wither, wither.getPosX(), wither.getPosYEye(), wither.getPosZ(), explosionPower, fireExplosion, explosion$mode);
	}

	private static void heal(WitherEntity wither, CompoundNBT tags) {
		if (ModConfig.COMMON.wither.health.maximumBonusRegen.get() == 0.0f)
			return;

		float difficulty = tags.getFloat("progressivebosses:difficulty");

		if (difficulty <= 0)
			return;

		double maxHeal = ModConfig.COMMON.wither.health.maximumBonusRegen.get();
		double heal = difficulty * ModConfig.COMMON.wither.health.bonusRegenPerSpawned.get();

		if (heal > maxHeal)
			heal = maxHeal;

		heal /= 20.0;

		double health = wither.getHealth();

		if (wither.getHealth() < wither.getMaxHealth() && wither.getHealth() > 0.0f)
			wither.setHealth((float) (health + heal));
	}

	private static void spawnSkeletons(WitherEntity wither, World world) {

		if (ModConfig.COMMON.wither.minions.maxSpawned.get() == 0)
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

		if (minionsCount >= ModConfig.COMMON.wither.minions.maxAround.get())
			return;

		CompoundNBT witherTags = wither.getPersistentData();

		//Mobs Properties Randomness
		witherTags.putBoolean("mobsrandomizzation:preventProcessing", true);

		float difficulty = witherTags.getFloat("progressivebosses:difficulty");

		int cooldown = witherTags.getInt("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown - 1);
		} else {
			int minCooldown = (int) (ModConfig.COMMON.wither.minions.minCooldown.get() * (wither.getHealth() / wither.getMaxHealth()));
			if (minCooldown < ModConfig.COMMON.wither.minions.minCooldown.get() * 0.25)
				minCooldown = (int) (ModConfig.COMMON.wither.minions.minCooldown.get() * 0.25);
			cooldown = MathRandom.getInt(world.rand, minCooldown, ModConfig.COMMON.wither.minions.maxCooldown.get());

			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = ModConfig.COMMON.wither.minions.difficultyToSpawn.get(); i <= difficulty; i++) {
				if (minionsCount >= ModConfig.COMMON.wither.minions.maxAround.get() && ModConfig.COMMON.wither.minions.maxAround.get() > 0)
					return;

				int spawn = i - ModConfig.COMMON.wither.minions.difficultyToSpawn.get();

				//Stops spawning if max count has reached
				if (spawn / ModConfig.COMMON.wither.minions.difficultyToSpawnOneMore.get() >= ModConfig.COMMON.wither.minions.maxSpawned.get())
					break;

				if (spawn % ModConfig.COMMON.wither.minions.difficultyToSpawnOneMore.get() == 0) {
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
						x = (int) (wither.getPositionVec().getX() + (MathRandom.getInt(world.rand, -3, 3)));
						y = (int) (wither.getPositionVec().getY() - 3);
						z = (int) (wither.getPositionVec().getZ() + (MathRandom.getInt(world.rand, -3, 3)));

						for (; y < wither.getPositionVec().getY() + 4; y++) {
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

					ModifiableAttributeInstance minionHealth = witherSkeleton.getAttribute(Attributes.MAX_HEALTH);
					float health = MathRandom.getFloat(world.rand, ModConfig.COMMON.wither.minions.minHealth.get(), ModConfig.COMMON.wither.minions.maxHealth.get());
					minionHealth.setBaseValue(health);

					ModifiableAttributeInstance speedAttibute = witherSkeleton.getAttribute(Attributes.MOVEMENT_SPEED);
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

					ArrayList<Goal> toRemove = new ArrayList<>();
					witherSkeleton.goalSelector.goals.forEach(goal -> {
						if (goal.getGoal() instanceof FleeSunGoal)
							toRemove.add(goal.getGoal());

						if (goal.getGoal() instanceof RestrictSunGoal)
							toRemove.add(goal.getGoal());

						if (goal.getGoal() instanceof AvoidEntityGoal)
							toRemove.add(goal.getGoal());
					});

					for (Goal goal : toRemove) {
						witherSkeleton.goalSelector.removeGoal(goal);
					}
					toRemove.clear();

					witherSkeleton.targetSelector.goals.forEach(goal -> {
						if (goal.getGoal() instanceof NearestAttackableTargetGoal)
							toRemove.add(goal.getGoal());
						if (goal.getGoal() instanceof HurtByTargetGoal)
							toRemove.add(goal.getGoal());
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

		double chance = ModConfig.COMMON.wither.rewards.shardPerDifficulty.get() * difficulty;
		if (chance > ModConfig.COMMON.wither.rewards.shardMaxChance.get())
			chance = ModConfig.COMMON.wither.rewards.shardMaxChance.get();

		int tries = (int) (difficulty / ModConfig.COMMON.wither.rewards.shardDivider.get()) + 1;
		if (tries > ModConfig.COMMON.wither.rewards.shardMaxCount.get())
			tries = ModConfig.COMMON.wither.rewards.shardMaxCount.get();
		int count = 0;
		for (int i = 0; i < tries; i++) {
			if (wither.world.rand.nextFloat() >= chance / 100f)
				continue;
			count++;
		}
		if (count == 0)
			return;

		ItemEntity shard = new ItemEntity(wither.world, wither.getPositionVec().getX(), wither.getPositionVec().getY(), wither.getPositionVec().getZ(), new ItemStack(ModItems.NETHER_STAR_SHARD, count));

		event.getDrops().add(shard);
	}
}
