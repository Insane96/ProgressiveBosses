package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Arrays;
import java.util.List;

@Label(name = "Resistances & Vulnerabilities", description = "Handles the Damage Resistances and Vulnerabilities")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class ResistancesFeature extends Feature {

	@Config(min = 0d, max = 1d)
	@Label(name = "Melee Damage reduction while at the center", description = "Melee Damage reduction at max difficulty while the Ender Dragon is at the center.")
	public static Double damageReductionWhenSitting = 0.24d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Melee Damage increase while not at the center", description = "Melee Damage is increased by this percentage while the Ender Dragon is not at the center.")
	public static Double damageIncreaseWhileNotSitting = 0.24d;
	@Config(min = 0d, max = 1d)
	@Label(name = "Explosion Damage reduction", description = "Damage reduction when hit by explosions (firework rockets excluded).")
	public static Double explosionDamageReduction = 0.667d;

	public ResistancesFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onDragonDamage(LivingDamageEvent event) {
		if (!this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon))
			return;

		meleeDamageReduction(event, dragon);
		meleeDamageIncrease(event, dragon);
		explosionDamageReduction(event, dragon);
	}

	private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> sittingPhases = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.TAKEOFF);

	private static void meleeDamageReduction(LivingDamageEvent event, EnderDragon dragon) {
		if (damageReductionWhenSitting == 0d)
			return;
		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()) && event.getSource().getDirectEntity() instanceof Player) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * (damageReductionWhenSitting * DifficultyHelper.getScalingDifficulty(dragon)))));
		}
	}

	private static void meleeDamageIncrease(LivingDamageEvent event, EnderDragon dragon) {
		if (damageIncreaseWhileNotSitting == 0d)
			return;
		if (!sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()) && event.getSource().getDirectEntity() instanceof Player) {
			event.setAmount((event.getAmount() + (float) (event.getAmount() * (damageIncreaseWhileNotSitting * DifficultyHelper.getScalingDifficulty(dragon)))));
		}
	}

	private static void explosionDamageReduction(LivingDamageEvent event, EnderDragon dragon) {
		if (explosionDamageReduction == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks")) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * explosionDamageReduction)));
		}
	}
}
