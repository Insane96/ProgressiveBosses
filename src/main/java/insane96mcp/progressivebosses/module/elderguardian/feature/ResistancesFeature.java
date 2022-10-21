package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances", description = "Handles the Damage Resistances")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> resistancePerElderGuardianDefeatedConfig;

	public double resistancePerElderGuardianDefeated = 0.3d;

	public ResistancesFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
		this.pushConfig(Config.builder);
		resistancePerElderGuardianDefeatedConfig = Config.builder
				.comment("Percentage Damage Reduction for each Elder Guardian Defeated.")
				.defineInRange("Damage Reduction per Elder Guardian Defeated", resistancePerElderGuardianDefeated, 0d, 1d);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.resistancePerElderGuardianDefeated = this.resistancePerElderGuardianDefeatedConfig.get();
	}

	@SubscribeEvent
	public void onElderGuardianDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (this.resistancePerElderGuardianDefeated == 0d)
			return;

		if (!(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		float damageReduction = (float) (this.resistancePerElderGuardianDefeated * elderGuardian.getPersistentData().getInt(Strings.Tags.DIFFICULTY));

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
