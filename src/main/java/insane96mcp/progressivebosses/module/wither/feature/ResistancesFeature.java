package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class ResistancesFeature extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Melee Damage reduction above half health", description = "Percentage Melee Damage Reduction (at max difficulty) while the Wither is above half health.")
	public static Double meleeDamageReductionAboveHalfHealth = 0.24d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Max Melee Damage reduction before half health", description = "Cap for 'Melee Damage reduction above half health'")
	public static Double maxMeleeDamageReductionAboveHalfHealth = 0.24d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Melee Damage reduction below half health", description = "Percentage Melee Damage Reduction (at max difficulty) as the Wither drops below half health.")
	public static Double meleeDamageReductionBelowHalfHealth = 0.48d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Max Melee Damage reduction below half health", description = "Cap for 'Melee Damage Reduction below half health'")
	public static Double maxMeleeDamageReductionBelowHalfHealth = 0.48d;
	@Config(min = 0d, max = 1024d)
	@Label(name = "Magic Damage Bonus", description = "Bonus magic damage based off missing health. 250 means that every 250 missing health the damage will be amplified by 100%. E.g. The first Wither (with 300 max health) is at 50 health (so it's missing 250hp), on magic damage he will receive 'magic_damage * (missing_health / magic_damage_bonus + 1)' = 'magic_damage * (250 / 250 + 1)' = 'magic_damage * 2'.")
	public static Double magicDamageBonus = 250d;

	public ResistancesFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onWitherDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		if ((meleeDamageReductionBelowHalfHealth == 0d || maxMeleeDamageReductionBelowHalfHealth == 0d)
				&& (meleeDamageReductionAboveHalfHealth == 0d || maxMeleeDamageReductionAboveHalfHealth == 0d)
				&& magicDamageBonus == 0d)
			return;

		//Handle Magic Damage
		if ((event.getSource().is(DamageTypes.MAGIC) || event.getSource().is(DamageTypes.INDIRECT_MAGIC)) && magicDamageBonus > 0d) {
			double missingHealth = wither.getMaxHealth() - wither.getHealth();
			event.setAmount((event.getAmount() * (float) (missingHealth / (magicDamageBonus) + 1)));
		}

		if (event.getSource().getDirectEntity() != event.getSource().getEntity())
			return;

		//Handle Damage Reduction
		float damageReduction;
		if (!wither.isPowered())
			damageReduction = (float) Math.min(maxMeleeDamageReductionAboveHalfHealth, meleeDamageReductionAboveHalfHealth * DifficultyHelper.getScalingDifficulty(wither));
		else
			damageReduction = (float) Math.min(maxMeleeDamageReductionBelowHalfHealth, meleeDamageReductionBelowHalfHealth * DifficultyHelper.getScalingDifficulty(wither));

		if (damageReduction == 0d)
			return;

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
