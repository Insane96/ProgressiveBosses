package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.ai.wither.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.ai.wither.WitherDoNothingGoal;
import insane96mcp.progressivebosses.ai.wither.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
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
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

@Label(name = "Attack", description = "Makes the Wither smarter (will no longer try to stand on the player's head ...), attack faster and hit harder")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Boolean> applyToVanillaWitherConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> attackIntervalConfig;
	private final ForgeConfigSpec.ConfigValue<Double> increasedAttackDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> attackSpeedMultiplierOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> increaseAttackSpeedWhenNearConfig;
	private final ForgeConfigSpec.ConfigValue<Double> chargeAttackAtHealthPercentageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> skullVelocityMultiplierConfig;

	public boolean applyToVanillaWither = true;
	public int attackInterval = 50;
	public double increasedAttackDamage = 0.02d;
	public double attackSpeedMultiplierOnHalfHealth = 0.66666667d;
	public boolean increaseAttackSpeedWhenNear = true;
	public double chargeAttackAtHealthPercentage = 0.2d;
	public double skullVelocityMultiplier = 2.5d;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		applyToVanillaWitherConfig = Config.builder
				.comment("If the AI changes should be applied to the first wither spawned too.")
				.define("Apply to Vanilla Wither", applyToVanillaWither);
		attackIntervalConfig = Config.builder
				.comment("Every how many ticks (20 ticks = 1 seconds) the middle head will fire a projectile to the target.")
				.defineInRange("Attack Interval", attackInterval, 0, Integer.MAX_VALUE);
		increasedAttackDamageConfig = Config.builder
				.comment("Percentage bonus damage for the wither per difficulty.")
				.defineInRange("Increased Attack Damage", increasedAttackDamage, 0d, Integer.MAX_VALUE);
		attackSpeedMultiplierOnHalfHealthConfig = Config.builder
				.comment("The middle head will attack twice as fast when the Wither drops below half health.")
				.defineInRange("Attack Speed Multiplier on Half Health", attackSpeedMultiplierOnHalfHealth, 0d, Double.MAX_VALUE);
		increaseAttackSpeedWhenNearConfig = Config.builder
				.comment("The middle head will attack faster (up to 40% of the attack speed) the nearer the target is to the Wither.")
				.define("Increase Attack Speed when Near", increaseAttackSpeedWhenNear);
		chargeAttackAtHealthPercentageConfig = Config.builder
				.comment("The Wither will charge an attack when dropping below this health percentage.")
				.defineInRange("Charge Attack at Health Percentage", chargeAttackAtHealthPercentage, 0d, 1d);
		skullVelocityMultiplierConfig = Config.builder
				.comment("Wither Skull Projectiles speed will be multiplied by this value.")
				.defineInRange("Skull Velocity Multiplier", skullVelocityMultiplier, 0d, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.applyToVanillaWither = this.applyToVanillaWitherConfig.get();
		this.attackInterval = this.attackIntervalConfig.get();
		this.increasedAttackDamage = this.increasedAttackDamageConfig.get();
		this.attackSpeedMultiplierOnHalfHealth = this.attackSpeedMultiplierOnHalfHealthConfig.get();
		this.increaseAttackSpeedWhenNear = this.increaseAttackSpeedWhenNearConfig.get();
		this.chargeAttackAtHealthPercentage = this.chargeAttackAtHealthPercentageConfig.get();
		this.skullVelocityMultiplier = this.skullVelocityMultiplierConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getEntity() instanceof WitherSkullEntity && this.isEnabled() && this.skullVelocityMultiplier > 0d){
			WitherSkullEntity witherSkullEntity = (WitherSkullEntity) event.getEntity();
			witherSkullEntity.accelerationX *= this.skullVelocityMultiplier;
			witherSkullEntity.accelerationY *= this.skullVelocityMultiplier;
			witherSkullEntity.accelerationZ *= this.skullVelocityMultiplier;
		}

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

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (this.chargeAttackAtHealthPercentage == 0d)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		CompoundNBT witherTags = wither.getPersistentData();
		if (witherTags.contains(Strings.Tags.CHARGE_ATTACK)){
			if (wither.ticksExisted % 10 == 0 && wither.getInvulTime() > 0) {
				wither.setHealth(wither.getHealth() - 10f + (wither.getMaxHealth() * 0.01f));
			}
			return;
		}

		if (wither.getHealth() / wither.getMaxHealth() <= this.chargeAttackAtHealthPercentage) {
			wither.setInvulTime(200);
			for (int h = 0; h < 3; h++)
				wither.updateWatchedTargetId(h, 0);
			witherTags.putBoolean(Strings.Tags.CHARGE_ATTACK, true);
		}
	}

	@SubscribeEvent
	public void onLivingDamage(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (this.increasedAttackDamage == 0d)
			return;

		if (!(event.getSource().getImmediateSource() instanceof WitherSkullEntity) || !(event.getSource().getTrueSource() instanceof WitherEntity))
			return;

		WitherSkullEntity witherSkull = (WitherSkullEntity) event.getSource().getImmediateSource();
		WitherEntity wither = (WitherEntity) event.getSource().getTrueSource();
		CompoundNBT compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedAttackDamage * difficulty)));
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
		wither.goalSelector.addGoal(2, new WitherRangedAttackGoal(wither,  this.attackInterval, 24.0f, this.attackSpeedMultiplierOnHalfHealth, this.increaseAttackSpeedWhenNear));
		wither.goalSelector.addGoal(2, new WitherChargeAttackGoal(wither));

		//Fixes https://bugs.mojang.com/browse/MC-29274
		wither.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(wither, PlayerEntity.class, 0, false, false, null));
	}
}
