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

@Label(name = "Resistances & Weaknesses", description = "Handles Damage Taken Reduction and Increase")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> damageReductionPerDifficultyOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxDamageReductionPerDifficultyOnHalfHealthConfig;
	private final ForgeConfigSpec.ConfigValue<Double> magicDamageMultiplierConfig;

	public double damageReductionPerDifficultyOnHalfHealth = 4d;
	public double maxDamageReductionPerDifficultyOnHalfHealth = 32d;
	public double magicDamageMultiplier = 3d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		damageReductionPerDifficultyOnHalfHealthConfig = Config.builder
				.comment("Percentage Damage Resistance as the Wither drops below half health.")
				.defineInRange("Damage reduction per Difficulty below half health", damageReductionPerDifficultyOnHalfHealth, 0d, 100f);
		maxDamageReductionPerDifficultyOnHalfHealthConfig = Config.builder
				.comment("Cap for 'Damage reduction per Difficulty below half health'")
				.defineInRange("Max Damage reduction per Difficulty below half health", maxDamageReductionPerDifficultyOnHalfHealth, 0d, 100f);
		magicDamageMultiplierConfig = Config.builder
				.comment("When the wither receives damage, if magic, will be multiplied by this value")
				.defineInRange("Magic Damage Multiplier", magicDamageMultiplier, 0d, 100f);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.damageReductionPerDifficultyOnHalfHealth = this.damageReductionPerDifficultyOnHalfHealthConfig.get();
		this.maxDamageReductionPerDifficultyOnHalfHealth = this.maxDamageReductionPerDifficultyOnHalfHealthConfig.get();
		this.magicDamageMultiplier = this.magicDamageMultiplierConfig.get();
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

		if (event.getSource().isMagicDamage()) {
			event.setAmount(event.getAmount() * (float)this.magicDamageMultiplier);
		}
		else {
			if (wither.getHealth() > wither.getMaxHealth() / 2f)
				return;

			CompoundNBT tags = wither.getPersistentData();
			float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

			float damageReduction = (float) Math.min(this.maxDamageReductionPerDifficultyOnHalfHealth, difficulty * this.damageReductionPerDifficultyOnHalfHealth) / 100f;
			event.setAmount(event.getAmount() * (1f - damageReduction));
		}
	}
}
