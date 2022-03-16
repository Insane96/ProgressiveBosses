package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.MCUtils;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration.")
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusCrystalRegenConfig;

	public double bonusPerDifficulty = 30d;
	public double maxBonusRegen = 1.0d;
	public double bonusRegenPerDifficulty = 0.125d;
	public double bonusCrystalRegen = 0d;

	public HealthFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusPerDifficultyConfig = Config.builder
				.comment("Increase Ender Dragon's Health by this value per difficulty")
				.defineInRange("Health Bonus per Difficulty", bonusPerDifficulty, 0.0, Double.MAX_VALUE);
		maximumBonusRegenConfig = Config.builder
				.comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the crystal regeneration of the Ender Dragon.")
				.defineInRange("Maximum Bonus Regeneration", maxBonusRegen, 0.0, Double.MAX_VALUE);
		bonusRegenPerDifficultyConfig = Config.builder
				.comment("How much health will the Ender Dragon regen per difficulty. This is added to the noaml Crystal regeneration.")
				.defineInRange("Bonus Regeneration per Difficulty", bonusRegenPerDifficulty, 0.0, Double.MAX_VALUE);
		this.bonusCrystalRegenConfig = Config.builder
				.comment("How much health (when missing 100% health) will the Ender Dragon regen per difficulty each second whenever she's attached to a Crystal. So if she's missing 30% health, this will be 30% effective. This is added to the normal Crystal regen.")
				.defineInRange("Bonus Crystal Regeneration", this.bonusCrystalRegen, 0.0, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.bonusPerDifficulty = this.bonusPerDifficultyConfig.get();
		this.maxBonusRegen = this.maximumBonusRegenConfig.get();
		this.bonusRegenPerDifficulty = this.bonusRegenPerDifficultyConfig.get();
		this.bonusCrystalRegen = this.bonusCrystalRegenConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.bonusPerDifficulty == 0d)
			return;

		if (!(event.getEntity() instanceof EnderDragon enderDragon))
			return;

		if (enderDragon.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		CompoundTag dragonTags = enderDragon.getPersistentData();
		double difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		MCUtils.applyModifier(enderDragon, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, difficulty * this.bonusPerDifficulty, AttributeModifier.Operation.ADDITION);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragon enderDragon))
			return;

		if (!enderDragon.isAlive() || enderDragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING)
			return;

		CompoundTag tags = enderDragon.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		float flatBonusHeal = getFlatBonusHeal(difficulty);
		float crystalBonusHeal = getCrystalBonusHeal(enderDragon, difficulty);

		float heal = flatBonusHeal + crystalBonusHeal;
		if (heal == 0f)
			return;

		heal /= 20f;

		enderDragon.heal(heal);
	}

	private float getFlatBonusHeal(float difficulty) {
		if (this.bonusRegenPerDifficulty == 0d || this.maxBonusRegen == 0d)
			return 0f;
		return (float) Math.min(difficulty * this.bonusRegenPerDifficulty, this.maxBonusRegen);
	}

	private float getCrystalBonusHeal(EnderDragon enderDragon, float difficulty) {
		if (this.bonusCrystalRegen == 0d)
			return 0f;

		if (enderDragon.nearestCrystal == null || !enderDragon.nearestCrystal.isAlive())
			return 0f;

		double currHealthPerc = 1 - (enderDragon.getHealth() / enderDragon.getMaxHealth());
		return (float) (this.bonusCrystalRegen * difficulty * currHealthPerc);
	}
}
