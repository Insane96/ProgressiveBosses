package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration.")
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusCrystalRegenConfig;
	private final ForgeConfigSpec.DoubleValue bonusRegenRatioWhenHitConfig;
	private final ForgeConfigSpec.IntValue bonusRegenerationWhenHitDurationConfig;

	public double bonusHealth = 200d;
	public double bonusRegen = 1.0d;
	public double maxBonusRegen = 1.0d;
	public double bonusCrystalRegen = 0d;
	public double bonusRegenRatioWhenHit = 0.4d;
	public int bonusRegenerationWhenHitDuration = 85;

	public HealthFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusHealthConfig = Config.builder
				.comment("Ender Dragon health will be increased by this value at max difficulty (scaling accordingly at lower difficulties)")
				.defineInRange("Health Bonus at Max Difficulty", bonusHealth, 0.0, Double.MAX_VALUE);
		bonusRegenConfig = Config.builder
				.comment("How much health will the Ender Dragon regen at max difficulty (scaling accordingly at lower difficulties). This doesn't affect the health regen given by crystals.")
				.defineInRange("Bonus Regeneration", bonusRegen, 0.0, Double.MAX_VALUE);
		maximumBonusRegenConfig = Config.builder
				.comment("Maximum bonus regeneration per second given by \"Bonus Regeneration\". Set to 0 to disable bonus health regeneration. Can be lower than \"Bonus Regeneration\". This doesn't affect the health regen given by crystals.")
				.defineInRange("Maximum Bonus Regeneration", maxBonusRegen, 0.0, Double.MAX_VALUE);
		this.bonusCrystalRegenConfig = Config.builder
				.comment("How much health (when missing 100% health) will the Ender Dragon regen at max difficulty each second whenever she's attached to a Crystal. So if she's missing 30% health, this will be 30% effective. This is added to the normal Crystal regen.")
				.defineInRange("Bonus Crystal Regeneration", this.bonusCrystalRegen, 0.0, Double.MAX_VALUE);
		this.bonusRegenRatioWhenHitConfig = Config.builder
				.comment("Bonus regeneration (also crystal bonus regeneration) will be multiplied by this ratio when the Dragon has been hit in the last 'Bonus Regeneration When Hit Duration' ticks.")
				.defineInRange("Bonus Regeneration Ratio When Hit", bonusRegenRatioWhenHit, 0.0d, 2.0d);
		this.bonusRegenerationWhenHitDurationConfig = Config.builder
				.defineInRange("Bonus Regeneration When Hit Duration", bonusRegenerationWhenHitDuration, 0, Integer.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusHealth = this.bonusHealthConfig.get();
		this.maxBonusRegen = this.maximumBonusRegenConfig.get();
		this.bonusRegen = this.bonusRegenConfig.get();
		this.bonusCrystalRegen = this.bonusCrystalRegenConfig.get();
		this.bonusRegenRatioWhenHit = this.bonusRegenRatioWhenHitConfig.get();
		this.bonusRegenerationWhenHitDuration = this.bonusRegenerationWhenHitDurationConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		//noinspection ConstantConditions
		if (event.getWorld().isClientSide
				|| !this.isEnabled()
				|| this.bonusHealth == 0d
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| dragon.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		MCUtils.applyModifier(dragon, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, this.bonusHealth * DifficultyHelper.getScalingDifficulty(dragon), AttributeModifier.Operation.ADDITION);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| !dragon.isAlive()
				|| dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING)
			return;

		CompoundTag tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		float flatBonusHeal = (float) Math.min(this.bonusRegen * DifficultyHelper.getScalingDifficulty(dragon), this.maxBonusRegen);
		float crystalBonusHeal = getCrystalBonusHeal(dragon);

		float heal = flatBonusHeal + crystalBonusHeal;
		if (heal == 0f)
			return;

		heal /= 20f;

		if (dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= bonusRegenerationWhenHitDuration) // 4.25 seconds
			heal *= this.bonusRegenRatioWhenHit;

		dragon.heal(heal);
	}

	private float getCrystalBonusHeal(EnderDragon dragon) {
		if (this.bonusCrystalRegen == 0d
				|| dragon.nearestCrystal == null || !dragon.nearestCrystal.isAlive())
			return 0f;

		double currHealthPerc = 1 - (dragon.getHealth() / dragon.getMaxHealth());
		return (float) (this.bonusCrystalRegen * DifficultyHelper.getScalingDifficulty(dragon) * currHealthPerc);
	}
}
