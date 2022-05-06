package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.module.wither.ai.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.module.wither.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.network.MessageWitherSync;
import insane96mcp.progressivebosses.network.PacketManager;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;

import java.util.ArrayList;
import java.util.UUID;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...), attack faster and hit harder")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> applyToVanillaWitherConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxChargeAttackChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> increasedDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxBarrageChancePerDiffConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minBarrageDurationConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxBarrageDurationConfig;
	private final ForgeConfigSpec.ConfigValue<Double> skullVelocityMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> attackIntervalConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> increaseAttackSpeedWhenNearConfig;

	public boolean applyToVanillaWither = true;
	public double maxChargeAttackChance = 0.06d;
	public double increasedDamage = 0.11d;
	//Barrage Attack
	public double maxBarrageChancePerDiff = 0.011d;
	public int minBarrageDuration = 20;
	public int maxBarrageDuration = 150;
	//Skulls
	public double skullVelocityMultiplier = 2.75d;
	//Attack Speed
	public int attackInterval = 40;
	public boolean increaseAttackSpeedWhenNear = true;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		applyToVanillaWitherConfig = Config.builder
				.comment("If the AI changes should be applied to the first wither spawned too.")
				.define("Apply to Vanilla Wither", applyToVanillaWither);
		maxChargeAttackChanceConfig = Config.builder
				.comment("Chance every time the Wither takes damage to start a charge attack. Less health = higher chance and more damage taken = more chance. This value is the chance at 0% health and when taking 10 damage.")
				.defineInRange("Max Charge Attack Chance", maxChargeAttackChance, 0d, 1d);
		increasedDamageConfig = Config.builder
				.comment("Percentage bonus damage dealt by the Wither per difficulty.")
				.defineInRange("Increased Damage", increasedDamage, 0d, Double.MAX_VALUE);
		//Barrage
		Config.builder.push("Barrage Attack");
		maxBarrageChancePerDiffConfig = Config.builder
				.comment("Chance (per difficulty) every time the Wither takes damage to start a barrage attack. Less health = higher chance and more damage taken = more chance. This value is the chance at 0% health and when taking 10 damage.")
				.defineInRange("Max Barrage Attack Chance Per Difficulty", maxBarrageChancePerDiff, 0d, 1d);
		minBarrageDurationConfig = Config.builder
				.comment("Min time (in ticks) for the duration of the barrage attack. Less health = longer barrage.")
				.defineInRange("Min Barrage Duration", minBarrageDuration, 0, Integer.MAX_VALUE);
		maxBarrageDurationConfig = Config.builder
				.comment("Max time (in ticks) for the duration of the barrage attack. Less health = longer barrage")
				.defineInRange("Max Barrage Duration", maxBarrageDuration, 0, Integer.MAX_VALUE);
		Config.builder.pop();
		//Skulls
		Config.builder.comment("Wither Skull Changes").push("Skulls");
		skullVelocityMultiplierConfig = Config.builder
				.comment("Wither Skull Projectiles speed will be multiplied by this value.")
				.defineInRange("Skull Velocity Multiplier", skullVelocityMultiplier, 0d, Double.MAX_VALUE);
		Config.builder.pop();
		//Attack Speed
		Config.builder.comment("Attack Speed Changes").push("Attack Speed");
		attackIntervalConfig = Config.builder
				.comment("Every how many ticks (20 ticks = 1 seconds) the middle head will fire a projectile to the target.")
				.defineInRange("Attack Interval", attackInterval, 0, Integer.MAX_VALUE);
		increaseAttackSpeedWhenNearConfig = Config.builder
				.comment("The middle head will attack faster (up to 40% of the attack speed) the nearer the target is to the Wither.")
				.define("Increase Attack Speed when Near", increaseAttackSpeedWhenNear);
		Config.builder.pop();

		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.applyToVanillaWither = this.applyToVanillaWitherConfig.get();
		this.maxChargeAttackChance = this.maxChargeAttackChanceConfig.get();
		this.increasedDamage = this.increasedDamageConfig.get();
		//Barrage
		this.maxBarrageChancePerDiff = this.maxBarrageChancePerDiffConfig.get();
		this.minBarrageDuration = this.minBarrageDurationConfig.get();
		this.maxBarrageDuration = this.maxBarrageDurationConfig.get();
		//Skulls
		this.skullVelocityMultiplier = this.skullVelocityMultiplierConfig.get();
		//Attack Speed
		this.attackInterval = this.attackIntervalConfig.get();
		this.increaseAttackSpeedWhenNear = this.increaseAttackSpeedWhenNearConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		witherSkullSpeed(event.getEntity());

		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;
		CompoundTag compoundNBT = wither.getPersistentData();
		if ((!compoundNBT.contains(Strings.Tags.DIFFICULTY) || compoundNBT.getFloat(Strings.Tags.DIFFICULTY) == 0f) && !this.applyToVanillaWither)
			return;

		setWitherAI(wither);
	}

	private void witherSkullSpeed(Entity entity) {
		if (!(entity instanceof WitherSkull witherSkullEntity))
			return;

		if (!this.isEnabled() || this.skullVelocityMultiplier == 0d)
			return;

		if (Math.abs(witherSkullEntity.xPower) > 10 || Math.abs(witherSkullEntity.yPower) > 10 || Math.abs(witherSkullEntity.zPower) > 10) {
			entity.kill();
			return;
		}

		witherSkullEntity.xPower *= this.skullVelocityMultiplier;
		witherSkullEntity.yPower *= this.skullVelocityMultiplier;
		witherSkullEntity.zPower *= this.skullVelocityMultiplier;
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (!this.isEnabled())
			return;

		if (!event.getEntity().isAlive())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		tickCharge(wither);

		if (event.getEntity().level.isClientSide)
			return;

		chargeUnseen(wither);
	}

	private void tickCharge(WitherBoss wither) {
		if (this.maxChargeAttackChance == 0d)
			return;
		byte chargeTick = wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK);

		// When in charge attack remove the vanilla health regeneration when he's invulnerable and add 1% health regeneration of the missing health per second
		if (chargeTick > 0){
			if (wither.tickCount % 10 == 0) {
				float missingHealth = wither.getMaxHealth() - wither.getHealth();
				wither.setHealth(wither.getHealth() + (missingHealth * 0.005f));
			}
			wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) (chargeTick - 1));
		}
	}

	private void chargeUnseen(WitherBoss wither) {
		CompoundTag witherTags = wither.getPersistentData();

		if (witherTags.getByte(Strings.Tags.CHARGE_ATTACK) <= 0 && wither.tickCount % 20 == 0) {
			doCharge(wither, witherTags.getInt(Strings.Tags.UNSEEN_PLAYER_TICKS) / 20f);
		}
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.increasedDamage == 0d)
			return;


		WitherBoss wither;
		if (event.getSource().getDirectEntity() instanceof WitherBoss)
			wither = (WitherBoss) event.getSource().getDirectEntity();
		else if (event.getSource().getEntity() instanceof WitherBoss)
			wither = (WitherBoss) event.getSource().getEntity();
		else
			return;

		CompoundTag compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedDamage * difficulty)));
	}

	//High priority so runs before the damage reduction
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamaged(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!event.getEntity().isAlive())
			return;

		if (!(event.getEntityLiving() instanceof WitherBoss wither))
			return;

		doBarrage(wither, event.getAmount());
		doCharge(wither, event.getAmount());
	}

	private void doBarrage(WitherBoss wither, float damageTaken) {
		if (this.maxBarrageChancePerDiff == 0d)
			return;

		CompoundTag witherTags = wither.getPersistentData();
		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);

		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();

		double chance = (this.maxBarrageChancePerDiff * difficulty) * missingHealthPerc;
		chance *= (damageTaken / 10f);
		double r = wither.getRandom().nextDouble();
		if (r < chance) {
			int duration = (int) (((this.maxBarrageDuration - this.minBarrageDuration) * missingHealthPerc) + this.minBarrageDuration);
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, duration);
		}
	}

	private void doCharge(WitherBoss wither, float damageTaken) {
		if (this.maxChargeAttackChance == 0d)
			return;
		if (wither.getPersistentData().getByte(Strings.Tags.CHARGE_ATTACK) > 0)
			return;

		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();
		double chance = this.maxChargeAttackChance * missingHealthPerc;
		chance *= (damageTaken / 10f);
		double r = wither.getRandom().nextDouble();
		if (r < chance) {
			initCharging(wither);
		}
	}

	public void setWitherAI(WitherBoss wither) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		wither.goalSelector.availableGoals.forEach(goal -> {
			if (goal.getGoal() instanceof RangedAttackGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof WitherBoss.WitherDoNothingGoal)
				toRemove.add(goal.getGoal());
		});

		toRemove.forEach(wither.goalSelector::removeGoal);

		wither.goalSelector.addGoal(1, new WitherChargeAttackGoal(wither));
		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither,  this.attackInterval, 24.0f, this.increaseAttackSpeedWhenNear));

		MCUtils.applyModifier(wither, Attributes.FOLLOW_RANGE, UUID.randomUUID(), "Wither Glasses", 48d, AttributeModifier.Operation.ADDITION);
	}

	public static class Consts {
		public static final int CHARGE_ATTACK_TICK_START = 90;
		public static final int CHARGE_ATTACK_TICK_CHARGE = 30;
	}

	public static void initCharging(WitherBoss wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) Consts.CHARGE_ATTACK_TICK_START);
		Object msg = new MessageWitherSync(wither.getId(), (byte) Consts.CHARGE_ATTACK_TICK_START);
		for (Player player : wither.level.players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	public static void stopCharging(WitherBoss wither) {
		wither.getPersistentData().putByte(Strings.Tags.CHARGE_ATTACK, (byte) 0);
		Object msg = new MessageWitherSync(wither.getId(), (byte) 0);
		for (Player player : wither.level.players()) {
			PacketManager.CHANNEL.sendTo(msg, ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}
}
