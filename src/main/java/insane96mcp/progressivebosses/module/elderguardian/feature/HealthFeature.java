package insane96mcp.progressivebosses.module.elderguardian.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Label(name = "Health", description = "Bonus Health and Health regeneration.")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "elder_guardian")
public class HealthFeature extends Feature {

	@Config(min = 0d)
	@Label(name = "Health Bonus per Difficulty", description = "Increase Elder Guardians' Health by this percentage (1 = +100% health)")
	public static Double bonusHealth = 0.5d;
	@Config(min = 0d)
	@Label(name = "Absorption Health", description = "Adds absorption health to Elder Guradians (health that doesn't regen)")
	public static Double absorptionHealth = 40d;
	@Config(min = 0d)
	@Label(name = "Health Regen", description = "Health Regen per second")
	public static Double healthRegen = 0.5d;

	public HealthFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide
				|| !this.isEnabled()
				|| bonusHealth == 0d && absorptionHealth == 0d
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian))
			return;

		CompoundTag nbt = elderGuardian.getPersistentData();
		if (nbt.contains(Strings.Tags.DIFFICULTY))
			return;

		if (bonusHealth > 0d) {
			//noinspection ConstantConditions
			if (elderGuardian.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
				return;
			MCUtils.applyModifier(elderGuardian, Attributes.MAX_HEALTH, Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, bonusHealth, AttributeModifier.Operation.MULTIPLY_BASE);
		}

		if (absorptionHealth > 0d)
			elderGuardian.setAbsorptionAmount(absorptionHealth.floatValue());
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingTickEvent event) {
		if (event.getEntity().level().isClientSide
				|| !this.isEnabled()
				|| !(event.getEntity() instanceof ElderGuardian elderGuardian)
				|| healthRegen == 0d
				|| !elderGuardian.isAlive())
			return;

		// divided by 20 because is the health regen per second and here I need per tick
		float heal = healthRegen.floatValue() / 20f;
		elderGuardian.heal(heal);
	}
}
