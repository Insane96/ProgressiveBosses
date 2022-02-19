package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.module.dragon.feature.CrystalFeature;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
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

	public CrystalRespawnPhase(EnderDragon dragonIn) {
		super(dragonIn);
	}

	/**
	 * Gives the phase a chance to update its status.
	 * Called by dragon's onLivingUpdate. Only used when !worldObj.isClientSide.
	 */
	public void doServerTick() {
		CompoundTag dragonTags = this.dragon.getPersistentData();
		float difficulty = dragonTags.getFloat(Strings.Tags.DIFFICULTY);

		if (this.targetLocation == null) {
			if (spikesToRespawn.isEmpty()) {
				dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
				return;
			}
			this.targetLocation = new Vec3(spikesToRespawn.get(0).getCenterX() + 0.5, spikesToRespawn.get(0).getHeight() + 5.5, spikesToRespawn.get(0).getCenterZ() + 0.5);
		}
		int tickSpawnCystal = (int) (50 - (difficulty / 4));
		if (!respawning) {
			double d0 = this.targetLocation.distanceToSqr(dragon.getX(), dragon.getY(), dragon.getZ());
			if (d0 < 16d) {
				dragon.setDeltaMovement(Vec3.ZERO);
				respawning = true;
			}
		}
		else {
			tick++;
			dragon.setDeltaMovement(Vec3.ZERO);
			if (tick <= 25)
				dragon.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4F, 1.0F);
			if (tick >= tickSpawnCystal) {
				if (dragon.getHealth() < 10f) {
					dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
					return;
				}
				EndCrystal crystal;
				double x = spikesToRespawn.get(0).getCenterX();
				double y = spikesToRespawn.get(0).getHeight();
				double z = spikesToRespawn.get(0).getCenterZ();
				if (dragon.getRandom().nextDouble() < Modules.dragon.crystal.crystalRespawnInsideTowerChance * difficulty)
					CrystalFeature.generateCrystalInTower(dragon.level, x + 0.5, y + 1, z + 0.5);
				else {
					crystal = new EndCrystal(dragon.level, x + 0.5, y + 1, z + 0.5);
					crystal.setShowBottom(true);
					crystal.level.explode(dragon, x + 0.5, y + 1.5, z + 0.5, 5f, Explosion.BlockInteraction.NONE);
					dragon.level.addFreshEntity(crystal);
					CrystalFeature.generateCage(crystal.level, crystal.blockPosition());
				}
				//dragon.hurt(dragon.head, DamageSource.explosion(dragon), 10f);
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
	public Vec3 getFlyTargetLocation() {
		return this.targetLocation;
	}

	public void addCrystalRespawn(SpikeFeature.EndSpike spike) {
		if (!this.spikesToRespawn.contains(spike))
			this.spikesToRespawn.add(spike);
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
