package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Resistances", description = "Handles the Damage Resistances")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class ResistancesFeature extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Damage Reduction per Elder Guardian Defeated", description = "Percentage Damage Reduction for each Elder Guardian Defeated.")
	public static Double resistancePerElderGuardianDefeated = 0.3d;

	public ResistancesFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onElderGuardianDamage(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| resistancePerElderGuardianDefeated == 0d
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		float damageReduction = (float) (resistancePerElderGuardianDefeated * elderGuardian.getPersistentData().getInt(Strings.Tags.DIFFICULTY));
		//Cap damage reduction due to stupid mods messing with the system
		if (damageReduction > 0.95f)
			damageReduction = 0.95f;

		event.setAmount(event.getAmount() * (1f - damageReduction));
	}
}
