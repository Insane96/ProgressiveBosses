package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> meleeDamageReductionAboveHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxMeleeDamageReductionAboveHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> meleeDamageReductionBelowHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxMeleeDamageReductionBelowHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageBonusConfig;

	public double meleeDamageReductionAboveHalfHealth = 0.24d;
	public double maxMeleeDamageReductionAboveHalfHealth = 0.24d;
	public double meleeDamageReductionBelowHalfHealth = 0.48d;
	public double maxMeleeDamageReductionBelowHalfHealth = 0.48d;
	public double magicDamageBonus = 250d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		meleeDamageReductionAboveHalfHealthConfig = Config.builder
				.comment("Percentage Melee Damage Reduction (at max difficulty) while the Wither is above half health.")
				.defineInRange("Melee Damage reduction above half health", meleeDamageReductionAboveHalfHealth, 0d, 1d);
		maxMeleeDamageReductionAboveHalfHealthConfig = Config.builder
				.comment("Cap for 'Melee Damage reduction above half health'")
				.defineInRange("Max Melee Damage reduction before half health", maxMeleeDamageReductionAboveHalfHealth, 0d, 1d);
		meleeDamageReductionBelowHalfHealthConfig = Config.builder
				.comment("Percentage Melee Damage Reduction (at max difficulty) as the Wither drops below half health.")
				.defineInRange("Melee Damage reduction below half health", meleeDamageReductionBelowHalfHealth, 0d, 1d);
		maxMeleeDamageReductionBelowHalfHealthConfig = Config.builder
				.comment("Cap for 'Melee Damage Reduction below half health'")
				.defineInRange("Max Melee Damage reduction below half health", maxMeleeDamageReductionBelowHalfHealth, 0d, 1d);
		magicDamageBonusConfig = Config.builder
				.comment("Bonus magic damage based off missing health. 250 means that every 250 missing health the damage will be amplified by 100%. E.g. The first Wither (with 300 max health) is at 50 health (so it's missing 250hp), on magic damage he will receive 'magic_damage * (missing_health / magic_damage_bonus + 1)' = 'magic_damage * (250 / 250 + 1)' = 'magic_damage * 2'.")
				.defineInRange("Magic Damage Bonus", magicDamageBonus, 0d, 1024f);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.meleeDamageReductionAboveHalfHealth = this.meleeDamageReductionAboveHalfHealthConfig.get();
		this.maxMeleeDamageReductionAboveHalfHealth = this.maxMeleeDamageReductionAboveHalfHealthConfig.get();
		this.meleeDamageReductionBelowHalfHealth = this.meleeDamageReductionBelowHalfHealthConfig.get();
		this.maxMeleeDamageReductionBelowHalfHealth = this.maxMeleeDamageReductionBelowHalfHealthConfig.get();
		this.magicDamageBonus = this.magicDamageBonusConfig.get();
	}

	@SubscribeEvent
	public void onWitherDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		if ((this.meleeDamageReductionBelowHalfHealth == 0d || this.maxMeleeDamageReductionBelowHalfHealth == 0d)
				&& (this.meleeDamageReductionAboveHalfHealth == 0d || this.maxMeleeDamageReductionAboveHalfHealth == 0d)
				&& this.magicDamageBonus == 0d)
			return;

		//Handle Magic Damage
		if (event.getSource().isMagic() && this.magicDamageBonus > 0d) {
			double missingHealth = wither.getMaxHealth() - wither.getHealth();
			event.setAmount((event.getAmount() * (float) (missingHealth / (this.magicDamageBonus) + 1)));
		}

		if (event.getSource().getDirectEntity() != event.getSource().getEntity())
			return;

		//Handle Damage Reduction
		float damageReduction;
		if (!wither.isPowered())
			damageReduction = (float) Math.min(this.maxMeleeDamageReductionAboveHalfHealth, this.meleeDamageReductionAboveHalfHealth * DifficultyHelper.getScalingDifficulty(wither));
		else
			damageReduction = (float) Math.min(this.maxMeleeDamageReductionBelowHalfHealth, this.meleeDamageReductionBelowHalfHealth * DifficultyHelper.getScalingDifficulty(wither));

		if (damageReduction == 0d)
			return;

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
