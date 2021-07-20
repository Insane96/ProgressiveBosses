package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinionEntity;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Label(name = "Minions", description = "Wither will spawn deadly (and tall) Minions")
public class MinionFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> minionAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> bonusMinionEveryDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxSpawnedConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxAroundConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Double> cooldownMultiplierBelowHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusSpeedPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> killMinionOnWitherDeathConfig;
	//Equipment
	private final ForgeConfigSpec.ConfigValue<Boolean> hasSwordConfig;
	private final ForgeConfigSpec.ConfigValue<Double> preHalfHealthBowChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> halfHealthBowChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> powerSharpnessChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> punchKnockbackChanceConfig;

	public int minionAtDifficulty = 1;
	public int bonusMinionEveryDifficulty = 2;
	public int maxSpawned = 6;
	public int maxAround = 20;
	public int minCooldown = 240;
	public int maxCooldown = 480;
	public double cooldownMultiplierBelowHalfHealth = 0.6d;
	public double bonusSpeedPerDifficulty = 0.012d;
	public boolean killMinionOnWitherDeath = true;
	//Equipment
	public boolean hasSword = true;
	public double preHalfHealthBowChance = 0.6d;
	public double halfHealthBowChance = 0.08d;
	public double powerSharpnessChance = 0.2d;
	public double punchKnockbackChance = 0.15d;

	public MinionFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		minionAtDifficultyConfig = Config.builder
				.comment("At which difficulty the Wither starts spawning Minions")
				.defineInRange("Minion at Difficulty", minionAtDifficulty, 0, Integer.MAX_VALUE);
		bonusMinionEveryDifficultyConfig = Config.builder
				.comment("As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion")
				.defineInRange("Bonus Minion Every Difficulty", bonusMinionEveryDifficulty, 0, Integer.MAX_VALUE);
		maxSpawnedConfig = Config.builder
				.comment("Maximum Minions spawned by the Wither")
				.defineInRange("Max Minions Spawned", maxSpawned, 0, Integer.MAX_VALUE);
		maxAroundConfig = Config.builder
				.comment("Maximum amount of Minions that can be around the Wither in a 16 block radius. After this number is reached the Wither will stop spawning minions. Set to 0 to disable this check")
				.defineInRange("Max Minions Around", maxAround, 0, Integer.MAX_VALUE);
		minCooldownConfig = Config.builder
				.comment("Minimum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Minimum Cooldown", minCooldown, 0, Integer.MAX_VALUE);
		maxCooldownConfig = Config.builder
				.comment("Maximum ticks (20 ticks = 1 seconds) after Minions can spwan.")
				.defineInRange("Maximum Cooldown", maxCooldown, 0, Integer.MAX_VALUE);
		cooldownMultiplierBelowHalfHealthConfig = Config.builder
				.comment("Min and Max cooldowns are multiplied by this value when the Wither drops below half health. Set to 1 to not change the cooldown when the wither's health drops below half.")
				.defineInRange("Cooldown Multiplier Below Half Health", cooldownMultiplierBelowHalfHealth, 0d, Double.MAX_VALUE);
		bonusSpeedPerDifficultyConfig = Config.builder
				.comment("Percentage bonus speed per difficulty. (0.01 means 1%)")
				.defineInRange("Bonus Movement Speed Per Difficulty", bonusSpeedPerDifficulty, 0d, Double.MAX_VALUE);
		killMinionOnWitherDeathConfig = Config.builder
				.comment("Wither Minions will die when the Wither that spawned them dies.")
				.define("Kill Minions on Wither Death", killMinionOnWitherDeath);

		Config.builder.push("Equipment");
		hasSwordConfig = Config.builder
				.comment("Wither Minions will spawn with a Stone Sword")
				.define("Has Sword", hasSword);
		preHalfHealthBowChanceConfig = Config.builder
				.comment("Chance for the Wither Minion to spawn with a bow when Wither's above Half Health")
				.defineInRange("Bow Chance Over Half Health", preHalfHealthBowChance, 0d, 1d);
		halfHealthBowChanceConfig = Config.builder
				.comment("Chance for the Wither Minion to spawn with a bow when Wither's below Half Health")
				.defineInRange("Bow Chance Below Half Health", halfHealthBowChance, 0d, 1d);
		powerSharpnessChanceConfig = Config.builder
				.comment("Chance (per difficulty) for the Wither Minion Sword / Bow to be enchanted with Sharpness / Power. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.")
				.defineInRange("Power / Sharpness Chance", powerSharpnessChance, 0d, Double.MAX_VALUE);
		punchKnockbackChanceConfig = Config.builder
				.comment("Chance (per difficulty) for the Wither Minion Sword / Bow to be enchanted with Knockback / Punch. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.")
				.defineInRange("Punch / Knockback Chance", punchKnockbackChance, 0d, Double.MAX_VALUE);
		Config.builder.pop();

		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.minionAtDifficulty = this.minionAtDifficultyConfig.get();
		this.bonusMinionEveryDifficulty = this.bonusMinionEveryDifficultyConfig.get();
		this.maxSpawned = this.maxSpawnedConfig.get();
		this.maxAround = this.maxAroundConfig.get();
		this.minCooldown = this.minCooldownConfig.get();
		this.maxCooldown = this.maxCooldownConfig.get();
		this.cooldownMultiplierBelowHalfHealth = this.cooldownMultiplierBelowHalfHealthConfig.get();
		this.bonusSpeedPerDifficulty = this.bonusSpeedPerDifficultyConfig.get();
		this.killMinionOnWitherDeath = this.killMinionOnWitherDeathConfig.get();
		//Equipment
		this.hasSword = this.hasSwordConfig.get();
		this.preHalfHealthBowChance = this.preHalfHealthBowChanceConfig.get();
		this.halfHealthBowChance = this.halfHealthBowChanceConfig.get();
		this.powerSharpnessChance = this.powerSharpnessChanceConfig.get();
		this.punchKnockbackChance = this.punchKnockbackChanceConfig.get();
	}

	@SubscribeEvent
	public void onWitherSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();

		CompoundNBT witherTags = wither.getPersistentData();

		int cooldown = (int) (RandomHelper.getInt(wither.world.rand, this.minCooldown, this.maxCooldown) * this.cooldownMultiplierBelowHalfHealth);
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void onSkellySpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherMinionEntity))
			return;

		WitherMinionEntity witherMinion = (WitherMinionEntity) event.getEntity();

		CompoundNBT tags = witherMinion.getPersistentData();
		if (!tags.contains(Strings.Tags.WITHER_MINION))
			return;

		setMinionAI(witherMinion);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		World world = event.getEntity().world;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT witherTags = wither.getPersistentData();

		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < this.minionAtDifficulty)
			return;

		if (wither.getHealth() <= 0)
			return;

		if (wither.getInvulTime() > 0)
			return;

		int cooldown = witherTags.getInt(Strings.Tags.WITHER_MINION_COOLDOWN);
		if (cooldown > 0) {
			witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in a radius from the wither, don't spawn minions
		int radius = 32;
		BlockPos pos1 = wither.getPosition().add(-radius, -radius, -radius);
		BlockPos pos2 = wither.getPosition().add(radius, radius, radius);
		AxisAlignedBB bb = new AxisAlignedBB(pos1, pos2);
		List<ServerPlayerEntity> players = world.getLoadedEntitiesWithinAABB(ServerPlayerEntity.class, bb);

		if (players.isEmpty())
			return;

		List<WitherMinionEntity> minionsInAABB = world.getLoadedEntitiesWithinAABB(WitherMinionEntity.class, wither.getBoundingBox().grow(16));
		int minionsCountInAABB = minionsInAABB.size();

		if (minionsCountInAABB >= this.maxAround)
			return;

		int minCooldown = this.minCooldown;
		int maxCooldown = this.maxCooldown;

		cooldown = RandomHelper.getInt(world.rand, minCooldown, maxCooldown);
		if (wither.isCharged())
			cooldown *= this.cooldownMultiplierBelowHalfHealth;
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown - 1);

		int minionSpawnedCount = 0;
		for (int i = this.minionAtDifficulty; i <= difficulty; i += this.bonusMinionEveryDifficulty) {

			int x = 0, y = 0, z = 0;
			//Tries to spawn the Minion up to 5 times
			for (int t = 0; t < 5; t++) {
				x = (int) (wither.getPosX() + (RandomHelper.getInt(world.rand, -3, 3)));
				y = (int) (wither.getPosY() + 3);
				z = (int) (wither.getPositionVec().getZ() + (RandomHelper.getInt(world.rand, -3, 3)));

				y = getYSpawn(EntityType.WITHER_SKELETON, new BlockPos(x, y, z), world, 8);
				if (y != -1)
					break;
			}
			if (y <= 0)
				continue;

			WitherMinionEntity witherMinion = summonMinion(world, new Vector3d(x + 0.5, y + 0.5, z + 0.5), difficulty, wither.isCharged());

			//No need since EntityJoinWorldEvent is triggered
			//setMinionAI(witherMinion);

			ListNBT minionsList = witherTags.getList(Strings.Tags.MINIONS, Constants.NBT.TAG_COMPOUND);
			CompoundNBT uuid = new CompoundNBT();
			uuid.putUniqueId("uuid", witherMinion.getUniqueID());
			minionsList.add(uuid);
			witherTags.put(Strings.Tags.MINIONS, minionsList);

			minionSpawnedCount++;
			if (minionSpawnedCount >= this.maxSpawned)
				break;

			minionsCountInAABB++;
			if (minionsCountInAABB >= this.maxAround)
				break;
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!this.killMinionOnWitherDeath)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		ServerWorld world = (ServerWorld) wither.world;

		CompoundNBT tags = wither.getPersistentData();
		ListNBT minionsList = tags.getList(Strings.Tags.MINIONS, Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < minionsList.size(); i++) {
			UUID uuid = minionsList.getCompound(i).getUniqueId("uuid");
			WitherMinionEntity witherMinionEntity = (WitherMinionEntity) world.getEntityByUuid(uuid);
			if (witherMinionEntity == null)
				continue;
			witherMinionEntity.addPotionEffect(new EffectInstance(Effects.INSTANT_HEALTH, 10000, 0, false, false));
		}
	}

	private void setEquipment(WitherMinionEntity witherMinionEntity, float difficulty, boolean isCharged) {
		witherMinionEntity.setDropChance(EquipmentSlotType.MAINHAND, Float.MIN_VALUE);

		int powerSharpnessLevel = (int) (this.powerSharpnessChance * difficulty);
		if (RandomHelper.getDouble(witherMinionEntity.world.getRandom(), 0d, 1d) < (this.powerSharpnessChance * difficulty) - powerSharpnessLevel)
			powerSharpnessLevel++;

		int punchKnockbackLevel = (int) (this.punchKnockbackChance * difficulty);
		if (RandomHelper.getDouble(witherMinionEntity.world.getRandom(), 0d, 1d) < (this.punchKnockbackChance * difficulty) - punchKnockbackLevel)
			punchKnockbackLevel++;

		ItemStack sword = new ItemStack(Items.STONE_SWORD);
		if (powerSharpnessLevel > 0)
			sword.addEnchantment(Enchantments.SHARPNESS, powerSharpnessLevel);
		if (punchKnockbackLevel > 0)
			sword.addEnchantment(Enchantments.KNOCKBACK, punchKnockbackLevel);
		if (this.hasSword)
			witherMinionEntity.setItemStackToSlot(EquipmentSlotType.MAINHAND, sword);

		ItemStack bow = new ItemStack(Items.BOW);
		if (powerSharpnessLevel > 0)
			bow.addEnchantment(Enchantments.POWER, powerSharpnessLevel);
		if (punchKnockbackLevel > 0)
			bow.addEnchantment(Enchantments.PUNCH, punchKnockbackLevel);
		if (isCharged) {
			if (RandomHelper.getDouble(witherMinionEntity.world.getRandom(), 0d, 1d) < this.halfHealthBowChance) {
				witherMinionEntity.setItemStackToSlot(EquipmentSlotType.MAINHAND, bow);
			}
		}
		else {
			if (RandomHelper.getDouble(witherMinionEntity.world.getRandom(), 0d, 1d) < this.preHalfHealthBowChance) {
				witherMinionEntity.setItemStackToSlot(EquipmentSlotType.MAINHAND, bow);
			}
		}
	}

	/**
	 * Returns -1 when no spawn spots are found, otherwise the Y coord
	 * @param pos
	 * @param world
	 * @param minRelativeY
	 * @return
	 */
	private static int getYSpawn(EntityType entityType, BlockPos pos, World world, int minRelativeY) {
		int height = (int) Math.ceil(entityType.getHeight());
		int fittingYPos = -1;
		for (int y = pos.getY(); y > pos.getY() - minRelativeY; y--) {
			boolean viable = true;
			BlockPos p = new BlockPos(pos.getX(), y, pos.getZ());
			for (int i = 0; i < height; i++) {
				if (world.getBlockState(p.up(i)).getMaterial().blocksMovement()) {
					viable = false;
					break;
				}
			}
			if (!viable)
				continue;
			fittingYPos = y;
			if (!world.getBlockState(p.down()).getMaterial().blocksMovement())
				continue;
			return y;
		}
		return fittingYPos;
	}

	private static final Predicate<LivingEntity> NOT_UNDEAD = livingEntity -> livingEntity != null && livingEntity.getCreatureAttribute() != CreatureAttribute.UNDEAD && livingEntity.attackable();

	private static void setMinionAI(WitherMinionEntity witherMinionEntity) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		witherMinionEntity.goalSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof FleeSunGoal)
				toRemove.add(goal.getGoal());

			if (goal.getGoal() instanceof RestrictSunGoal)
				toRemove.add(goal.getGoal());

			if (goal.getGoal() instanceof AvoidEntityGoal)
				toRemove.add(goal.getGoal());
		});

		for (Goal goal : toRemove) {
			witherMinionEntity.goalSelector.removeGoal(goal);
		}
		toRemove.clear();

		witherMinionEntity.goalSelector.addGoal(1, new SwimGoal(witherMinionEntity));

		witherMinionEntity.targetSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof NearestAttackableTargetGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof HurtByTargetGoal)
				toRemove.add(goal.getGoal());
		});

		for (Goal goal : toRemove) {
			witherMinionEntity.targetSelector.removeGoal(goal);
		}
		toRemove.clear();

		witherMinionEntity.targetSelector.addGoal(1, new HurtByTargetGoal(witherMinionEntity, WitherEntity.class, WitherMinionEntity.class));
		witherMinionEntity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(witherMinionEntity, PlayerEntity.class, true));
		witherMinionEntity.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(witherMinionEntity, MobEntity.class, 0, false, false, NOT_UNDEAD));
	}

	public WitherMinionEntity summonMinion(World world, Vector3d pos, float difficulty, boolean isCharged) {
		WitherMinionEntity witherMinion = new WitherMinionEntity(PBEntities.WITHER_MINION.get(), world);
		CompoundNBT minionTags = witherMinion.getPersistentData();
		minionTags.putBoolean(Strings.Tags.WITHER_MINION, true);

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		witherMinion.setPosition(pos.x, pos.y, pos.z);
		//witherMinion.setCustomName(new TranslationTextComponent(Strings.Translatable.WITHER_MINION));
		setEquipment(witherMinion, difficulty, isCharged);
		//witherMinion.deathLootTable = LootTables.EMPTY;
		witherMinion.enablePersistence();

		ModifiableAttributeInstance movementSpeed = witherMinion.getAttribute(Attributes.MOVEMENT_SPEED);
		double speedBonus = this.bonusSpeedPerDifficulty * difficulty;
		AttributeModifier movementSpeedModifier = new AttributeModifier(Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS_UUID, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS, speedBonus, AttributeModifier.Operation.MULTIPLY_BASE);
		movementSpeed.applyPersistentModifier(movementSpeedModifier);

		ModifiableAttributeInstance followRange = witherMinion.getAttribute(Attributes.FOLLOW_RANGE);
		AttributeModifier followRangeBonus = new AttributeModifier(Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 16, AttributeModifier.Operation.ADDITION);
		followRange.applyPersistentModifier(followRangeBonus);

		ModifiableAttributeInstance swimSpeed = witherMinion.getAttribute(ForgeMod.SWIM_SPEED.get());
		if (swimSpeed != null) {
			AttributeModifier swimSpeedBonus = new AttributeModifier(Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);
			swimSpeed.applyPersistentModifier(swimSpeedBonus);
		}

		world.addEntity(witherMinion);
		return witherMinion;
	}
}
