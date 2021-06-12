package insane96mcp.progressivebosses.modules.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

@Label(name = "Attack", description = "Makes the dragon hit harder and more often")
public class AttackFeature extends Feature {

	private final ForgeConfigSpec.ConfigValue<Double> increasedDirectDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> increasedAcidPoolDamageConfig;
	private final ForgeConfigSpec.ConfigValue<Double> chargePlayerMaxChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> fireballMaxChanceConfig;
	private final ForgeConfigSpec.ConfigValue<Double> maxChanceAtDifficultyConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> increaseMaxRiseAndFallConfig;
	private final ForgeConfigSpec.ConfigValue<Boolean> fireballExplosionDamagesConfig;

	//TODO Nerf, at max difficulty is player 1 shot and Unbr III armor 3-shot-break
	public double increasedDirectDamage = 0.10d;
	//TODO Nerf, but not too much
	public double increasedAcidPoolDamage = 0.10d;
	public double chargePlayerMaxChance = 0.01d;
	public double fireballMaxChance = 0.015;
	public double maxChanceAtDifficulty = 16;
	public boolean increaseMaxRiseAndFall = true;
	public boolean fireballExplosionDamages = true;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		increasedDirectDamageConfig = Config.builder
				.comment("How much more damage per difficulty (percentage) does the Ender Dragon (directly) deal per difficulty?")
				.defineInRange("Bonus Direct Damage", increasedDirectDamage, 0.0, Double.MAX_VALUE);
		increasedAcidPoolDamageConfig = Config.builder
				.comment("How much more damage per difficulty (percentage) does the Ender Dragon's Acid Pool deal per difficulty?")
				.defineInRange("Bonus Acid Pool Damage", increasedAcidPoolDamage, 0.0, Double.MAX_VALUE);

		//TODO Maybe change this per hit instead of on update?
		chargePlayerMaxChanceConfig = Config.builder
				.comment("Normally the Ender Dragon attacks only when leaving the center platform. With this active she has a chance each tick (1/20th of second) when roaming around to attack the player.\n" +
						"This defines the chance to attack the player each tick when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher. The actual formula is\n" +
						"(this_value / 'Max Chance at Difficulty') * difficulty * (1 / MAX(remaining_crystals, 1)).")
				.defineInRange("Charge Player Max Chance", chargePlayerMaxChance, 0.0, Double.MAX_VALUE);
		//TODO Change this per hit instead of on update
		fireballMaxChanceConfig = Config.builder
				.comment("Normally the Ender Dragon spits fireballs when a Crystal is destroyed and rarely during the fight. With this active she has a chance each tick (1/20th of second) when roaming around to spit a fireball.\n" +
						"This defines the chance to spit a fireball each tick when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher. The actual formula is\n" +
						"(this_value / 'Max Chance at Difficulty') * difficulty * (1 / MAX(remaining_crystals, 1)).")
				.defineInRange("Fireball Max Chance", fireballMaxChance, 0.0, Double.MAX_VALUE);
		maxChanceAtDifficultyConfig = Config.builder
				.comment("Defines at which difficulty the Dragon has max chance to attack or spit fireballs when all crystals are destroyed (see 'Fireball Max Chance' and 'Charge Player Max Chance')")
				.defineInRange("Max Chance at Difficulty", maxChanceAtDifficulty, 0.0, Double.MAX_VALUE);
		increaseMaxRiseAndFallConfig = Config.builder
				.comment("Since around 1.13/1.14 the Ender Dragon can no longer dive for more than about 3 blocks so she takes a lot to rise / fall. With this active the dragon will be able to rise and fall many more blocks, making easier to hit the player and approach the center.")
				.define("Increase Max Rise and Fall", increaseMaxRiseAndFall);

		fireballExplosionDamagesConfig = Config.builder
				.comment("On impact the Acid Fireball will deal magic damage in an area.")
				.define("Fireball Explosion Magic Damage", fireballExplosionDamages);
		Config.builder.pop();
	}

	@Override
	public void loadConfig() {
		super.loadConfig();
		this.increasedDirectDamage = this.increasedDirectDamageConfig.get();
		this.increasedAcidPoolDamage = this.increasedAcidPoolDamageConfig.get();
		this.chargePlayerMaxChance = this.chargePlayerMaxChanceConfig.get();
		this.fireballMaxChance = this.fireballMaxChanceConfig.get();
		this.maxChanceAtDifficulty = this.maxChanceAtDifficultyConfig.get();
		this.increaseMaxRiseAndFall = this.increaseMaxRiseAndFallConfig.get();
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinWorldEvent event) {

	}

	@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntityLiving() instanceof EnderDragonEntity))
			return;

		chargePlayer((EnderDragonEntity) event.getEntity());
		fireballPlayer((EnderDragonEntity) event.getEntity());
	}

	private void chargePlayer(EnderDragonEntity dragon) {
		if (this.chargePlayerMaxChance == 0f)
			return;

		if (dragon.getFightManager() == null)
			return;

		if (dragon.getPhaseManager().getCurrentPhase().getType() != PhaseType.HOLDING_PATTERN)
			return;

		CompoundNBT tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		double chance = this.chargePlayerMaxChance / maxChanceAtDifficulty;
		chance *= difficulty;
		int crystalsAlive = Math.max(dragon.getFightManager().getNumAliveCrystals(), 1);
		chance *= (1f / crystalsAlive);
		chance = Math.min(this.chargePlayerMaxChance, chance);

		double rng = RandomHelper.getDouble(dragon.getRNG(), 0d, 1d);

		if (rng >= chance)
			return;

		ServerPlayerEntity player = (ServerPlayerEntity) dragon.world.getClosestPlayer(new EntityPredicate().setDistance(150d), dragon, dragon.getPosX(), dragon.getPosX(), dragon.getPosX());

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
		Vector3d targetPos = player.getPositionVec();
		if (targetPos.y < dragon.getPosY())
			targetPos = targetPos.add(0d, -5d, 0d);
		else
			targetPos = targetPos.add(0d, 5d, 0d);
		dragon.getPhaseManager().getPhase(PhaseType.CHARGING_PLAYER).setTarget(targetPos);
	}

	private void fireballPlayer(EnderDragonEntity dragon) {
		if (this.fireballMaxChance == 0f)
			return;

		if (dragon.getFightManager() == null)
			return;

		if (dragon.getPhaseManager().getCurrentPhase().getType() != PhaseType.HOLDING_PATTERN)
			return;

		CompoundNBT tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		double chance = this.fireballMaxChance / maxChanceAtDifficulty;
		chance *= difficulty;
		int crystalsAlive = Math.max(dragon.getFightManager().getNumAliveCrystals(), 1);
		chance *= (1f / crystalsAlive);
		chance = Math.min(this.fireballMaxChance, chance);
		double rng = RandomHelper.getDouble(dragon.getRNG(), 0d, 1d);

		if (rng >= chance)
			return;

		ServerPlayerEntity player = (ServerPlayerEntity) dragon.world.getClosestPlayer(new EntityPredicate().setDistance(150d), dragon, dragon.getPosX(), dragon.getPosX(), dragon.getPosX());

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
		dragon.getPhaseManager().getPhase(PhaseType.STRAFE_PLAYER).setTarget(player);
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		onDirectDamage(event);
		onAcidDamage(event);
	}

	private void onDirectDamage(LivingHurtEvent event) {
		if (!(event.getSource().getImmediateSource() instanceof EnderDragonEntity))
			return;
		EnderDragonEntity wither = (EnderDragonEntity) event.getSource().getImmediateSource();

		CompoundNBT compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty == 0f)
			return;

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedDirectDamage * difficulty)));

		LogHelper.info("dmg: %f", event.getAmount());
	}

	private void onAcidDamage(LivingHurtEvent event) {
		if (!(event.getSource().getTrueSource() instanceof EnderDragonEntity))
			return;
		EnderDragonEntity wither = (EnderDragonEntity) event.getSource().getTrueSource();

		CompoundNBT compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty == 0f)
			return;

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedAcidPoolDamage * difficulty)));
	}

	public void onFireballImpact(DragonFireballEntity fireball, @Nullable Entity shooter, RayTraceResult result) {
		if (!this.isEnabled())
			return;

		if (!this.fireballExplosionDamages)
			return;
		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(result.getHitVec(), result.getHitVec()).grow(4d);
		List<LivingEntity> livingEntities = fireball.world.getLoadedEntitiesWithinAABB(LivingEntity.class, axisAlignedBB);
		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity.getDistanceSq(fireball.getPositionVec()) < 20.25d)
				livingEntity.attackEntityFrom((new IndirectEntityDamageSource(Strings.Translatable.DRAGON_FIREBALL, fireball, shooter)).setDamageBypassesArmor().setMagicDamage(), (float)6);
		}
	}
}
