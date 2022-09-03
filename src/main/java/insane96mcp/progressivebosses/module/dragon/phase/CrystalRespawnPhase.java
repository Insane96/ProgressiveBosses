package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.module.dragon.feature.CrystalFeature;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CrystalRespawnPhase extends AbstractDragonPhaseInstance {
	private static EnderDragonPhase<CrystalRespawnPhase> CRYSTAL_RESPAWN;

	public Vec3 targetLocation;
	private int tick = 0;
	private boolean respawning = false;
	private final ArrayList<SpikeFeature.EndSpike> spikesToRespawn = new ArrayList<>();

	private final int TICK_RESPAWN_CRYSTAL = 100;

	public CrystalRespawnPhase(EnderDragon dragonIn) {
		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isClientSide.
	 */
	public void doServerTick() {
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
			dragon.setDeltaMovement(Vec3.ZERO);
			if (tick <= 75 && tick % 5 == 0)
				dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4F, 1.0F);
			if (tick >= TICK_RESPAWN_CRYSTAL) {
				double x = spikesToRespawn.get(0).getCenterX();
				double y = spikesToRespawn.get(0).getHeight();
				double z = spikesToRespawn.get(0).getCenterZ();
				EndCrystal crystal = new EndCrystal(dragon.level, x + 0.5, y + 1, z + 0.5);
				crystal.setShowBottom(true);
				crystal.level.explode(dragon, x + 0.5, y + 1.5, z + 0.5, 5f, Explosion.BlockInteraction.NONE);
				dragon.level.addFreshEntity(crystal);
				CrystalFeature.generateCage(crystal.level, crystal.blockPosition());
				spikesToRespawn.remove(0);
				if (this.spikesToRespawn.isEmpty())
					LogHelper.info("No more crystals to respawn left");
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
		return 12F;
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
		if (source.isExplosion() && !source.getMsgId().equals("fireworks"))
			return amount;

		return amount * 1.5f;
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
}
