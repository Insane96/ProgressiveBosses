package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class HealthFeature extends Feature {

	@Config(min = 0)
	@Label(name = "Health Bonus at Max Difficulty", description = "Ender Dragon health will be increased by this value at max difficulty (scaling accordingly at lower difficulties)")
	public static Double bonusHealth = 200d;
	@Config(min = 0)
	@Label(name = "Bonus Regeneration", description = "How much health will the Ender Dragon regen at max difficulty (scaling accordingly at lower difficulties). This doesn't affect the health regen given by crystals.")
	public static Double bonusRegen = 1.0d;
	@Config(min = 0)
	@Label(name = "Maximum Bonus Regeneration", description = "Maximum bonus regeneration per second given by \"Bonus Regeneration\". Set to 0 to disable bonus health regeneration. Can be lower than \"Bonus Regeneration\". This doesn't affect the health regen given by crystals.")
	public static Double maxBonusRegen = 1.0d;
	@Config(min = 0)
	@Label(name = "Bonus Crystal Regeneration", description = "How much health (when missing 100% health) will the Ender Dragon regen at max difficulty each second whenever she's attached to a Crystal. So if she's missing 30% health, this will be 30% effective. This is added to the normal Crystal regen.")
	public static Double bonusCrystalRegen = 0d;
	@Config(min = 0d, max = 2d)
	@Label(name = "Bonus Regeneration Ratio When Hit", description = "Bonus regeneration (also bonus crystal regen) will be multiplied by this ratio when the Dragon has been hit in the last 3 seconds.")
	public static Double bonusRegenRatioWhenHit = 0.4d;

	public HealthFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		//noinspection ConstantConditions
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| bonusHealth == 0d
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| dragon.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		MCUtils.applyModifier(dragon, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, bonusHealth * DifficultyHelper.getScalingDifficulty(dragon), AttributeModifier.Operation.ADDITION);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof EnderDragon dragon)
				|| !dragon.isAlive()
				|| dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING)
			return;

		CompoundTag tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		float flatBonusHeal = (float) Math.min(bonusRegen * DifficultyHelper.getScalingDifficulty(dragon), maxBonusRegen);
		float crystalBonusHeal = getCrystalBonusHeal(dragon);

		float heal = flatBonusHeal + crystalBonusHeal;
		if (heal == 0f)
			return;

		heal /= 20f;

		if (dragon.tickCount - dragon.getLastHurtByMobTimestamp() <= 60) // 3 seconds
			heal *= bonusRegenRatioWhenHit;

		dragon.heal(heal);
	}

	private static float getCrystalBonusHeal(EnderDragon dragon) {
		if (bonusCrystalRegen == 0d
				|| dragon.nearestCrystal == null
				|| !dragon.nearestCrystal.isAlive())
			return 0f;

		double currHealthPerc = 1 - (dragon.getHealth() / dragon.getMaxHealth());
		return (float) (bonusCrystalRegen * DifficultyHelper.getScalingDifficulty(dragon) * currHealthPerc);
	}
}
