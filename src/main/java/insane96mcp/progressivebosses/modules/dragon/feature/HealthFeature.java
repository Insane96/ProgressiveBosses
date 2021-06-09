package insane96mcp.progressivebosses.modules.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration.")
//TODO Maybe disable this if crystals respawn is added
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenPerDifficultyConfig;

	public double bonusPerDifficulty = 10d;
	public double maxBonusRegen = 0.5d;
	public double bonusRegenPerDifficulty = 0.025d;

	public HealthFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		bonusPerDifficultyConfig = Config.builder
				.comment("Increase Ender Dragon's Health by this value per difficulty")
				.defineInRange("Health Bonus per Difficulty", bonusPerDifficulty, 0.0, Double.MAX_VALUE);
		maximumBonusRegenConfig = Config.builder
				.comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the crystal regeneration of the Ender Dragon.")
				.defineInRange("Maximum Bonus Regeneration", maxBonusRegen, 0.0, Double.MAX_VALUE);
		bonusRegenPerDifficultyConfig = Config.builder
				.comment("How much health will the Ender Dragon regen per difficulty. This doesn't affect the crystal regeneration of the Ender Dragon.")
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
	public void onSpawn(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (this.bonusPerDifficulty == 0d)
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity enderDragon = (EnderDragonEntity) event.getEntity();

		if (enderDragon.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		CompoundNBT dragonTags = enderDragon.getPersistentData();
		double difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		ModifiableAttributeInstance health = enderDragon.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier modifier = new AttributeModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, difficulty * this.bonusPerDifficulty, AttributeModifier.Operation.ADDITION);
		health.applyPersistentModifier(modifier);
		enderDragon.setHealth(enderDragon.getMaxHealth());
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		if (this.bonusRegenPerDifficulty == 0d || this.maxBonusRegen == 0d)
			return;

		EnderDragonEntity enderDragon = (EnderDragonEntity) event.getEntity();

		CompoundNBT tags = enderDragon.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		if (enderDragon.getHealth() <= 0f)
			return;

		float heal = (float) Math.min(difficulty * this.bonusRegenPerDifficulty, this.maxBonusRegen);

		heal /= 20f;

		enderDragon.heal(heal);
	}
}
