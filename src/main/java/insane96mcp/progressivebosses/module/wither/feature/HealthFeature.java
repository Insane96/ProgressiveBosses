package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration. The feature even fixes the Wither health bar not updating on spawn.")
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenPerDifficultyConfig;

	public double bonusPerDifficulty = 90d;
	public double maxBonusRegen = 2d;
	public double bonusRegenPerDifficulty = 0.3d;

	public HealthFeature(Module module) {
		super(Config.builder, module);
		this.pushConfig(Config.builder);
		bonusPerDifficultyConfig = Config.builder
				.comment("Increase Wither's Health by this value per difficulty")
				.defineInRange("Health Bonus per Difficulty", bonusPerDifficulty, 0.0, Double.MAX_VALUE);
		maximumBonusRegenConfig = Config.builder
				.comment("""
						Maximum bonus regeneration per second given by "Bonus Regeneration per Difficulty".
						Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second).
						Note that the health regen is disabled when Wither's health is between 49% and 50% to prevent making it impossible to approach when half health.""")
				.defineInRange("Maximum Bonus Regeneration", maxBonusRegen, 0.0, Double.MAX_VALUE);
		bonusRegenPerDifficultyConfig = Config.builder
				.comment("How many half hearts will the Wither regen per difficulty. This is added to the natural regeneration of the Wither (1 Health per Second).")
				.defineInRange("Bonus Regeneration per Difficulty", bonusRegenPerDifficulty, 0.0, Double.MAX_VALUE);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		bonusPerDifficulty = bonusPerDifficultyConfig.get();
		maxBonusRegen = maximumBonusRegenConfig.get();
		bonusRegenPerDifficulty = bonusRegenPerDifficultyConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (this.bonusPerDifficulty == 0d)
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		if (wither.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		CompoundTag witherTags = wither.getPersistentData();
		double difficulty = witherTags.getFloat(Strings.Tags.DIFFICULTY);
		MCUtils.applyModifier(wither, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, difficulty * this.bonusPerDifficulty, AttributeModifier.Operation.ADDITION);

		boolean hasInvulTicks = wither.getInvulnerableTicks() > 0;

		if (hasInvulTicks)
			wither.setHealth(Math.max(1, wither.getMaxHealth() - 200));
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level.isClientSide)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof WitherBoss wither))
			return;

		if (this.bonusRegenPerDifficulty == 0d || this.maxBonusRegen == 0d)
			return;

		//fixInvulBossBar(wither);

		if (wither.getInvulnerableTicks() > 0 || !wither.isAlive())
			return;

		//Disable bonus health regen when health between 49% and 50%
		if (wither.getHealth() / wither.getMaxHealth() > 0.49f && wither.getHealth() / wither.getMaxHealth() < 0.50f)
			return;

		CompoundTag tags = wither.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		float heal = (float) Math.min(difficulty * this.bonusRegenPerDifficulty, this.maxBonusRegen);

		heal /= 20f;

		wither.heal(heal);
	}


	private void fixInvulBossBar(WitherBoss wither) {
		if (wither.getInvulnerableTicks() == 0)
			return;

		wither.bossEvent.setProgress(wither.getHealth() / wither.getMaxHealth());
	}
}
