package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.wither.ai.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.module.wither.ai.WitherDoNothingGoal;
import insane96mcp.progressivebosses.module.wither.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.RangedAttackGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...), attack faster and hit harder")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> applyToVanillaWitherConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxChargeAttackChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> increasedDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxBarrageChancePerDiffConfig;
	//private final ForgeConfigSpec.ConfigValue<Double> maxBarrageAttackChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> minBarrageDurationConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> maxBarrageDurationConfig;
	private final ForgeConfigSpec.ConfigValue<Double> skullVelocityMultiplierConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> attackIntervalConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> increaseAttackSpeedWhenNearConfig;

	public boolean applyToVanillaWither = true;
	public double maxChargeAttackChance = 0.06;
	public double increasedDamage = 0.04d;
	//Barrage Attack
	public double maxBarrageChancePerDiff = 0.0035d;
	//public double maxBarrageAttackChance = 0.04d;
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
				.comment("Max Chance every time the Wither takes damage to start a charge attack. The actual chance is inversely proportional to Wither's health (100% health = 0% chance, 50% health = 2.5% chance, ...).")
				.defineInRange("Max Charge Attack Chance", maxChargeAttackChance, 0d, 1d);
		increasedDamageConfig = Config.builder
				.comment("Percentage bonus damage dealt by the Wither per difficulty.")
				.defineInRange("Increased Damage", increasedDamage, 0d, Double.MAX_VALUE);
		//Barrage
		Config.builder.push("Barrage Attack");
		maxBarrageChancePerDiffConfig = Config.builder
				.comment("Chance (per difficulty) every time the Wither takes damage to start a barrage attack. The actual chance is inversely proportional to Wither's health and damage taken (more damage and less health = higher chance).")
				.defineInRange("Max Barrage Attack Chance Per Difficulty", maxBarrageChancePerDiff, 0d, 1d);
		/*maxBarrageAttackChanceConfig = Config.builder
				.comment("Max Chance for the barrage attack. The max chance is doubled when the Wither is below half health")
				.defineInRange("Max Barrage Attack Chance", maxBarrageAttackChance, 0d, 1d);*/
		minBarrageDurationConfig = Config.builder
				.comment("Min time (in ticks) for the duration of the barrage attack. The actual duration is inversely proportional to Wither's health and damage taken (more damage and less health = higher chance)")
				.defineInRange("Min Barrage Duration", minBarrageDuration, 0, Integer.MAX_VALUE);
		maxBarrageDurationConfig = Config.builder
				.comment("Max time (in ticks) for the duration of the barrage attack. The actual duration is inversely proportional to Wither's health (100% health = min duration, 0% health = max duration)")
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
		//this.maxBarrageAttackChance = this.maxBarrageAttackChanceConfig.get();
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

		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT compoundNBT = wither.getPersistentData();
		if ((!compoundNBT.contains(Strings.Tags.DIFFICULTY) || compoundNBT.getFloat(Strings.Tags.DIFFICULTY) == 0f) && !this.applyToVanillaWither)
			return;

		setWitherAI(wither);
	}

	private void witherSkullSpeed(Entity entity) {
		if (!(entity instanceof WitherSkullEntity))
			return;

		if (!this.isEnabled() || this.skullVelocityMultiplier == 0d)
			return;

		WitherSkullEntity witherSkullEntity = (WitherSkullEntity) entity;

		if (Math.abs(witherSkullEntity.accelerationX) > 10 || Math.abs(witherSkullEntity.accelerationY) > 10 || Math.abs(witherSkullEntity.accelerationZ) > 10) {
			entity.onKillCommand();
			return;
		}

		witherSkullEntity.accelerationX *= this.skullVelocityMultiplier;
		witherSkullEntity.accelerationY *= this.skullVelocityMultiplier;
		witherSkullEntity.accelerationZ *= this.skullVelocityMultiplier;
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (this.maxChargeAttackChance == 0d /*|| this.maxChargeAttackChance == 0d*/)
			return;

		if (!event.getEntity().isAlive())
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT witherTags = wither.getPersistentData();
		if (witherTags.contains(Strings.Tags.CHARGE_ATTACK) && wither.ticksExisted % 10 == 0){
			float missingHealth = wither.getMaxHealth() - wither.getHealth();
			//Remove the vanilla health regeneration when he's invulnerable and adds 1% health regeneration of the missing health per second
			wither.setHealth(wither.getHealth() - 10f + (missingHealth * 0.005f));
		}
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (this.increasedDamage == 0d)
			return;


		WitherEntity wither;
		if (event.getSource().getImmediateSource() instanceof WitherEntity)
			wither = (WitherEntity) event.getSource().getImmediateSource();
		else if (event.getSource().getTrueSource() instanceof WitherEntity)
			wither = (WitherEntity) event.getSource().getTrueSource();
		else
			return;

		CompoundNBT compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedDamage * difficulty)));
	}

	//High priority so runs before the damage reduction
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onDamaged(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!event.getEntity().isAlive())
			return;

		if (!(event.getEntityLiving() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntityLiving();
		LogHelper.info("Damage taken: %s", event.getAmount());

		doBarrage(wither, event.getAmount());
		doCharge(wither, event.getAmount());
	}

	private void doBarrage(WitherEntity wither, float damageTaken) {
		if (this.maxBarrageChancePerDiff == 0d/* || this.maxBarrageAttackChance == 0d*/)
			return;

		CompoundNBT witherTags = wither.getPersistentData();
		float difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);

		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();

		double chance = (this.maxBarrageChancePerDiff * difficulty) * missingHealthPerc;
		chance *= (damageTaken / 10f);
		double r = wither.getRNG().nextDouble();
		if (r < chance) {
			//int barrage = witherTags.getInt(Strings.Tags.BARRAGE_ATTACK);
			int duration = (int) (((this.maxBarrageDuration - this.minBarrageDuration) * missingHealthPerc) + this.minBarrageDuration);
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, duration);
		}
	}

	private void doCharge(WitherEntity wither, float damageTaken) {
		if (this.maxChargeAttackChance == 0d)
			return;

		CompoundNBT witherTags = wither.getPersistentData();

		double missingHealthPerc = 1d - wither.getHealth() / wither.getMaxHealth();
		double chance = this.maxChargeAttackChance * missingHealthPerc;
		chance *= (damageTaken / 10f);
		double r = wither.getRNG().nextDouble();
		if (r < chance) {
			wither.setInvulTime(Consts.CHARGE_ATTACK_TICK_START);
			witherTags.putBoolean(Strings.Tags.CHARGE_ATTACK, true);
		}
	}

	public void setWitherAI(WitherEntity wither) {
		ArrayList<Goal> toRemove = new ArrayList<>();
		wither.goalSelector.goals.forEach(goal -> {
			if (goal.getGoal() instanceof RangedAttackGoal)
				toRemove.add(goal.getGoal());
			if (goal.getGoal() instanceof WitherEntity.DoNothingGoal)
				toRemove.add(goal.getGoal());
		});

		toRemove.forEach(wither.goalSelector::removeGoal);

		wither.goalSelector.addGoal(0, new WitherDoNothingGoal(wither));
		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither,  this.attackInterval, 24.0f, this.increaseAttackSpeedWhenNear));
		wither.goalSelector.addGoal(2, new WitherChargeAttackGoal(wither));

		//Fixes https://bugs.mojang.com/browse/MC-29274
		wither.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(wither, PlayerEntity.class, 0, false, false, null));
	}

	public static class Consts {
		public static final int CHARGE_ATTACK_TICK_START = 90;
		public static final int CHARGE_ATTACK_TICK_CHARGE = 30;
	}
}
