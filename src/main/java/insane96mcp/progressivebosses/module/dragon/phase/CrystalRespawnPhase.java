package insane96mcp.progressivebosses.module.dragon.phase;

import com.google.common.collect.ImmutableList;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonCrystal;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CrystalRespawnPhase extends AbstractDragonPhaseInstance {
	private static EnderDragonPhase<CrystalRespawnPhase> CRYSTAL_RESPAWN;

	public Vec3 targetLocation;
	private int tick = 0;
	private boolean respawning = false;
	private final ArrayList<SpikeFeature.EndSpike> spikesToRespawn = new ArrayList<>();

	public CrystalRespawnPhase(EnderDragon dragonIn) {
		super(dragonIn);
	}

	public void doServerTick() {
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
		if (stats.isEmpty()) {
			dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
			return;
		}

		if (this.targetLocation == null) {
			if (this.spikesToRespawn.isEmpty()) {
				dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
				return;
			}
			this.targetLocation = new Vec3(spikesToRespawn.get(0).getCenterX() + 0.5, spikesToRespawn.get(0).getHeight() + 5.5, spikesToRespawn.get(0).getCenterZ() + 0.5);
		}
		if (!respawning) {
			double d0 = this.targetLocation.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
			if (d0 < 9d) { //sqrt = 3
				dragon.setDeltaMovement(Vec3.ZERO);
				respawning = true;
			}
		}
		else {
			tick++;
			//dragon.setDeltaMovement(Vec3.ZERO);
			if (tick <= stats.get().crystal.timeToRespawn - 10 && tick % 5 == 0)
				dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4F, 1.0F);
			if (tick >= stats.get().crystal.timeToRespawn) {
				SpikeFeature.EndSpike spike = spikesToRespawn.get(0);
				boolean shouldBeGuarded = this.dragon.getRandom().nextFloat() < stats.get().crystal.respawnCagedChance;
				boolean wasGuarded = spike.guarded;
				spike.guarded = shouldBeGuarded;
				this.dragon.level().explode(null, spike.getCenterX() + 0.5F, spike.getHeight(), spike.getCenterZ() + 0.5F, 5.0F, Level.ExplosionInteraction.BLOCK);
				RandomSource yungRandom = RandomSource.create(-1157087832721040245L); // Generates 0.0058419704 for Yung's Better End Island spikes to generate guarded
				net.minecraft.world.level.levelgen.feature.Feature.END_SPIKE.place(new SpikeConfiguration(true, ImmutableList.of(spike), null), (ServerLevel) this.dragon.level(), ((ServerLevel) this.dragon.level()).getChunkSource().getGenerator(), shouldBeGuarded ? yungRandom : this.dragon.getRandom(), new BlockPos(spike.getCenterX(), 45, spike.getCenterZ()));
				spike.guarded = wasGuarded;
				this.dragon.level().getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()).forEach(endCrystal -> endCrystal.setInvulnerable(false));
				spikesToRespawn.remove(0);
				if (this.spikesToRespawn.isEmpty())
					LogHelper.info("No more crystals to respawn left");
				for (int i = 0; i < stats.get().crystal.phantomCount; i++) {
					summonPhantom(spike, stats.get().crystal);
				}
				tick = 0;
				respawning = false;
				this.targetLocation = null;
			}
		}
	}

	private void summonPhantom(SpikeFeature.EndSpike spike, DragonCrystal crystal) {
		Phantom phantom = EntityType.PHANTOM.create(this.dragon.level());
		if (phantom == null)
			return;
		float angle = phantom.getRandom().nextFloat() * (float) Math.PI * 2f;
		float x = (float) (spike.getCenterX() + Math.floor(Math.cos(angle) * 6f)) + 0.5f;
		float z = (float) (spike.getCenterZ() + Math.floor(Math.sin(angle) * 6f)) + 0.5f;
		phantom.setPos(x, spike.getHeight() + 10, z);
		phantom.setPhantomSize(crystal.phantomSize);
		if (phantom.getAttribute(Attributes.ATTACK_KNOCKBACK) != null)
            //noinspection DataFlowIssue
            phantom.getAttribute(Attributes.ATTACK_KNOCKBACK).setBaseValue(10d);
		phantom.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(32d);
		phantom.getAttribute(Attributes.MAX_HEALTH).setBaseValue(phantom.getAttributeBaseValue(Attributes.MAX_HEALTH) * 0.5f);
		phantom.setHealth((float) phantom.getAttributeValue(Attributes.MAX_HEALTH));
		List<WrappedGoal> toRemoveList = new ArrayList<>();
		for (WrappedGoal wrappedGoal : phantom.targetSelector.availableGoals) {
			if (wrappedGoal.getGoal() instanceof PhantomAttackPlayerTargetGoal) {
				toRemoveList.add(wrappedGoal);
				phantom.targetSelector.addGoal(1, new PhantomAttackPlayerTargetGoal(phantom));
			}
		}
		for (WrappedGoal toRemove : toRemoveList) {
			phantom.targetSelector.removeGoal(toRemove);
		}
		phantom.getPersistentData().putBoolean(DragonCrystal.DRAGON_PHANTOM, true);
		this.dragon.level().addFreshEntity(phantom);
	}

	public boolean isSitting() {
		return respawning;
	}

	/**
	 * Called when this phase is set to active
	 */
	public void begin() {
		this.targetLocation = null;
		this.spikesToRespawn.clear();
		this.tick = 0;
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getFlySpeed() {
		return 2F;
	}

	@Override
	public float getTurnSpeed() {
		float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
		float f1 = Math.min(f, 40.0F);
		return 0.875f / f1 / f;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vec3 getFlyTargetLocation() {
		return this.targetLocation;
	}

	public void addCrystalRespawn(SpikeFeature.EndSpike spike) {
		if (!this.spikesToRespawn.contains(spike))
			this.spikesToRespawn.add(spike);
	}

	@Override
	public float onHurt(DamageSource source, float amount) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
		if (stats.isEmpty())
			return amount;
		if (source.is(DamageTypeTags.IS_EXPLOSION) && !source.getMsgId().equals("fireworks"))
			return amount;

		return amount * stats.get().vulnerabilities.respawningCrystalDamageMultiplier;
	}

	public EnderDragonPhase<CrystalRespawnPhase> getPhase() {
		return CRYSTAL_RESPAWN;
	}

	public static EnderDragonPhase<CrystalRespawnPhase> getPhaseType() {
		return CRYSTAL_RESPAWN;
	}

	public static void init() {
		CRYSTAL_RESPAWN = EnderDragonPhase.create(CrystalRespawnPhase.class, "CrystalRespawn");
	}

	static class PhantomAttackPlayerTargetGoal extends Goal {
		private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0D);
		private int nextScanTick = reducedTickDelay(20);
		private final Phantom phantom;

        PhantomAttackPlayerTargetGoal(Phantom phantom) {
            this.phantom = phantom;
        }

        /**
		 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
		 * method as well.
		 */
		public boolean canUse() {
			if (this.nextScanTick > 0) {
				--this.nextScanTick;
				return false;
			} else {
				this.nextScanTick = reducedTickDelay(60);
				List<Player> list = this.phantom.level().getNearbyPlayers(this.attackTargeting, this.phantom, this.phantom.getBoundingBox().inflate(this.phantom.getAttributeValue(Attributes.FOLLOW_RANGE), this.phantom.getAttributeValue(Attributes.FOLLOW_RANGE) * 2, this.phantom.getAttributeValue(Attributes.FOLLOW_RANGE)));
				if (!list.isEmpty()) {
					list.sort(Comparator.<Entity, Double>comparing(Entity::getY).reversed());

					for(Player player : list) {
						if (this.phantom.canAttack(player, TargetingConditions.DEFAULT)) {
							this.phantom.setTarget(player);
							return true;
						}
					}
				}

				return false;
			}
		}

		/**
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		public boolean canContinueToUse() {
			LivingEntity livingentity = this.phantom.getTarget();
			return livingentity != null && this.phantom.canAttack(livingentity, TargetingConditions.DEFAULT);
		}
	}
}
