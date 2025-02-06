package insane96mcp.progressivebosses.module.dragon.phase;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DragonFakeDeathPhase extends AbstractDragonPhaseInstance {
    private static EnderDragonPhase<DragonFakeDeathPhase> FAKE_DEATH;
    @Nullable
    private Vec3 targetLocation;

    public DragonFakeDeathPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    public void doServerTick() {
        if (this.targetLocation == null) {
            this.targetLocation = Vec3.atBottomCenterOf(this.dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin())));
        }

        if (this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ()) < 1.0D) {
            this.dragon.getPhaseManager().setPhase(DragonSecondPhase.getPhaseType());
            this.dragon.setPos(this.targetLocation);
        }

    }

    /**
     * Returns the maximum amount dragon may rise or fall during this phase
     */
    public float getFlySpeed() {
        return 1.5F;
    }

    public float getTurnSpeed() {
        float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
        float f1 = Math.min(f, 40.0F);
        return f1 / f;
    }

    /**
     * Called when this phase is set to active
     */
    public void begin() {
        this.targetLocation = null;
    }

    @Override
    public float onHurt(DamageSource p_31199_, float p_31200_) {
        return 0f;
    }

    /**
     * Returns the location the dragon is flying toward
     */
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    public @NotNull EnderDragonPhase<DragonFakeDeathPhase> getPhase() {
        return FAKE_DEATH;
    }

    public static EnderDragonPhase<DragonFakeDeathPhase> getPhaseType() {
        return FAKE_DEATH;
    }

    public static void init() {
        FAKE_DEATH = EnderDragonPhase.create(DragonFakeDeathPhase.class, "FakeDeath");
    }
}
