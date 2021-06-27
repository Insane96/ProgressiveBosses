package insane96mcp.progressivebosses.module.dragon.feature;

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
public class HealthFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> bonusPerDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maximumBonusRegenConfig;
	private final ForgeConfigSpec.ConfigValue<Double> bonusRegenPerDifficultyConfig;

	public double bonusPerDifficulty = 10d;
	public double maxBonusRegen = 1.0d;
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

		/*if (event.getEntity() instanceof EnderDragonEntity) {
			EnderDragonEntity dragon = (EnderDragonEntity) event.getEntity();
			List<Entity> list = dragon.world.getEntitiesWithinAABB(PlayerEntity.class, dragon.getBoundingBox());
			for (Entity entity : list) {
				PlayerEntity player = (PlayerEntity) entity;
				LogHelper.info("Colliding");
				for (EnderDragonPartEntity part : dragon.getDragonParts()) {
					ServerWorld world = (ServerWorld) dragon.world;
					world.spawnParticle(ParticleTypes.ANGRY_VILLAGER, part.getPosX(), part.getPosY(), part.getPosZ(), 1, 0, 0, 0, 0);
					if (part.getBoundingBox().intersects(player.getBoundingBox())) {
						part.attackEntityFrom(DamageSource.causePlayerDamage(player), 20f);
						LogHelper.info("Colliding with %s", part.field_213853_c);
					}
				}
			}
		}*/
		//result = {AxisAlignedBB@21366} "AABB[5.838289610550541, 59.455070982207616, -3.6175390750013285] -> [6.838289610550541, 60.455070982207616, -2.6175390750013285]"
		//result = {AxisAlignedBB@21379} "AABB[5.3581248052303865, 59.495200877005914, -2.5141609254509736] -> [5.958124829072244, 61.2952008293222, -1.9141609016091157]"
		//result = {AxisAlignedBB@21595} "AABB[-7.863716004406766, 60.26373616009399, -7.691778564177095] -> [8.136283995593235, 68.263736160094, 8.308221435822905]"
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
