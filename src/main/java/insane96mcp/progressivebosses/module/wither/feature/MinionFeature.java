package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.WitherMinion;
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
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;
import java.util.UUID;

@Label(name = "Minions", description = "Wither will spawn deadly Minions")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class MinionFeature extends Feature {
	@Config(min = 0)
	@Label(name = "Minion at Difficulty", description = "At which difficulty the Wither starts spawning Minions")
	public static Integer minionAtDifficulty = 1;
	@Config(min = 0)
	@Label(name = "Bonus Minion Every Difficulty", description = "As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion")
	public static Integer bonusMinionEveryDifficulty = 1;
	@Config(min = 0)
	@Label(name = "Max Minions Spawned", description = "Maximum Minions spawned by the Wither")
	public static Integer maxSpawned = 6;
	@Config(min = 0)
	@Label(name = "Max Minions Around", description = "Maximum amount of Minions that can be around the Wither in a 16 block radius. After this number is reached the Wither will stop spawning minions. Set to 0 to disable this check")
	public static Integer maxAround = 18;
	@Config(min = 0)
	@Label(name = "Minimum Cooldown", description = "Minimum ticks (20 ticks = 1 seconds) after Minions can spawn.")
	public static Integer minCooldown = 400;
	@Config(min = 0)
	@Label(name = "Maximum Cooldown", description = "Maximum ticks (20 ticks = 1 seconds) after Minions can spawn.")
	public static Integer maxCooldown = 800;
	@Config(min = 0d)
	@Label(name = "Cooldown Multiplier Below Half Health", description = "Min and Max cooldowns are multiplied by this value when the Wither drops below half health. Set to 1 to not change the cooldown when the wither's health drops below half.")
	public static Double cooldownMultiplierBelowHalfHealth = 0.50d;
	@Config(min = 0d)
	@Label(name = "Bonus Movement Speed Per Difficulty", description = "Percentage bonus speed at max difficulty.")
	public static Double bonusSpeed = 0.25d;
	@Config(min = 0d)
	@Label(name = "Magic Damage Taken Multiplier", description = "Wither Minions will take magic damage multiplied by this value.")
	public static Double magicDamageMultiplier = 3.0d;
	@Config
	@Label(name = "Kill Minions on Wither Death", description = "Wither Minions will die when the Wither that summoned them dies.")
	public static Boolean killMinionOnWitherDeath = true;
	@Config(min = 0d, max = 1d)
	@Label(name = "Equipment.Bow Chance Above Half Health", description = "Chance for the Wither Minion to spawn with a bow instead of a Stone Sword when Wither's above Half Health.")
	public static Double aboveHalfHealthBowChance = 0.60d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Equipment.Bow Chance Below Half Health", description = "Chance for the Wither Minion to spawn with a bow instead of a Stone Sword when Wither's below Half Health.")
	public static Double belowHalfHealthBowChance = 0.08d;
	//TODO Make a list of enchantments instead of making one config option per enchantment
	@Config(min = 0d, max = 127d)
	@Label(name = "Equipment.Enchantments.Sharpness Chance", description = "Chance (at max difficulty) for the Wither Minion's Sword to be enchanted with Sharpness. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining dictates the chance to add on more level.")
	public static Double sharpnessChance = 2.40d;
	@Config(min = 0d, max = 127d)
	@Label(name = "Equipment.Enchantments.Knockback Chance", description = "Chance (at max difficulty) for the Wither Minion's Sword to be enchanted with Knockback. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining dictates the chance to add on more level.")
	public static Double knockbackChance = 2.40d;
	@Config(min = 0d, max = 127d)
	@Label(name = "Equipment.Enchantments.Power Chance", description = "Chance (at max difficulty) for the Wither Minion's Bow to be enchanted with Power. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining dictates the chance to add on more level.")
	public static Double powerChance = 3.20d;
	@Config(min = 0d, max = 127d)
	@Label(name = "Equipment.Enchantments.Punch Chance", description = "Chance (at max difficulty) for the Wither Minion's Bow to be enchanted with Punch. Note that every 100% chance adds one guaranteed level of the enchantment, while the remaining dictates the chance to add on more level.")
	public static Double punchChance = 1.50d;

	public MinionFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@Override
	public void readConfig(final ModConfigEvent event) {
		super.readConfig(event);
		if (minCooldown > maxCooldown)
			minCooldown = maxCooldown;
	}

	@SubscribeEvent
	public void onWitherSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		CompoundTag witherTags = wither.getPersistentData();

		int cooldown = (int) (Mth.nextInt(wither.level().random, minCooldown, maxCooldown) * cooldownMultiplierBelowHalfHealth);
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown);
	}

	@SubscribeEvent
	public void update(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		Level world = event.getEntity().level();
		CompoundTag witherTags = wither.getPersistentData();

		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);
		if (difficulty < minionAtDifficulty
				|| wither.getHealth() <= 0
				|| wither.getInvulnerableTicks() > 0)
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

		if (minionsCountInAABB >= maxAround)
			return;

		cooldown = Mth.nextInt(world.random, minCooldown, maxCooldown);
		if (wither.isPowered())
			cooldown *= cooldownMultiplierBelowHalfHealth;
		witherTags.putInt(Strings.Tags.WITHER_MINION_COOLDOWN, cooldown - 1);

		int minionSpawnedCount = 0;
		for (int i = minionAtDifficulty; i <= difficulty; i += bonusMinionEveryDifficulty) {

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
			if (y <= wither.level().getMinBuildHeight())
				continue;

			WitherMinion witherMinion = summonMinion(world, new Vec3(x + 0.5, y + 0.5, z + 0.5), DifficultyHelper.getScalingDifficulty(wither), wither.isPowered());

			ListTag minionsList = witherTags.getList(Strings.Tags.MINIONS, Tag.TAG_COMPOUND);
			CompoundTag uuid = new CompoundTag();
			uuid.putUUID("uuid", witherMinion.getUUID());
			minionsList.add(uuid);
			witherTags.put(Strings.Tags.MINIONS, minionsList);

			minionSpawnedCount++;
			if (minionSpawnedCount >= maxSpawned)
				break;

			minionsCountInAABB++;
			if (minionsCountInAABB >= maxAround)
				break;
		}
	}

	@SubscribeEvent
	public void onMinionDamage(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| magicDamageMultiplier == 0d
				|| !(event.getEntity() instanceof WitherMinion)
				|| (!event.getSource().is(DamageTypes.MAGIC) && !event.getSource().is(DamageTypes.INDIRECT_MAGIC)))
			return;

		event.setAmount((float) (event.getAmount() * magicDamageMultiplier));
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !killMinionOnWitherDeath
				|| !(event.getEntity() instanceof WitherBoss wither))
			return;

		ServerLevel world = (ServerLevel) wither.level();

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

	@SubscribeEvent
	public void onEntityDeath(LivingDeathEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getSource().getEntity() instanceof WitherMinion witherMinion))
			return;

		LivingEntity livingEntity = event.getEntity();

		boolean hasPlantedRose = false;
		if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(witherMinion.level(), witherMinion)) {
			BlockPos blockpos = livingEntity.blockPosition();
			BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
			if (livingEntity.level().isEmptyBlock(blockpos) && blockstate.canSurvive(livingEntity.level(), blockpos)) {
				livingEntity.level().setBlock(blockpos, blockstate, 3);
				hasPlantedRose = true;
			}
		}

		if (!hasPlantedRose) {
			ItemEntity itementity = new ItemEntity(livingEntity.level(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), new ItemStack(Items.WITHER_ROSE));
			livingEntity.level().addFreshEntity(itementity);
		}
	}

	private static void setEquipment(WitherMinion witherMinion, float scalingDifficulty, boolean isCharged) {
		witherMinion.setDropChance(EquipmentSlot.MAINHAND, Float.MIN_VALUE);

		double bowChance = isCharged ? belowHalfHealthBowChance : aboveHalfHealthBowChance;
		ItemStack item;

		if (Mth.nextDouble(witherMinion.level().getRandom(), 0d, 1d) < bowChance) {
			item = new ItemStack(Items.BOW);
			int powerLevel = MathHelper.getAmountWithDecimalChance(witherMinion.getRandom(), powerChance * scalingDifficulty);
			if (powerLevel > 0)
				item.enchant(Enchantments.POWER_ARROWS, powerLevel);
			int punchLevel = MathHelper.getAmountWithDecimalChance(witherMinion.getRandom(), punchChance * scalingDifficulty);
			if (punchLevel > 0)
				item.enchant(Enchantments.PUNCH_ARROWS, punchLevel);
		}
		else {
			item = new ItemStack(Items.STONE_SWORD);
			int sharpnessLevel = MathHelper.getAmountWithDecimalChance(witherMinion.getRandom(), sharpnessChance * scalingDifficulty);
			if (sharpnessLevel > 0)
				item.enchant(Enchantments.SHARPNESS, sharpnessLevel);
			int knockbackLevel = MathHelper.getAmountWithDecimalChance(witherMinion.getRandom(), knockbackChance * scalingDifficulty);
			if (knockbackLevel > 0)
				item.enchant(Enchantments.KNOCKBACK, knockbackLevel);
		}
		witherMinion.setItemSlot(EquipmentSlot.MAINHAND, item);
	}

	public static WitherMinion summonMinion(Level world, Vec3 pos, float scalingDifficulty, boolean isCharged) {
		WitherMinion witherMinion = new WitherMinion(PBEntities.WITHER_MINION.get(), world);
		CompoundTag minionTags = witherMinion.getPersistentData();

		minionTags.putBoolean("mobspropertiesrandomness:processed", true);
		//TODO Scaling health

		witherMinion.setPos(pos.x, pos.y, pos.z);
		setEquipment(witherMinion, scalingDifficulty, isCharged);
		witherMinion.setDropChance(EquipmentSlot.MAINHAND, -0.1f);
		witherMinion.setCanPickUpLoot(false);
		witherMinion.setPersistenceRequired();

		double speedBonus = bonusSpeed * scalingDifficulty;
		MCUtils.applyModifier(witherMinion, Attributes.MOVEMENT_SPEED, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS_UUID, Strings.AttributeModifiers.MOVEMENT_SPEED_BONUS, speedBonus, AttributeModifier.Operation.MULTIPLY_BASE);

		world.addFreshEntity(witherMinion);
		return witherMinion;
	}
}
