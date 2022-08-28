package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinion;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.UUID;

@Label(name = "Minions", description = "Wither will spawn deadly Minions")
public class MinionFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Integer> minionAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> bonusMinionEveryDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxSpawnedConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxAroundConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxCooldownConfig;
	private final ForgeConfigSpec.ConfigValue<Double> cooldownMultiplierBelowHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusSpeedConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> killMinionOnWitherDeathConfig;
	//Equipment
	private final ForgeConfigSpec.ConfigValue<Boolean> hasSwordConfig;
	private final ForgeConfigSpec.ConfigValue<Double> preHalfHealthBowChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> halfHealthBowChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> powerSharpnessChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> punchKnockbackChanceConfig;

	public int minionAtDifficulty = 1;
	public int bonusMinionEveryDifficulty = 1;
	public int maxSpawned = 6;
	public int maxAround = 18;
	public int minCooldown = 400;
	public int maxCooldown = 800;
	public double cooldownMultiplierBelowHalfHealth = 0.50d;
	public double bonusSpeed = 0.25d;
	public double magicDamageMultiplier = 3.0d;
	public boolean killMinionOnWitherDeath = true;
	//Equipment
	public boolean hasSword = true;
	public double preHalfHealthBowChance = 0.60d;
	public double halfHealthBowChance = 0.08d;
	public double powerSharpnessChance = 4.80d;
	public double punchKnockbackChance = 2.40d;

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
		bonusSpeedConfig = Config.builder
				.comment("Percentage bonus speed at max difficulty.")
				.defineInRange("Bonus Movement Speed Per Difficulty", bonusSpeed, 0d, Double.MAX_VALUE);
		magicDamageMultiplierConfig = Config.builder
				.comment("Wither Minions will take magic damage multiplied by this value.")
				.defineInRange("Magic Damage Multiplier", magicDamageMultiplier, 0, Double.MAX_VALUE);
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
				.comment("Chance (at max difficulty) for the Wither Minion Sword / Bow to be enchanted with Sharpness / Power. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.")
				.defineInRange("Power / Sharpness Chance", powerSharpnessChance, 0d, Double.MAX_VALUE);
		punchKnockbackChanceConfig = Config.builder
				.comment("Chance (at max difficulty) for the Wither Minion Sword / Bow to be enchanted with Knockback / Punch. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining chance dictates if one more level will be added.")
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
		if (this.minCooldown > this.maxCooldown)
			this.minCooldown = this.maxCooldown;
		this.cooldownMultiplierBelowHalfHealth = this.cooldownMultiplierBelowHalfHealthConfig.get();
		this.bonusSpeed = this.bonusSpeedConfig.get();
		this.magicDamageMultiplier = this.magicDamageMultiplierConfig.get();
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
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;
		CompoundTag witherTags = wither.getPersistentData();

		int cooldown = (int) (Mth.nextInt(wither.level.random, this.minCooldown, this.maxCooldown) * this.cooldownMultiplierBelowHalfHealth);
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		Level world = event.getEntity().level;
		CompoundTag witherTags = wither.getPersistentData();

		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < this.minionAtDifficulty)
			return;

		if (wither.getHealth() <= 0)
			return;

		if (wither.getInvulnerableTicks() > 0)
			return;

		int cooldown = witherTags.getInt(Strings.Tags.WITHER_MINION_COOLDOWN);
		if (cooldown > 0) {
			witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown - 1);
			return;
		}

		//If there is no player in a radius from the wither, don't spawn minions
		int radius = 32;
		BlockPos pos1 = wither.blockPosition().offset(-radius, -radius, -radius);
		BlockPos pos2 = wither.blockPosition().offset(radius, radius, radius);
		AABB bb = new AABB(pos1, pos2);
		List<ServerPlayer> players = world.getEntitiesOfClass(ServerPlayer.class, bb);

		if (players.isEmpty())
			return;

		List<WitherMinion> minionsInAABB = world.getEntitiesOfClass(WitherMinion.class, wither.getBoundingBox().inflate(16));
		int minionsCountInAABB = minionsInAABB.size();

		if (minionsCountInAABB >= this.maxAround)
			return;

		int minCooldown = this.minCooldown;
		int maxCooldown = this.maxCooldown;

		cooldown = Mth.nextInt(world.random, minCooldown, maxCooldown);
		if (wither.isPowered())
			cooldown *= this.cooldownMultiplierBelowHalfHealth;
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown - 1);

		int minionSpawnedCount = 0;
		for (int i = this.minionAtDifficulty; i <= difficulty; i += this.bonusMinionEveryDifficulty) {

			int x = 0, y = 0, z = 0;
			//Tries to spawn the Minion up to 5 times
			for (int t = 0; t < 5; t++) {
				x = (int) (wither.getX() + (Mth.nextInt(world.random, -3, 3)));
				y = (int) (wither.getY() + 3);
				z = (int) (wither.getZ() + (Mth.nextInt(world.random, -3, 3)));

				y = MCUtils.getFittingY(PBEntities.WITHER_MINION.get(), new BlockPos(x, y, z), world, 8);
				if (y != -1)
					break;
			}
			if (y <= wither.level.getMinBuildHeight())
				continue;

			WitherMinion witherMinion = summonMinion(world, new Vec3(x + 0.5, y + 0.5, z + 0.5), DifficultyHelper.getScalingDifficulty(wither), wither.isPowered());

			ListTag minionsList = witherTags.getList(Strings.Tags.MINIONS, Tag.TAG_COMPOUND);
			CompoundTag uuid = new CompoundTag();
			uuid.putUUID("uuid", witherMinion.getUUID());
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
	public void onMinionDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (this.magicDamageMultiplier == 0d)
			return;

		if (!(event.getEntity() instanceof WitherMinion))
			return;

		//Handle Magic Damage
		if (event.getSource().isMagic()) {
			event.setAmount((float) (event.getAmount() * this.magicDamageMultiplier));
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!this.killMinionOnWitherDeath)
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;
		ServerLevel world = (ServerLevel) wither.level;

		CompoundTag tags = wither.getPersistentData();
		ListTag minionsList = tags.getList(Strings.Tags.MINIONS, Tag.TAG_COMPOUND);

		for (int i = 0; i < minionsList.size(); i++) {
			UUID uuid = minionsList.getCompound(i).getUUID("uuid");
			WitherMinion witherMinion = (WitherMinion) world.getEntity(uuid);
			if (witherMinion == null)
				continue;
			witherMinion.addEffect(new MobEffectInstance(MobEffects.HEAL, 10000, 0, false, false));
		}
	}

	private void setEquipment(WitherMinion witherMinion, float scalingDifficulty, boolean isCharged) {
		witherMinion.setDropChance(EquipmentSlot.MAINHAND, Float.MIN_VALUE);

		int powerSharpnessLevel = (int) (this.powerSharpnessChance * scalingDifficulty);
		if (Mth.nextDouble(witherMinion.level.getRandom(), 0d, 1d) < (this.powerSharpnessChance * scalingDifficulty) - powerSharpnessLevel)
			powerSharpnessLevel++;

		int punchKnockbackLevel = (int) (this.punchKnockbackChance * scalingDifficulty);
		if (Mth.nextDouble(witherMinion.level.getRandom(), 0d, 1d) < (this.punchKnockbackChance * scalingDifficulty) - punchKnockbackLevel)
			punchKnockbackLevel++;

		ItemStack sword = new ItemStack(Items.STONE_SWORD);
		if (powerSharpnessLevel > 0)
			sword.enchant(Enchantments.SHARPNESS, powerSharpnessLevel);
		if (punchKnockbackLevel > 0)
			sword.enchant(Enchantments.KNOCKBACK, punchKnockbackLevel);
		if (this.hasSword)
			witherMinion.setItemSlot(EquipmentSlot.MAINHAND, sword);

		ItemStack bow = new ItemStack(Items.BOW);
		if (powerSharpnessLevel > 0)
			bow.enchant(Enchantments.POWER_ARROWS, powerSharpnessLevel);
		if (punchKnockbackLevel > 0)
			bow.enchant(Enchantments.POWER_ARROWS, punchKnockbackLevel);
		if (isCharged) {
			if (Mth.nextDouble(witherMinion.level.getRandom(), 0d, 1d) < this.halfHealthBowChance) {
				witherMinion.setItemSlot(EquipmentSlot.MAINHAND, bow);
			}
		}
		else {
			if (Mth.nextDouble(witherMinion.level.getRandom(), 0d, 1d) < this.preHalfHealthBowChance) {
				witherMinion.setItemSlot(EquipmentSlot.MAINHAND, bow);
			}
		}
	}

	public WitherMinion summonMinion(Level world, Vec3 pos, float scalingDifficulty, boolean isCharged) {
		WitherMinion witherMinion = new WitherMinion(PBEntities.WITHER_MINION.get(), world);
		CompoundTag minionTags = witherMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		witherMinion.setPos(pos.x, pos.y, pos.z);
		setEquipment(witherMinion, scalingDifficulty, isCharged);
		witherMinion.setDropChance(EquipmentSlot.MAINHAND, -0.1f);
		witherMinion.setCanPickUpLoot(false);
		witherMinion.setPersistenceRequired();

		double speedBonus = this.bonusSpeed * scalingDifficulty;
		MCUtils.applyModifier(witherMinion, Attributes.MOVEMENT_SPEED, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS_UUID, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS, speedBonus, AttributeModifier.Operation.MULTIPLY_BASE);
		MCUtils.applyModifier(witherMinion, Attributes.FOLLOW_RANGE, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS_UUID, Strings.AttributeModifiers.FOLLOW_RANGE_BONUS, 16, AttributeModifier.Operation.ADDITION);
		MCUtils.applyModifier(witherMinion, ForgeMod.SWIM_SPEED.get(), Strings.AttributeModifiers.SWIM_SPEED_BONUS_UUID, Strings.AttributeModifiers.SWIM_SPEED_BONUS, 2d, AttributeModifier.Operation.MULTIPLY_BASE);

		world.addFreshEntity(witherMinion);
		return witherMinion;
	}
}
