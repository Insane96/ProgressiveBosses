package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances", description = "Handles the Damage Resistances")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> resistancePerElderGuardianDefeatedConfig;

	public double resistancePerElderGuardianDefeated = 0.3d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
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

		if (!(event.getEntity() instanceof ElderGuardian))
			return;

		ElderGuardian elderGuardian = (ElderGuardian) event.getEntity();

		float damageReduction = (float) (BaseFeature.getDeadElderGuardians(elderGuardian) * this.resistancePerElderGuardianDefeated);

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
