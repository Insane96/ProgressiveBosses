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

	private final ForgeConfigSpec.ConfigValue<Double> damageReductionBeforeHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxDamageReductionBeforeHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> damageReductionOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxDamageReductionOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageBonusConfig;

	public double damageReductionBeforeHalfHealth = 0.01d;
	public double maxDamageReductionBeforeHalfHealth = 0.15d;
	public double damageReductionOnHalfHealth = 0.02d;
	public double maxDamageReductionOnHalfHealth = 0.36d;
	public double magicDamageBonus = 200d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		damageReductionBeforeHalfHealthConfig = Config.builder
				.comment("Percentage Damage Reduction while the Wither is above half health.")
				.defineInRange("Damage reduction per Difficulty above half health", damageReductionBeforeHalfHealth, 0d, 1d);
		maxDamageReductionBeforeHalfHealthConfig = Config.builder
				.comment("Cap for 'Damage reduction per Difficulty above half health'")
				.defineInRange("Max Damage reduction per Difficulty before half health", maxDamageReductionBeforeHalfHealth, 0d, 1d);
		damageReductionOnHalfHealthConfig = Config.builder
				.comment("Percentage Damage Reduction as the Wither drops below half health.")
				.defineInRange("Damage reduction per Difficulty below half health", damageReductionOnHalfHealth, 0d, 1d);
		maxDamageReductionOnHalfHealthConfig = Config.builder
				.comment("Cap for 'Damage Reduction per Difficulty below half health'")
				.defineInRange("Max Damage reduction per Difficulty below half health", maxDamageReductionOnHalfHealth, 0d, 1d);
		magicDamageBonusConfig = Config.builder
				.comment("Bonus magic damage based off missing health. 150 means that every 150 missing health the damage will be amplified by 100%. E.g. The difficulty = 0 Wither (with 300 max health) is at half health (so it's missing 150hp), on magic damage he will receive 'magic_damage * (missing_health / magic_damage_bonus + 1)' = 'magic_damage * (150 / 150 + 1)' = 'magic_damage * 2'.")
				.defineInRange("Magic Damage Bonus", magicDamageBonus, 0d, 1024f);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.damageReductionBeforeHalfHealth = this.damageReductionBeforeHalfHealthConfig.get();
		this.maxDamageReductionBeforeHalfHealth = this.maxDamageReductionBeforeHalfHealthConfig.get();
		this.damageReductionOnHalfHealth = this.damageReductionOnHalfHealthConfig.get();
		this.maxDamageReductionOnHalfHealth = this.maxDamageReductionOnHalfHealthConfig.get();
		this.magicDamageBonus = this.magicDamageBonusConfig.get();
	}

	@SubscribeEvent
	public void onWitherDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if ((this.damageReductionOnHalfHealth == 0d || this.maxDamageReductionOnHalfHealth == 0d) && (this.damageReductionBeforeHalfHealth == 0d || this.maxDamageReductionBeforeHalfHealth == 0d) && this.magicDamageBonus == 0d)
			return;

		if (!(event.getEntity() instanceof WitherEntity))
			return;

		WitherEntity wither = (WitherEntity) event.getEntity();
		//Handle Magic Damage
		if (event.getSource().isMagicDamage() && this.magicDamageBonus > 0d) {
			double missingHealth = wither.getMaxHealth() - wither.getHealth();
			event.setAmount((event.getAmount() * (float) (missingHealth / (this.magicDamageBonus) + 1)));
		}

		CompoundNBT tags = wither.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		//Handle Damage Reduction
		float damageReduction;
		if (!wither.isCharged())
			damageReduction = (float) Math.min(this.maxDamageReductionBeforeHalfHealth, difficulty * this.damageReductionBeforeHalfHealth);
		else
			damageReduction = (float) Math.min(this.maxDamageReductionOnHalfHealth, difficulty * this.damageReductionOnHalfHealth);

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
