package insane96mcp.progressivebosses.module.wither.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Bonus regeneration. The feature even fixes the Wither health bar not updating on spawn.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "wither")
public class HealthFeature extends Feature {
	@Config(min = 0d)
	@Label(name = "Health Bonus per Difficulty", description = "Increase Wither's Health by this value at max difficulty (scales accordingly at lower difficulties)")
	public static Double bonusHealth = 720d;
	@Config(min = 0d)
	@Label(name = "Maximum Bonus Regeneration", description = """
						Maximum bonus regeneration per second given by "Bonus Regeneration".
						Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second).
						Note that this bonus health regen is disabled when Wither's health is between 49% and 50% to prevent making it impossible to approach when reaches half health.""")
	public static Double maxBonusRegen = 2d;
	@Config(min = 0d)
	@Label(name = "Bonus Regeneration", description = "How many half hearts will the Wither regen at max difficulty. This is added to the natural regeneration of the Wither (1 Health per Second).")
	public static Double bonusRegen = 2.4d;
	@Config(min = 0d, max = 2d)
	@Label(name = "Bonus Regeneration Ratio When Hit", description = "Bonus regeneration will be multiplied by this ratio when the Wither has been hit in the last 'Bonus Regeneration When Hit Duration' ticks.")
	public static Double bonusRegenRatioWhenHit = 0.6d;
	@Config(min = 0)
	@Label(name = "Bonus Regeneration When Hit Duration")
	public static Integer bonusRegenerationWhenHitDuration = 60;

	public HealthFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		//noinspection ConstantConditions
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| bonusHealth == 0d
				|| !(event.getEntity() instanceof WitherBoss wither)
				|| wither.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		MCUtils.applyModifier(wither, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, bonusHealth * DifficultyHelper.getScalingDifficulty(wither), AttributeModifier.Operation.ADDITION);
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level.isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof WitherBoss wither)
				|| bonusRegen == 0d
				|| maxBonusRegen == 0d
				|| wither.getInvulnerableTicks() > 0
				|| !wither.isAlive())
			return;

		//Disable bonus health regen when health between 49% and 50%
		if (wither.getHealth() / wither.getMaxHealth() > 0.49f && wither.getHealth() / wither.getMaxHealth() < 0.50f)
			return;

		float heal = (float) Math.min(bonusRegen * DifficultyHelper.getScalingDifficulty(wither), maxBonusRegen);
		heal /= 20f;

		if (wither.tickCount - wither.getLastHurtByMobTimestamp() <= bonusRegenerationWhenHitDuration)
			heal *= bonusRegenRatioWhenHit;

		if (heal > 0f)
			wither.heal(heal);
	}
}
