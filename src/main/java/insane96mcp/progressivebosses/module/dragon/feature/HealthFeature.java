package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
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
	private final ForgeConfigSpec.ConfigValue<Double> bonusCrystalRegenConfig;

	public double bonusPerDifficulty = 34.4d;
	public double maxBonusRegen = 1.0d;
	public double bonusRegenPerDifficulty = 0.05d;
	public double bonusCrystalRegen = 0.05d;

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
				.comment("How much health will the Ender Dragon regen per difficulty each second whenever she's attached to a Crystal. This is added to the normal Crystal regen.")
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

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity enderDragon = (EnderDragonEntity) event.getEntity();

		if (enderDragon.getAttribute(Attributes.MAX_HEALTH).getModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID) != null)
			return;

		CompoundNBT dragonTags = enderDragon.getPersistentData();
		double difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);
		ModifiableAttributeInstance health = enderDragon.getAttribute(Attributes.MAX_HEALTH);
		AttributeModifier modifier = new AttributeModifier(Strings.AttributeModifiers.BONUS_HEALTH_UUID, Strings.AttributeModifiers.BONUS_HEALTH, difficulty * this.bonusPerDifficulty, AttributeModifier.Operation.ADDITION);
		health.addPermanentModifier(modifier);
		enderDragon.setHealth(enderDragon.getMaxHealth());
	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().level.isClientSide)
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
		if (!this.isEnabled())
			return;

		if (!(event.getEntity() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity enderDragon = (EnderDragonEntity) event.getEntity();

		if (!enderDragon.isAlive() || enderDragon.getPhaseManager().getCurrentPhase().getPhase() == PhaseType.DYING)
			return;

		CompoundNBT tags = enderDragon.getPersistentData();

		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty <= 0)
			return;

		float flatBonusHeal = getFlatBonusHeal(difficulty);
		float crystalBonusHeal = getCrystalBonusHeal(enderDragon, difficulty);

		float heal = flatBonusHeal + crystalBonusHeal;
		if (heal == 0f)
			return;

		if (enderDragon.tickCount % 20 == 0)
			LogHelper.info(ProgressiveBosses.LOGGER, "heal: %s, health: %s", heal, enderDragon.getHealth());

		heal /= 20f;

		enderDragon.heal(heal);
	}

	private float getFlatBonusHeal(float difficulty) {
		if (this.bonusRegenPerDifficulty == 0d || this.maxBonusRegen == 0d)
			return 0f;
		return (float) Math.min(difficulty * this.bonusRegenPerDifficulty, this.maxBonusRegen);
	}

	private float getCrystalBonusHeal(EnderDragonEntity enderDragon, float difficulty) {
		if (this.bonusCrystalRegen == 0d)
			return 0f;

		if (enderDragon.nearestCrystal == null || !enderDragon.nearestCrystal.isAlive())
			return 0f;

		return (float) (this.bonusCrystalRegen * difficulty);
	}
}
