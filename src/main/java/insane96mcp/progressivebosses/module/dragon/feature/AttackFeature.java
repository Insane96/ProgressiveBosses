package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.setup.Config;
import insane96mcp.progressivebosses.setup.Reflection;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.EndPodiumFeature;
import net.minecraftforge.common.ForgeConfigSpec;
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
	private final ForgeConfigSpec.ConfigValue<Boolean> fireball3DEffectCloudConfig;

	//TODO Add a min chance for charge and fireball
	public double increasedDirectDamage = 0.04d;
	public double increasedAcidPoolDamage = 0.0475d;
	public double chargePlayerMaxChance = 0.15d;
	public double fireballMaxChance = 0.15d;
	public double maxChanceAtDifficulty = 16;
	public boolean increaseMaxRiseAndFall = true;
	public boolean fireballExplosionDamages = true;
	public boolean fireball3DEffectCloud = true;

	public AttackFeature(Module module) {
		super(Config.builder, module);
		Config.builder.comment(this.getDescription()).push(this.getName());
		increasedDirectDamageConfig = Config.builder
				.comment("How much more damage per difficulty (percentage) does the Ender Dragon (directly) deal per difficulty?")
				.defineInRange("Bonus Direct Damage", increasedDirectDamage, 0.0, Double.MAX_VALUE);
		increasedAcidPoolDamageConfig = Config.builder
				.comment("How much more damage per difficulty (percentage) does the Ender Dragon's Acid Pool deal per difficulty?")
				.defineInRange("Bonus Acid Pool Damage", increasedAcidPoolDamage, 0.0, Double.MAX_VALUE);

		chargePlayerMaxChanceConfig = Config.builder
				.comment("Normally the Ender Dragon attacks only when leaving the center platform. With this active she has a chance everytime she takes damage to charge the player.\n" +
						"This defines the chance to attack the player each tick when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher.\n" +
						"The actual formula is: (this_value / 'Max Chance at Difficulty') * difficulty * (1 / MAX(remaining_crystals, 1)).")
				.defineInRange("Charge Player Max Chance", chargePlayerMaxChance, 0.0, Double.MAX_VALUE);
		//TODO add a multiple fireball attack
		fireballMaxChanceConfig = Config.builder
				.comment("Normally the Ender Dragon spits fireballs when a Crystal is destroyed and rarely during the fight. With this active she has a chance everytime she takes damage to spit a fireball.\n" +
						"This defines the chance to spit a fireball everytime she takes damage when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher.\n" +
						"The actual formula is: (this_value / 'Max Chance at Difficulty') * difficulty * (1 / MAX(remaining_crystals, 1)).")
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
		fireball3DEffectCloudConfig = Config.builder
				.comment("On impact the Acid Fireball will generate a 3D area of effect cloud instead of a normal flat one.")
				.define("Fireball 3D Area Effect Cloud", fireball3DEffectCloud);
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
		this.fireballExplosionDamages = this.fireballExplosionDamagesConfig.get();
		this.fireball3DEffectCloud = this.fireball3DEffectCloudConfig.get();
	}

	/*@SubscribeEvent
	public void onUpdate(LivingEvent.LivingUpdateEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		if (!(event.getEntityLiving() instanceof EnderDragonEntity))
			return;

	}*/

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (event.getEntity().getEntityWorld().isRemote)
			return;

		if (!this.isEnabled())
			return;

		onDirectDamage(event);
		onAcidDamage(event);

		fireballPlayer(event);
		chargePlayer(event);
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
	}

	private void onAcidDamage(LivingHurtEvent event) {
		if (!(event.getSource().getTrueSource() instanceof EnderDragonEntity) || !(event.getSource().getImmediateSource() instanceof AreaEffectCloudEntity))
			return;
		EnderDragonEntity wither = (EnderDragonEntity) event.getSource().getTrueSource();

		CompoundNBT compoundNBT = wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty == 0f)
			return;

		event.setAmount(event.getAmount() * (float)(1d + (this.increasedAcidPoolDamage * difficulty)));
	}


	private void chargePlayer(LivingHurtEvent event) {
		if (this.chargePlayerMaxChance == 0f)
			return;

		if (!(event.getEntityLiving() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntityLiving();

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

		BlockPos centerPodium = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AxisAlignedBB bb = new AxisAlignedBB(centerPodium).grow(128d);
		ServerPlayerEntity player = (ServerPlayerEntity) getRandomPlayer(dragon.world, bb);

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(PhaseType.CHARGING_PLAYER);
		Vector3d targetPos = player.getPositionVec();
		if (targetPos.y < dragon.getPosY())
			targetPos = targetPos.add(0d, -6d, 0d);
		else
			targetPos = targetPos.add(0d, 6d, 0d);
		dragon.getPhaseManager().getPhase(PhaseType.CHARGING_PLAYER).setTarget(targetPos);
	}

	private void fireballPlayer(LivingHurtEvent event) {
		if (this.fireballMaxChance == 0f)
			return;

		if (!(event.getEntityLiving() instanceof EnderDragonEntity))
			return;

		EnderDragonEntity dragon = (EnderDragonEntity) event.getEntityLiving();

		if (dragon.getFightManager() == null)
			return;

		if (dragon.getPhaseManager().getCurrentPhase().getType() != PhaseType.HOLDING_PATTERN)
			return;

		CompoundNBT tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty == 0f)
			return;

		double chance = this.fireballMaxChance / maxChanceAtDifficulty;
		chance *= difficulty;
		int crystalsAlive = Math.max(dragon.getFightManager().getNumAliveCrystals(), 1);
		chance *= (1f / crystalsAlive);
		chance = Math.min(this.fireballMaxChance, chance);
		double rng = RandomHelper.getDouble(dragon.getRNG(), 0d, 1d);

		if (rng >= chance)
			return;

		BlockPos centerPodium = dragon.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AxisAlignedBB bb = new AxisAlignedBB(centerPodium).grow(128d);
		ServerPlayerEntity player = (ServerPlayerEntity) getRandomPlayer(dragon.world, bb);

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(PhaseType.STRAFE_PLAYER);
		dragon.getPhaseManager().getPhase(PhaseType.STRAFE_PLAYER).setTarget(player);
	}


	public boolean onFireballImpact(DragonFireballEntity fireball, @Nullable Entity shooter, RayTraceResult result) {
		if (!this.isEnabled())
			return false;

		onImpactExplosion(fireball, shooter, result);
		return onImpact3DCloud(fireball, result);
	}

	private void onImpactExplosion(DragonFireballEntity fireball, @Nullable Entity shooter, RayTraceResult result) {
		if (!this.fireballExplosionDamages)
			return;

		float difficulty = 0;
		if (shooter != null) {
			CompoundNBT compoundNBT = shooter.getPersistentData();
			difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);
		}

		float damage = 6 * (1f + (float) (this.increasedAcidPoolDamage * difficulty));

		AxisAlignedBB axisAlignedBB = new AxisAlignedBB(result.getHitVec(), result.getHitVec()).grow(4d);
		List<LivingEntity> livingEntities = fireball.world.getLoadedEntitiesWithinAABB(LivingEntity.class, axisAlignedBB);
		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity.getDistanceSq(fireball.getPositionVec()) < 20.25d)
				livingEntity.attackEntityFrom((new IndirectEntityDamageSource(Strings.Translatable.DRAGON_FIREBALL, fireball, shooter)).setDamageBypassesArmor().setMagicDamage(), damage);
		}
	}

	private boolean onImpact3DCloud(DragonFireballEntity fireball, RayTraceResult result) {
		if (!this.isEnabled())
			return false;

		if (!this.fireball3DEffectCloud)
			return false;

		RayTraceResult.Type raytraceresult$type = result.getType();
		if (raytraceresult$type == RayTraceResult.Type.ENTITY) {
			Reflection.ProjectileEntity_onEntityHit(fireball, (EntityRayTraceResult)result);
		} else if (raytraceresult$type == RayTraceResult.Type.BLOCK) {
			Reflection.ProjectileEntity_onBlockHit(fireball, (BlockRayTraceResult)result);
		}
		Entity entity = fireball.func_234616_v_();
		if (result.getType() != RayTraceResult.Type.ENTITY || !((EntityRayTraceResult)result).getEntity().isEntityEqual(entity)) {
			if (!fireball.world.isRemote) {
				List<LivingEntity> list = fireball.world.getLoadedEntitiesWithinAABB(LivingEntity.class, fireball.getBoundingBox().grow(4.0D, 2.0D, 4.0D));
				AreaEffectCloud3DEntity areaeffectcloudentity = new AreaEffectCloud3DEntity(fireball.world, fireball.getPosX(), fireball.getPosY(), fireball.getPosZ());
				if (entity instanceof LivingEntity) {
					areaeffectcloudentity.setOwner((LivingEntity)entity);
				}

				areaeffectcloudentity.setParticleData(ParticleTypes.DRAGON_BREATH);
				areaeffectcloudentity.setRadius(3.0F);
				areaeffectcloudentity.setDuration(600);
				areaeffectcloudentity.setRadiusPerTick((7.0F - areaeffectcloudentity.getRadius()) / (float)areaeffectcloudentity.getDuration());
				areaeffectcloudentity.addEffect(new EffectInstance(Effects.INSTANT_DAMAGE, 1, 1));
				if (!list.isEmpty()) {
					for(LivingEntity livingentity : list) {
						double d0 = fireball.getDistanceSq(livingentity);
						if (d0 < 16.0D) {
							areaeffectcloudentity.setPosition(livingentity.getPosX(), livingentity.getPosY(), livingentity.getPosZ());
							break;
						}
					}
				}

				fireball.world.playEvent(2006, fireball.getPosition(), fireball.isSilent() ? -1 : 1);
				fireball.world.addEntity(areaeffectcloudentity);
				fireball.remove();
			}
		}

		return true;
	}

	@Nullable
	public PlayerEntity getRandomPlayer(World world, AxisAlignedBB boundingBox) {
		List<PlayerEntity> players = world.getLoadedEntitiesWithinAABB(PlayerEntity.class, boundingBox);
		if (players.isEmpty())
			return null;

		int r = RandomHelper.getInt(world.rand, 0, players.size());
		return players.get(r);
	}
}
