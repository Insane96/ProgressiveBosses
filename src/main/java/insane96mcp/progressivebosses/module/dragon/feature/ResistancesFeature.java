package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusCurHealthDirectDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusCurHealthIndirectDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> damageRedutionWhenSittingConfig;
	private final ForgeConfigSpec.ConfigValue<Double> explosionDamageReductionConfig;

	public double bonusCurHealthDirectDamage = 0.02d;
	public double bonusCurHealthIndirectDamage = 0.008d;
	public double damageRedutionWhenSitting = 0.0125d;
	public double explosionDamageReduction = 0.50d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusCurHealthDirectDamageConfig = Config.builder
				.comment("Percentage of Dragon's current health dealth as Bonus damage when attacked directly (melee) and she's not at the center.")
				.defineInRange("Bonus Current Health Direct Damage", bonusCurHealthDirectDamage, 0d, Double.MAX_VALUE);
		bonusCurHealthIndirectDamageConfig = Config.builder
				.comment("Percentage of Dragon's current health dealth as Bonus damage when attacked indirectly (e.g. arrows) and she's not at the center.")
				.defineInRange("Bonus Current Health Indirect Damage", bonusCurHealthIndirectDamage, 0d, Double.MAX_VALUE);
		damageRedutionWhenSittingConfig = Config.builder
				.comment("Melee Damage reduction per difficulty while the Ender Dragon is at the center.")
				.defineInRange("Melee Damage reduction while at the center", damageRedutionWhenSitting, 0d, Double.MAX_VALUE);
		explosionDamageReductionConfig = Config.builder
				.comment("Damage reduction when hit by explosions.")
				.defineInRange("Explosion Damage reduction", explosionDamageReduction, 0d, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusCurHealthDirectDamage = this.bonusCurHealthDirectDamageConfig.get();
		this.bonusCurHealthIndirectDamage = this.bonusCurHealthIndirectDamageConfig.get();
		this.damageRedutionWhenSitting = this.damageRedutionWhenSittingConfig.get();
		this.explosionDamageReduction = this.explosionDamageReductionConfig.get();
	}

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();

		bonusDamageNotInCenter(event, dragon);
		meleeDamageReduction(event, dragon);
		explosionDamageReduction(event, dragon);
	}

	private static final List<PhaseType<? extends IPhase>> sittingPhases = Arrays.asList(PhaseType.SITTING_SCANNING, PhaseType.SITTING_ATTACKING, PhaseType.SITTING_FLAMING, PhaseType.TAKEOFF);

	private void bonusDamageNotInCenter(LivingDamageEvent event, EnderDragonEntity dragon) {
		if (this.bonusCurHealthDirectDamage == 0d && this.bonusCurHealthIndirectDamage == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks"))
			return;

		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
			return;

		float curHealth = dragon.getHealth();
		if (event.getSource().getDirectEntity() instanceof PlayerEntity) {
			event.setAmount(event.getAmount() + (float) (curHealth * this.bonusCurHealthDirectDamage));
		}
		else if (event.getSource().getEntity() instanceof PlayerEntity) {
			event.setAmount(event.getAmount() + (float) (curHealth * this.bonusCurHealthIndirectDamage));
		}
	}

	private void meleeDamageReduction(LivingDamageEvent event, EnderDragonEntity dragon) {
		if (this.damageRedutionWhenSitting == 0d)
			return;

		CompoundNBT compoundNBT = dragon.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()) && event.getSource().getDirectEntity() instanceof PlayerEntity) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * (this.damageRedutionWhenSitting * difficulty))));
		}
	}

	private void explosionDamageReduction(LivingDamageEvent event, EnderDragonEntity dragon) {
		if (this.explosionDamageReduction == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks")) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * this.explosionDamageReduction)));
		}
	}
}
