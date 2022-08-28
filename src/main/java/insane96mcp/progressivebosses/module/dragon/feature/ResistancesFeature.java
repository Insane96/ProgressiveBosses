package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
public class ResistancesFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> damageRedutionWhenSittingConfig;
	private final ForgeConfigSpec.ConfigValue<Double> explosionDamageReductionConfig;

	public double damageRedutionWhenSitting = 0.60d;
	public double explosionDamageReduction = 0.667d;

	public ResistancesFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		damageRedutionWhenSittingConfig = Config.builder
				.comment("Melee Damage reduction at max difficulty while the Ender Dragon is at the center.")
				.defineInRange("Melee Damage reduction while at the center", damageRedutionWhenSitting, 0d, Double.MAX_VALUE);
		explosionDamageReductionConfig = Config.builder
				.comment("Damage reduction when hit by explosions (firework rockets excluded).")
				.defineInRange("Explosion Damage reduction", explosionDamageReduction, 0d, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.damageRedutionWhenSitting = this.damageRedutionWhenSittingConfig.get();
		this.explosionDamageReduction = this.explosionDamageReductionConfig.get();
	}

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		meleeDamageReduction(event, dragon);
		explosionDamageReduction(event, dragon);
	}

	private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> sittingPhases = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.TAKEOFF);

	private void meleeDamageReduction(LivingDamageEvent event, EnderDragon dragon) {
		if (this.damageRedutionWhenSitting == 0d)
			return;
		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()) && event.getSource().getDirectEntity() instanceof Player) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * (this.damageRedutionWhenSitting * DifficultyHelper.getScalingDifficulty(dragon)))));
		}
	}

	private void explosionDamageReduction(LivingDamageEvent event, EnderDragon dragon) {
		if (this.explosionDamageReduction == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks")) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * this.explosionDamageReduction)));
		}
	}
}
