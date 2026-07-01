package insane96mcp.progressivebosses.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla stopped networking a projectile's acceleration (in 1.20.1 {@code xPower/yPower/zPower} were sent via the
 * spawn packet's velocity field; in 1.21 the spawn packet only carries the delta movement and {@code accelerationPower}
 * is left at its client-side default). When we boost an acid ball's {@code accelerationPower} server-side the client
 * keeps accelerating with the default 0.1 and lags behind until a motion update arrives a few ticks later.
 * <p>
 * This mixin syncs {@code accelerationPower} through entity data so it's present in the spawn bundle and stays in sync
 * afterwards (e.g. when {@code onDeflection} changes it).
 */
@Mixin(AbstractHurtingProjectile.class)
public abstract class AbstractHurtingProjectileMixin extends Entity implements IAccelerationPowerSync {
    @Unique
    private static final EntityDataAccessor<Float> PB$ACCELERATION_POWER =
            SynchedEntityData.defineId(AbstractHurtingProjectile.class, EntityDataSerializers.FLOAT);

    @Shadow
    public double accelerationPower;

    public AbstractHurtingProjectileMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void progressivebosses$defineAccelerationPower(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(PB$ACCELERATION_POWER, 0.1f);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void progressivebosses$syncAccelerationPower(CallbackInfo ci) {
        if (this.level().isClientSide)
            this.accelerationPower = this.getEntityData().get(PB$ACCELERATION_POWER);
        else
            this.getEntityData().set(PB$ACCELERATION_POWER, (float) this.accelerationPower);
    }

    @Override
    public void progressivebosses$setAccelerationPower(double accelerationPower) {
        this.accelerationPower = accelerationPower;
        this.getEntityData().set(PB$ACCELERATION_POWER, (float) accelerationPower);
    }
}
