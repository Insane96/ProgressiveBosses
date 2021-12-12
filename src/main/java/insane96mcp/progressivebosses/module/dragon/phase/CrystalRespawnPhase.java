package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.module.dragon.feature.CrystalFeature;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.gen.feature.EndSpikeFeature;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CrystalRespawnPhase extends Phase {
	private static PhaseType<CrystalRespawnPhase> CRYSTAL_RESPAWN;

	public Vector3d targetLocation;
	private int tick = 0;
	private boolean respawning = false;
	private final ArrayList<EndSpikeFeature.EndSpike> spikesToRespawn = new ArrayList<>();

	public CrystalRespawnPhase(EnderDragonEntity dragonIn) {
		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isClientSide.
	 */
	public void doServerTick() {
		CompoundNBT dragonTags = this.dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		if (this.targetLocation == null) {
			if (spikesToRespawn.isEmpty()) {
				dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
				return;
			}
			this.targetLocation = new Vector3d(spikesToRespawn.get(0).getCenterX() + 0.5, spikesToRespawn.get(0).getHeight() + 5.5, spikesToRespawn.get(0).getCenterZ() + 0.5);
		}
		int tickSpawnCystal = (int) (50 - (difficulty / 4));
		if (!respawning) {
			double d0 = this.targetLocation.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
			if (d0 < 16d) {
				dragon.setDeltaMovement(Vector3d.ZERO);
				respawning = true;
			}
		}
		else {
			tick++;
			dragon.setDeltaMovement(Vector3d.ZERO);
			if (tick <= 25)
				dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4F, 1.0F);
			if (tick >= tickSpawnCystal) {
				if (dragon.getHealth() < 10f) {
					dragon.getPhaseManager().setPhase(PhaseType.TAKEOFF);
					return;
				}
				EnderCrystalEntity crystal;
				double x = spikesToRespawn.get(0).getCenterX();
				double y = spikesToRespawn.get(0).getHeight();
				double z = spikesToRespawn.get(0).getCenterZ();
				if (dragon.getRandom().nextDouble() < Modules.dragon.crystal.crystalRespawnInsideTowerChance * difficulty)
					CrystalFeature.generateCrystalInTower(dragon.level, x + 0.5, y + 1, z + 0.5);
				else {
					crystal = new EnderCrystalEntity(dragon.level, x + 0.5, y + 1, z + 0.5);
					crystal.setShowBottom(true);
					crystal.level.explode(dragon, x + 0.5, y + 1.5, z + 0.5, 5f, Explosion.Mode.NONE);
					dragon.level.addFreshEntity(crystal);
					CrystalFeature.generateCage(crystal.level, crystal.blockPosition());
				}
				dragon.hurt(dragon.head, DamageSource.explosion(dragon), 10f);
				spikesToRespawn.remove(0);
				tick = 0;
				respawning = false;
				this.targetLocation = null;
			}
		}
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
	}

	/**
	 * Returns the maximum amount dragon may rise or fall during this phase
	 */
	public float getFlySpeed() {
		return 24F;
	}

	/**
	 * Returns the location the dragon is flying toward
	 */
	@Nullable
	public Vector3d getFlyTargetLocation() {
		return this.targetLocation;
	}

	public void addCrystalRespawn(EndSpikeFeature.EndSpike spike) {
		if (!this.spikesToRespawn.contains(spike))
			this.spikesToRespawn.add(spike);
	}

	public PhaseType<CrystalRespawnPhase> getPhase() {
		return CRYSTAL_RESPAWN;
	}

	public static PhaseType<CrystalRespawnPhase> getPhaseType() {
		return CRYSTAL_RESPAWN;
	}

	public static void init() {
		CRYSTAL_RESPAWN = PhaseType.create(CrystalRespawnPhase.class, "CrystalRespawn");
	}
}
