package insane96mcp.progressivebosses.module.dragon.feature;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.insanelib.base.Label;
import insane96mcp.insanelib.base.Module;
import insane96mcp.insanelib.base.config.Config;
import insane96mcp.insanelib.base.config.LoadFeature;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.insanelib.util.MathHelper;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.setup.Reflection;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DifficultyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Label(name = "Attack", description = "Makes the dragon hit harder in various different ways")
@LoadFeature(module = ProgressiveBosses.RESOURCE_PREFIX + "ender_dragon")
public class AttackFeature extends Feature {
	@Config(min = 0d)
	@Label(name = "Bonus Direct Damage", description = "How much more damage at max difficulty (percentage) does the Ender Dragon deal per difficulty?")
	public static Double increasedDirectDamage = 2.25d;
	@Config(min = 0d)
	@Label(name = "Bonus Acid Pool Damage", description = "How much more damage at max difficulty (percentage) does the Ender Dragon's Acid fireball and pool deal per difficulty?")
	public static Double increasedAcidPoolDamage = 2.4d;
	@Config(min = 0d)
	@Label(name = "Charge Player Max Chance", description = """
						Normally the Ender Dragon attacks only when leaving the center platform. With this active she has a chance when she has finished charging / fireballing or before checking if she should land in the center to charge the player.
						This is the chance to start a charge attack when the difficulty is at max. Otherwise it scales accordingly.
						The actual chance is: (this_value * (difficulty / max difficulty)).""")
	public static Double chargePlayerMaxChance = 0.45d;
	@Config(min = 0d)
	@Label(name = "Fireball Max Chance", description = """
						Normally the Ender Dragon spits fireballs when a Crystal is destroyed and rarely during the fight. With this active she has a chance when she has finished charging / fireballing or before checking if she should land in the center to spit a fireball.
						This is the chance to start a fireball attack when the difficulty is at max. Otherwise it scales accordingly.
						The actual chance is: (this_value * (difficulty / max difficulty)).""")
	public static Double fireballMaxChance = 0.35d;
	@Config
	@Label(name = "Increase Max Rise and Fall", description = "Since around 1.13/1.14 the Ender Dragon can no longer dive for more than about 3 blocks so she takes a lot to rise / fall. With this active the dragon will be able to rise and fall many more blocks, making easier to hit the player and approach the center.")
	public static Boolean increaseMaxRiseAndFall = true;
	@Config
	@Label(name = "Fireball Explosion Magic Damage", description = "On impact the Acid Fireball will deal magic damage in an area.")
	public static Boolean fireballExplosionDamages = true;
	@Config
	@Label(name = "Fireball 3D Area Effect Cloud", description = "On impact the Acid Fireball will generate a 3D area of effect cloud instead of a normal flat one. The 3D cloud lasts for half the time.")
	public static Boolean fireball3DEffectCloud = true;
	@Config(min = 0d)
	@Label(name = "Fireball Velocity Multiplier", description = "Speed multiplier for the Dragon Fireball.")
	public static Double fireballVelocityMultiplier = 2.5d;
	@Config(min = 0d)
	@Label(name = "Bonus Fireballs", description = "The dragon will fire (up to) this more fireballs at max difficulty. The bonus fireballs have a slight shotting error so aren't all directly aimed at the player.")
	public static Double maxBonusFireball = 15d;

	public AttackFeature(Module module, boolean enabledByDefault, boolean canBeDisabled) {
		super(module, enabledByDefault, canBeDisabled);
	}

	@SubscribeEvent
	public void onSpawn(EntityJoinLevelEvent event) {
		fireballSpeed(event.getEntity());
	}

	private void fireballSpeed(Entity entity) {
		if (!(entity instanceof DragonFireball fireball))
			return;

		if (!this.isEnabled() || fireballVelocityMultiplier == 0d)
			return;

		if (Math.abs(fireball.xPower) > 10 || Math.abs(fireball.yPower) > 10 || Math.abs(fireball.zPower) > 10) {
			entity.kill();
			return;
		}

		fireball.xPower *= fireballVelocityMultiplier;
		fireball.yPower *= fireballVelocityMultiplier;
		fireball.zPower *= fireballVelocityMultiplier;
	}

	@SubscribeEvent
	public void onDamageDealt(LivingHurtEvent event) {
		if (!this.isEnabled()
				|| event.getEntity().getCommandSenderWorld().isClientSide)
			return;

		onDirectDamage(event);
		onAcidDamage(event);
	}

	private static void onDirectDamage(LivingHurtEvent event) {
		if (!(event.getSource().getDirectEntity() instanceof EnderDragon dragon)
				|| event.getEntity() instanceof EnderDragon)
			return;

		event.setAmount(event.getAmount() * (float)(1d + (increasedDirectDamage * DifficultyHelper.getScalingDifficulty(dragon))));
	}

	private static void onAcidDamage(LivingHurtEvent event) {
		if (!(event.getSource().getEntity() instanceof EnderDragon dragon)
				|| !(event.getSource().getDirectEntity() instanceof AreaEffectCloud))
			return;

		event.setAmount(event.getAmount() * (float)(1d + (increasedAcidPoolDamage * DifficultyHelper.getScalingDifficulty(dragon))));
	}

	public static boolean onPhaseEnd(EnderDragon dragon) {
		boolean chargePlayer = shouldChargePlayer(dragon);
		boolean fireballPlayer = shouldFireballPlayer(dragon);

		if (chargePlayer && fireballPlayer)
			if (dragon.getRandom().nextFloat() < 0.5f)
				chargePlayer(dragon);
			else
				fireballPlayer(dragon);
		else if (chargePlayer)
			chargePlayer(dragon);
		else if (fireballPlayer)
			fireballPlayer(dragon);

		return chargePlayer || fireballPlayer;
	}

	private static boolean shouldChargePlayer(EnderDragon dragon) {
		if (chargePlayerMaxChance == 0f)
			return false;

		if (dragon.getDragonFight() == null)
			return false;

		CompoundTag tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		double chance = chargePlayerMaxChance * (difficulty / DifficultyFeature.maxDifficulty);

		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AABB boundingBox = new AABB(centerPodium).inflate(64d);
		List<Player> players = dragon.level.getEntitiesOfClass(Player.class, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR);

		for (Player player : players) {
			List<EndCrystal> endCrystals = player.level.getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(10d));
			if (endCrystals.size() > 0) {
				chance *= 2d;
				break;
			}
		}

		double rng = dragon.getRandom().nextDouble();

		return rng < chance;
	}

	private static void chargePlayer(EnderDragon dragon) {
		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AABB bb = new AABB(centerPodium).inflate(64d);
		ServerPlayer player = (ServerPlayer) getRandomPlayerNearCrystal(dragon.level, bb);

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
		Vec3 targetPos = player.position();
		if (targetPos.y < dragon.getY())
			targetPos = targetPos.add(0d, -5d, 0d);
		else
			targetPos = targetPos.add(0d, 6d, 0d);
		dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(targetPos);
	}

	private static boolean shouldFireballPlayer(EnderDragon dragon) {
		if (fireballMaxChance == 0f
				|| dragon.getDragonFight() == null)
			return false;

		CompoundTag tags = dragon.getPersistentData();
		float difficulty = tags.getFloat(Strings.Tags.DIFFICULTY);

		if (difficulty == 0f)
			return false;

		double chance = fireballMaxChance * (difficulty / DifficultyFeature.maxDifficulty);

		double rng = dragon.getRandom().nextDouble();

		return rng < chance;
	}

	private static void fireballPlayer(EnderDragon dragon) {
		BlockPos centerPodium = dragon.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
		AABB bb = new AABB(centerPodium).inflate(64d);
		ServerPlayer player = (ServerPlayer) getRandomPlayer(dragon.level, bb);

		if (player == null)
			return;

		dragon.getPhaseManager().setPhase(EnderDragonPhase.STRAFE_PLAYER);
		dragon.getPhaseManager().getPhase(EnderDragonPhase.STRAFE_PLAYER).setTarget(player);
	}


	public static boolean onFireballImpact(DragonFireball fireball, @Nullable Entity shooter, HitResult result) {
		if (!isEnabled(AttackFeature.class))
			return false;

		onImpactExplosion(fireball, shooter, result);
		return onImpact3DCloud(fireball, result);
	}

	private static void onImpactExplosion(DragonFireball fireball, @Nullable Entity shooter, HitResult result) {
		if (!fireballExplosionDamages)
			return;

		float difficultyScaling = 0;
		if (shooter instanceof LivingEntity livingEntity) {
			difficultyScaling = DifficultyHelper.getScalingDifficulty(livingEntity);
		}

		float damage = 6 * (1f + (float) (increasedAcidPoolDamage * difficultyScaling));

		AABB axisAlignedBB = new AABB(result.getLocation(), result.getLocation()).inflate(4d);
		List<LivingEntity> livingEntities = fireball.level.getEntitiesOfClass(LivingEntity.class, axisAlignedBB);
		for (LivingEntity livingEntity : livingEntities) {
			if (livingEntity.distanceToSqr(fireball.position()) < 20.25d)
				livingEntity.hurt((new IndirectEntityDamageSource(Strings.Translatable.DRAGON_FIREBALL, fireball, shooter)).bypassArmor().setProjectile().setMagic(), damage);
		}
	}

	private static boolean onImpact3DCloud(DragonFireball fireball, HitResult result) {
		if (!fireball3DEffectCloud)
			return false;

		HitResult.Type hitResult$type = result.getType();
		if (hitResult$type == HitResult.Type.ENTITY) {
			Reflection.Projectile_onHitEntity(fireball, (EntityHitResult)result);
		}
		else if (hitResult$type == HitResult.Type.BLOCK) {
			Reflection.Projectile_onHitBlock(fireball, (BlockHitResult)result);
		}
		Entity entity = fireball.getOwner();
		if (entity != null && (result.getType() != HitResult.Type.ENTITY || !((EntityHitResult)result).getEntity().is(entity))) {
			if (!fireball.level.isClientSide) {
				List<LivingEntity> list = fireball.level.getEntitiesOfClass(LivingEntity.class, fireball.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
				AreaEffectCloud3DEntity areaeffectcloudentity = new AreaEffectCloud3DEntity(fireball.level, fireball.getX(), fireball.getY(), fireball.getZ());
				if (entity instanceof LivingEntity) {
					areaeffectcloudentity.setOwner((LivingEntity)entity);
				}

				areaeffectcloudentity.setParticle(ParticleTypes.DRAGON_BREATH);
				areaeffectcloudentity.setRadius(3.0F);
				areaeffectcloudentity.setDuration(300);
				areaeffectcloudentity.setWaitTime(10);
				areaeffectcloudentity.setRadiusPerTick((7.0F - areaeffectcloudentity.getRadius()) / (float)areaeffectcloudentity.getDuration());
				areaeffectcloudentity.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
				if (!list.isEmpty()) {
					for(LivingEntity livingentity : list) {
						double d0 = fireball.distanceToSqr(livingentity);
						if (d0 < 16.0D) {
							areaeffectcloudentity.setPos(livingentity.getX(), livingentity.getY(), livingentity.getZ());
							break;
						}
					}
				}

				fireball.level.levelEvent(2006, fireball.blockPosition(), fireball.isSilent() ? -1 : 1);
				fireball.level.addFreshEntity(areaeffectcloudentity);
				fireball.discard();
			}
		}

		return true;
	}

	public static void fireFireball(EnderDragon dragon, LivingEntity attackTarget) {
		Vec3 vector3d2 = dragon.getViewVector(1.0F);
		double x = dragon.head.getX() - vector3d2.x;
		double y = dragon.head.getY(0.5D) + 0.5D;
		double z = dragon.head.getZ() - vector3d2.z;
		double xPower = attackTarget.getX() - x;
		double yPower = attackTarget.getY(0.5D) - y;
		double zPower = attackTarget.getZ() - z;
		if (!dragon.isSilent()) {
			dragon.level.levelEvent(null, 1017, dragon.blockPosition(), 0);
		}

		DragonFireball dragonfireballentity = new DragonFireball(dragon.level, dragon, xPower, yPower, zPower);
		dragonfireballentity.moveTo(x, y, z, 0.0F, 0.0F);
		dragon.level.addFreshEntity(dragonfireballentity);

		float fireballs = Mth.nextFloat(dragon.getRandom(), 0f, (float) (maxBonusFireball * DifficultyHelper.getScalingDifficulty(dragon)));
		fireballs = MathHelper.getAmountWithDecimalChance(dragon.getRandom(), fireballs);
		if (fireballs == 0f)
			return;

		for (int i = 0; i < fireballs; i++) {
			x = dragon.head.getX() - vector3d2.x;
			y = dragon.head.getY(0.5D) + 0.5D;
			z = dragon.head.getZ() - vector3d2.z;
			xPower = attackTarget.getX() + Mth.nextDouble(dragon.getRandom(), -(fireballs), fireballs) - x;
			yPower = attackTarget.getY(0.5D) + Mth.nextDouble(dragon.getRandom(), -(fireballs), fireballs) - y;
			zPower = attackTarget.getZ() + Mth.nextDouble(dragon.getRandom(), -(fireballs), fireballs) - z;
			if (!dragon.isSilent()) {
				dragon.level.levelEvent(null, 1017, dragon.blockPosition(), 0);
			}

			dragonfireballentity = new DragonFireball(dragon.level, dragon, xPower, yPower, zPower);
			dragonfireballentity.moveTo(x, y, z, 0.0F, 0.0F);
			dragon.level.addFreshEntity(dragonfireballentity);
		}
	}

	@Nullable
	public static Player getRandomPlayer(Level world, AABB boundingBox) {
		List<Player> players = world.getEntitiesOfClass(Player.class, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
		if (players.isEmpty())
			return null;

		int r = Mth.nextInt(world.random, 0, players.size() - 1);
		return players.get(r);
	}

	//Returns a random player that is at least 10 blocks near a Crystal or a random player if no players are near crystals
	@Nullable
	public static Player getRandomPlayerNearCrystal(Level world, AABB boundingBox) {
		List<Player> players = world.getEntitiesOfClass(Player.class, boundingBox);
		if (players.isEmpty())
			return null;

		List<Player> playersNearCrystals = new ArrayList<>();

 		for (Player player : players) {
			List<EndCrystal> endCrystals = player.level.getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(10d), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
			if (endCrystals.size() > 0)
				playersNearCrystals.add(player);
		}

 		int r;
 		if (playersNearCrystals.isEmpty()) {
			r = Mth.nextInt(world.random, 0, players.size() - 1);
			return players.get(r);
		}

		r = Mth.nextInt(world.random, 0, playersNearCrystals.size() - 1);
		return playersNearCrystals.get(r);
	}
}
