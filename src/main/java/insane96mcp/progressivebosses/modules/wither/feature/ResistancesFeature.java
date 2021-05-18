package insane96mcp.progressivebosses.modules.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> damageReductionPerDifficultyOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxDamageReductionPerDifficultyOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageBonusConfig;

	public double damageReductionPerDifficultyOnHalfHealth = 4d;
	public double maxDamageReductionPerDifficultyOnHalfHealth = 32d;
	public double magicDamageBonus = 5d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		damageReductionPerDifficultyOnHalfHealthConfig = Config.builder
				.comment("Percentage Damage Resistance as the Wither drops below half health.")
				.defineInRange("Damage reduction per Difficulty below half health", damageReductionPerDifficultyOnHalfHealth, 0d, 100f);
		maxDamageReductionPerDifficultyOnHalfHealthConfig = Config.builder
				.comment("Cap for 'Damage reduction per Difficulty below half health'")
				.defineInRange("Max Damage reduction per Difficulty below half health", maxDamageReductionPerDifficultyOnHalfHealth, 0d, 100f);
		magicDamageBonusConfig = Config.builder
				.comment("Bonus magic damage based off missing health. 5 means that the Wither will receive bonus damage equal to 5% of the missing health. E.g. The difficulty = 0 Wither (with 300 max health) is at 1/3 of health (so it's missing 200hp), if he were to take magic damage he will receive 200 * 5% = 10 more damage.")
				.defineInRange("Magic Damage Bonus", magicDamageBonus, 0d, 100f);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.damageReductionPerDifficultyOnHalfHealth = this.damageReductionPerDifficultyOnHalfHealthConfig.get();
		this.maxDamageReductionPerDifficultyOnHalfHealth = this.maxDamageReductionPerDifficultyOnHalfHealthConfig.get();
		this.magicDamageBonus = this.magicDamageBonusConfig.get();
	}

	@SubscribeEvent
	public void onUpdate(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (this.damageReductionPerDifficultyOnHalfHealth == 0d || this.maxDamageReductionPerDifficultyOnHalfHealth == 0d)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		//Handle Magic Damage
		if (event.getSource().isMagicDamage() && this.magicDamageBonus > 0d) {
			double missingHealth = wither.getMaxHealth() - wither.getHealth();
			event.setAmount((event.getAmount() + (float) (this.magicDamageBonus * 0.01 * missingHealth)));
		}
		//Handle Damage Reduction
		else {
			if (!wither.isCharged())
				return;

			CompoundNBT tags = wither.getPersistentData();
			float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

			float damageReduction = (float) Math.min(this.maxDamageReductionPerDifficultyOnHalfHealth, difficulty * this.damageReductionPerDifficultyOnHalfHealth) / 100f;
			event.setAmount(event.getAmount() * (1f - damageReduction));
		}
	}
}
