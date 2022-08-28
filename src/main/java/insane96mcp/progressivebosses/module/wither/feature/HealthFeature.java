package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration. The feature even fixes the Wither health bar not updating on spawn.")
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenConfig;

	public double bonusHealth = 720d;
	public double maxBonusRegen = 2d;
	public double bonusRegen = 2.4d;

	public HealthFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		this.bonusHealthConfig = Config.builder
				.comment("Increase Wither's Health by this value at max difficulty (scales accordingly at lower difficulties)")
				.defineInRange("Health Bonus per Difficulty", bonusHealth, 0.0, Double.MAX_VALUE);
		this.maximumBonusRegenConfig = Config.builder
				.comment("""
						Maximum bonus regeneration per second given by "Bonus Regeneration".
						Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second).
						Note that this bonus health regen is disabled when Wither's health is between 49% and 50% to prevent making it impossible to approach when reaches half health.""")
				.defineInRange("Maximum Bonus Regeneration", maxBonusRegen, 0.0, Double.MAX_VALUE);
		this.bonusRegenConfig = Config.builder
				.comment("How many half hearts will the Wither regen at max difficulty. This is added to the natural regeneration of the Wither (1 Health per Second).")
				.defineInRange("Bonus Regeneration", bonusRegen, 0.0, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusHealth = this.bonusHealthConfig.get();
		this.maxBonusRegen = this.maximumBonusRegenConfig.get();
		this.bonusRegen = this.bonusRegenConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide
				|| !this.isEnabled()
				|| this.bonusHealth == 0d
				|| !(event.getEntity() instanceof WitherBoss wither)
				|| wither.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		MCUtils.applyModifier(wither, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, this.bonusHealth * DifficultyHelper.getScalingDifficulty(wither), AttributeModifier.Operation.ADDITION);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss wither)
				|| this.bonusRegen == 0d
				|| this.maxBonusRegen == 0d
				|| wither.getInvulnerableTicks() > 0
				|| !wither.isAlive())
			return;

		//Disable bonus health regen when health between 49% and 50%
		if (wither.getHealth() / wither.getMaxHealth() > 0.49f && wither.getHealth() / wither.getMaxHealth() < 0.50f)
			return;

		float heal = (float) Math.min(this.bonusRegen * DifficultyHelper.getScalingDifficulty(wither), this.maxBonusRegen);
		heal /= 20f;

		if (heal > 0f)
			wither.heal(heal);
	}
}
