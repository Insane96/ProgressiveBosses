package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.monster.ElderGuardianEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Attack", description = "More damage and attack speed based off Elder Guardians Defeated")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Integer> attackDurationReductionConfig;

	public double bonusDamage = 0d;
	public int attackDurationReduction = 25;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusDamageConfig = Config.builder
				.comment("Percentage Bonus damage per defeated Elder Guardian.")
				.defineInRange("Bonus Damage per Elder Guardian Defeated", bonusDamage, 0d, 128d);
		attackDurationReductionConfig = Config.builder
				.comment("How many ticks faster will Elder Guardian attack (multiplied by defeated Elder Guardians). Vanilla Attack Duration is 60 ticks (3 secs)")
				.defineInRange("Attack Duration Reduction per Elder Guardian Defeated", attackDurationReduction, 0, 60);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusDamage = this.bonusDamageConfig.get();
		this.attackDurationReduction = this.attackDurationReductionConfig.get();
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.bonusDamage == 0d)
			return;

		if (!(event.getSource().getEntity() instanceof ElderGuardianEntity))
			return;

		ElderGuardianEntity elderGuardian = (ElderGuardianEntity) event.getSource().getEntity();

		float bonusDamage = (float) (this.bonusDamage * BaseFeature.getDeadElderGuardians(elderGuardian));

		event.setAmount(event.getAmount() * (1f + bonusDamage));
	}

	private static final int BASE_ATTACK_DURATION = 60;

	public int getAttackDuration(ElderGuardianEntity elderGuardian) {
		if (!this.isEnabled() || this.attackDurationReduction == 0)
			return BASE_ATTACK_DURATION;
		int elderGuardiansNearby = elderGuardian.level.getEntities(elderGuardian, elderGuardian.getBoundingBox().inflate(48d), entity -> entity instanceof ElderGuardianEntity).size();
		if (elderGuardiansNearby == 2)
			return BASE_ATTACK_DURATION;

		return BASE_ATTACK_DURATION - ((2 - elderGuardiansNearby) * this.attackDurationReduction);
	}
}
