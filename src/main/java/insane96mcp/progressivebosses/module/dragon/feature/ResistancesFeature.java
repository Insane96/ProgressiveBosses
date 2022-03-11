package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
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

	private final ForgeConfigSpec.ConfigValue<Double> bonusCurHealthDirectDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusCurHealthIndirectDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> damageRedutionWhenSittingConfig;
	private final ForgeConfigSpec.ConfigValue<Double> explosionDamageReductionConfig;

	public double bonusCurHealthDirectDamage = 0.03d;
	public double bonusCurHealthIndirectDamage = 0.01d;
	public double damageRedutionWhenSitting = 0.0125d;
	public double explosionDamageReduction = 0.667d;

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

		if (!(event.getEntity() instanceof EnderDragon dragon))
			return;

		bonusDamageWhenRespawningCrystals(event, dragon);
		bonusDamageNotInCenter(event, dragon);
		meleeDamageReduction(event, dragon);
		explosionDamageReduction(event, dragon);
	}

	private void bonusDamageWhenRespawningCrystals(LivingDamageEvent event, EnderDragon dragon) {
		if (this.bonusCurHealthDirectDamage == 0d && this.bonusCurHealthIndirectDamage == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks"))
			return;

		if (!dragon.getPhaseManager().getCurrentPhase().getPhase().equals(CrystalRespawnPhase.getPhaseType()))
			return;

		float curHealthPerc = dragon.getHealth() / dragon.getMaxHealth();
		event.setAmount(event.getAmount() * (1.25f + curHealthPerc));
	}

	private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> sittingPhases = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.TAKEOFF);

	private void bonusDamageNotInCenter(LivingDamageEvent event, EnderDragon dragon) {
		if (this.bonusCurHealthDirectDamage == 0d && this.bonusCurHealthIndirectDamage == 0d)
			return;

		if (event.getSource().isExplosion() && !event.getSource().getMsgId().equals("fireworks"))
			return;

		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
			return;

		float curHealth = dragon.getHealth();
		if (event.getSource().getDirectEntity() instanceof Player) {
			event.setAmount(event.getAmount() + (float) (curHealth * this.bonusCurHealthDirectDamage));
		}
		else if (event.getSource().getEntity() instanceof Player) {
			event.setAmount(event.getAmount() + (float) (curHealth * this.bonusCurHealthIndirectDamage));
		}
	}

	private void meleeDamageReduction(LivingDamageEvent event, EnderDragon dragon) {
		if (this.damageRedutionWhenSitting == 0d)
			return;

		CompoundTag compoundNBT = dragon.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		if (sittingPhases.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()) && event.getSource().getDirectEntity() instanceof Player) {
			event.setAmount((event.getAmount() - (float) (event.getAmount() * (this.damageRedutionWhenSitting * difficulty))));
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
