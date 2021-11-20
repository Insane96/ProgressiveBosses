package insane96mcp.progressivebosses.module.wither.feature;

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

	private final ForgeConfigSpec.ConfigValue<Double> meleeDamageReductionBeforeHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxMeleeDamageReductionBeforeHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> meleeDamageReductionOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxMeleeDamageReductionOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageBonusConfig;

	public double meleeDamageReductionBeforeHalfHealth = 0.01d;
	public double maxMeleeDamageReductionBeforeHalfHealth = 0.24d;
	public double meleeDamageReductionOnHalfHealth = 0.02d;
	public double maxDamageReductionOnHalfHealth = 0.48d;
	public double magicDamageBonus = 250d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		meleeDamageReductionBeforeHalfHealthConfig = Config.builder
				.comment("Percentage Melee Damage Reduction (per difficulty) while the Wither is above half health.")
				.defineInRange("Melee Damage reduction per Difficulty above half health", meleeDamageReductionBeforeHalfHealth, 0d, 1d);
		maxMeleeDamageReductionBeforeHalfHealthConfig = Config.builder
				.comment("Cap for 'Melee Damage reduction per Difficulty above half health'")
				.defineInRange("Max Melee Damage reduction per Difficulty before half health", maxMeleeDamageReductionBeforeHalfHealth, 0d, 1d);
		meleeDamageReductionOnHalfHealthConfig = Config.builder
				.comment("Percentage Melee Damage Reduction (per difficulty) as the Wither drops below half health.")
				.defineInRange("Melee Damage reduction per Difficulty below half health", meleeDamageReductionOnHalfHealth, 0d, 1d);
		maxMeleeDamageReductionOnHalfHealthConfig = Config.builder
				.comment("Cap for 'Melee Damage Reduction per Difficulty below half health'")
				.defineInRange("Max Melee Damage reduction per Difficulty below half health", maxDamageReductionOnHalfHealth, 0d, 1d);
		magicDamageBonusConfig = Config.builder
				.comment("Bonus magic damage based off missing health. 150 means that every 150 missing health the damage will be amplified by 100%. E.g. The difficulty = 0 Wither (with 300 max health) is at half health (so it's missing 150hp), on magic damage he will receive 'magic_damage * (missing_health / magic_damage_bonus + 1)' = 'magic_damage * (150 / 150 + 1)' = 'magic_damage * 2'.")
				.defineInRange("Magic Damage Bonus", magicDamageBonus, 0d, 1024f);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.meleeDamageReductionBeforeHalfHealth = this.meleeDamageReductionBeforeHalfHealthConfig.get();
		this.maxMeleeDamageReductionBeforeHalfHealth = this.maxMeleeDamageReductionBeforeHalfHealthConfig.get();
		this.meleeDamageReductionOnHalfHealth = this.meleeDamageReductionOnHalfHealthConfig.get();
		this.maxDamageReductionOnHalfHealth = this.maxMeleeDamageReductionOnHalfHealthConfig.get();
		this.magicDamageBonus = this.magicDamageBonusConfig.get();
	}

	@SubscribeEvent
	public void onWitherDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if ((this.meleeDamageReductionOnHalfHealth == 0d || this.maxDamageReductionOnHalfHealth == 0d) && (this.meleeDamageReductionBeforeHalfHealth == 0d || this.maxMeleeDamageReductionBeforeHalfHealth == 0d) && this.magicDamageBonus == 0d)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		//Handle Magic Damage
		if (event.getSource().isMagic() && this.magicDamageBonus > 0d) {
			double missingHealth = wither.getMaxHealth() - wither.getHealth();
			event.setAmount((event.getAmount() * (float) (missingHealth / (this.magicDamageBonus) + 1)));
		}

		if (event.getSource().getDirectEntity() != event.getSource().getEntity())
			return;

		CompoundNBT tags = wither.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);
		//Handle Damage Reduction
		float damageReduction;
		if (!wither.isPowered())
			damageReduction = (float) Math.min(this.maxMeleeDamageReductionBeforeHalfHealth, difficulty * this.meleeDamageReductionBeforeHalfHealth);
		else
			damageReduction = (float) Math.min(this.maxDamageReductionOnHalfHealth, difficulty * this.meleeDamageReductionOnHalfHealth);

		if (damageReduction == 0d)
			return;

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
