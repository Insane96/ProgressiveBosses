package insane96mcp.progressivebosses.module.dragon.phase;

import com.google.common.collect.ImmutableList;
import insane96mcp.progressivebosses.utils.LogHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class CrystalRespawnPhase extends AbstractDragonPhaseInstance {
	private static EnderDragonPhase<CrystalRespawnPhase> CRYSTAL_RESPAWN;

	public Vec3 targetLocation;
	private int tick = 0;
	private boolean respawning = false;
	private final ArrayList<SpikeFeature.EndSpike> spikesToRespawn = new ArrayList<>();

	@SuppressWarnings("FieldCanBeLocal")
	private final int TICK_RESPAWN_CRYSTAL = 100;

	public CrystalRespawnPhase(EnderDragon dragonIn) {
		super(dragonIn);
	}

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
				SpikeFeature.EndSpike spike = spikesToRespawn.get(0);
				boolean wasGuarded = spike.guarded;
				spike.guarded = true;
				this.dragon.level().explode(null, spike.getCenterX() + 0.5F, spike.getHeight(), spike.getCenterZ() + 0.5F, 5.0F, Level.ExplosionInteraction.BLOCK);
				RandomSource random = RandomSource.create(-1157087832721040245L); // Generates 0.0058419704 for Yung's Better End Island to generate guarded
				net.minecraft.world.level.levelgen.feature.Feature.END_SPIKE.place(new SpikeConfiguration(true, ImmutableList.of(spike), null), (ServerLevel) this.dragon.level(), ((ServerLevel) this.dragon.level()).getChunkSource().getGenerator(), random, new BlockPos(spike.getCenterX(), 45, spike.getCenterZ()));
				spike.guarded = wasGuarded;
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
		if (source.is(DamageTypeTags.IS_EXPLOSION) && !source.getMsgId().equals("fireworks"))
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
