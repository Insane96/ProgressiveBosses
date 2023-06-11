package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Attack", description = "More damage and attack speed based off Elder Guardians Defeated")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class AttackFeature extends Feature {
	@Config(min = 0d, max = 128d)
	@Label(name = "Bonus Damage per Elder Guardian Defeated", description = "Percentage Bonus damage per defeated Elder Guardian.")
	public static Double bonusDamage = 0d;
	@Config(min = 0, max = 60)
	@Label(name = "Attack Duration Reduction per Elder Guardian Defeated", description = "How many ticks faster will Elder Guardian attack (multiplied by defeated Elder Guardians). Vanilla Attack Duration is 60 ticks (3 secs)")
	public static Integer attackDurationReduction = 25;

	public AttackFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| bonusDamage == 0d
				|| !(event.getSource().getEntity() instanceof ElderGuardian elderGuardian))
			return;

		float bonusDmg = (float) (bonusDamage * elderGuardian.getPersistentData().getInt(Strings.Tags.DIFFICULTY));

		event.setAmount(event.getAmount() * (1f + bonusDmg));
	}

	private static final int BASE_ATTACK_DURATION = 60;

	public static int getAttackDuration(ElderGuardian elderGuardian) {
		if (!isEnabled(AttackFeature.class) || attackDurationReduction == 0)
			return BASE_ATTACK_DURATION;
		int elderGuardiansNearby = elderGuardian.level().getEntities(elderGuardian, elderGuardian.getBoundingBox().inflate(48d), entity -> entity instanceof ElderGuardian).size();
		if (elderGuardiansNearby == 2)
			return BASE_ATTACK_DURATION;

		return BASE_ATTACK_DURATION - ((2 - elderGuardiansNearby) * attackDurationReduction);
	}
}
