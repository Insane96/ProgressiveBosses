package insane96mcp.progressivebosses.events.entities;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.function.Predicate;

public class Wither {
	public static void setStats(EntityJoinWorldEvent event) {
		/*if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();

		CompoundNBT tags = wither.getPersistentData();
		boolean alreadyProcessed = tags.getBoolean("progressivebosses:processed");

		if (alreadyProcessed)
			return;

		tags.putBoolean("progressivebosses:processed", true);

		int radius = Config.COMMON.wither.general.spawnRadiusPlayerCheck.get();
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
			if (c >= Config.COMMON.wither.general.maxDifficulty.get())
				continue;
			playerTags.putInt("progressivebosses:spawned_withers", c + 1);
		}

		if (spawnedCount == 1)
			return;

		if (!Config.COMMON.wither.general.sumSpawnedWitherDifficulty.get())
			spawnedCount /= players.size();

		setArmor(wither, spawnedCount);
		setExperience(wither, spawnedCount);

		tags.putFloat("progressivebosses:difficulty", spawnedCount);

		int cooldown = MathRandom.getInt(wither.world.rand, Config.COMMON.wither.minions.minCooldown.get(), Config.COMMON.wither.minions.maxCooldown.get());
		tags.putInt("progressivebosses:skeletons_cooldown", cooldown);*/
	}

	private static void setExperience(WitherEntity wither, float difficulty) {
		//wither.experienceValue = 50 + (int) (50 * (Config.COMMON.wither.rewards.bonusExperience.get() * difficulty / 100f));
	}

	private static void setArmor(WitherEntity wither, float killedCount) {
		//Armor only when < half health
		/*ModifiableAttributeInstance attribute = wither.getAttribute(Attributes.ARMOR);
		double armor = killedCount * Config.COMMON.wither.armor.bonusPerDifficulty.get();
		if (armor > Config.COMMON.wither.armor.maximum.get())
			armor = Config.COMMON.wither.armor.maximum.get();

		AttributeModifier modifier = new AttributeModifier(ProgressiveBosses.RESOURCE_PREFIX + "wither_bonus_armor", armor, AttributeModifier.Operation.ADDITION);

		attribute.applyPersistentModifier(modifier);*/
	}

	public static void update(LivingEvent.LivingUpdateEvent event) {
		/*if (!(event.getEntity() instanceof WitherEntity))
			return;

		World world = event.getEntity().world;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT tags = wither.getPersistentData();

		if (wither.getHealth() < 0)
			return;

		if (wither.getInvulTime() > 0) {
		} else {
			spawnSkeletons(wither, world);
		}*/
	}

	private static void spawnSkeletons(WitherEntity wither, World world) {

		/*if (Config.COMMON.wither.minions.maxSpawned.get() == 0)
			return;

		if (wither.getHealth() <= 0f)
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

		if (minionsCount >= Config.COMMON.wither.minions.maxAround.get())
			return;

		CompoundNBT witherTags = wither.getPersistentData();

		//Mobs Properties Randomness
		//witherTags.putBoolean("mobsrandomizzation:preventProcessing", true);

		float difficulty = witherTags.getFloat("progressivebosses:difficulty");

		int cooldown = witherTags.getInt("progressivebosses:skeletons_cooldown");
		if (cooldown > 0) {
			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown - 1);
		} else {
			int minCooldown = (int) (Config.COMMON.wither.minions.minCooldown.get() * (wither.getHealth() / wither.getMaxHealth()));
			if (minCooldown < Config.COMMON.wither.minions.minCooldown.get() * 0.25)
				minCooldown = (int) (Config.COMMON.wither.minions.minCooldown.get() * 0.25);
			cooldown = MathRandom.getInt(world.rand, minCooldown, Config.COMMON.wither.minions.maxCooldown.get());

			witherTags.putInt("progressivebosses:skeletons_cooldown", cooldown);
			for (int i = Config.COMMON.wither.minions.difficultyToSpawn.get(); i <= difficulty; i++) {
				if (minionsCount >= Config.COMMON.wither.minions.maxAround.get() && Config.COMMON.wither.minions.maxAround.get() > 0)
					return;

				int spawn = i - Config.COMMON.wither.minions.difficultyToSpawn.get();

				//Stops spawning if max count has reached
				if (spawn / Config.COMMON.wither.minions.difficultyToSpawnOneMore.get() >= Config.COMMON.wither.minions.maxSpawned.get())
					break;

				if (spawn % Config.COMMON.wither.minions.difficultyToSpawnOneMore.get() == 0) {
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
					float health = MathRandom.getFloat(world.rand, Config.COMMON.wither.minions.minHealth.get(), Config.COMMON.wither.minions.maxHealth.get());
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
		}*/
	}

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity != null && livingEntity.getCreatureAttribute() != CreatureAttribute.UNDEAD && livingEntity.attackable();

	/*
	 * Check if the mob has space to spawn and if sits on solid ground
	 */
	/*private static boolean canSpawn(MobEntity mob, BlockPos pos, World world) {
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
	}*/

	public static void onDeath(LivingDeathEvent event) {
		/*if (!(event.getEntity() instanceof WitherEntity))
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
		}*/
	}

	public static void setDrops(LivingDropsEvent event) {
		/*if (!(event.getEntityLiving() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntityLiving();

		CompoundNBT tags = wither.getPersistentData();
		float difficulty = tags.getFloat("progressivebosses:difficulty");

		double chance = Config.COMMON.wither.rewards.shardPerDifficulty.get() * difficulty;
		if (chance > Config.COMMON.wither.rewards.shardMaxChance.get())
			chance = Config.COMMON.wither.rewards.shardMaxChance.get();

		int tries = (int) (difficulty / Config.COMMON.wither.rewards.shardDivider.get()) + 1;
		if (tries > Config.COMMON.wither.rewards.shardMaxCount.get())
			tries = Config.COMMON.wither.rewards.shardMaxCount.get();
		int count = 0;
		for (int i = 0; i < tries; i++) {
			if (wither.world.rand.nextFloat() >= chance / 100f)
				continue;
			count++;
		}
		if (count == 0)
			return;

		ItemEntity shard = new ItemEntity(wither.world, wither.getPositionVec().getX(), wither.getPositionVec().getY(), wither.getPositionVec().getZ(), new ItemStack(ModItems.NETHER_STAR_SHARD.get(), count));

		event.getDrops().add(shard);*/
	}
}
