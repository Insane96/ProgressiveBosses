package insane96mcp.progressivebosses.duck;

import net.minecraft.network.syncher.SynchedEntityData;

/**
 * Duck interface implemented (via mixin) by {@link net.minecraft.world.entity.projectile.AbstractHurtingProjectile}
 * to set its acceleration power AND immediately mirror it into synched entity data, so the value is present in the
 * spawn packet and the client doesn't have to wait for a motion update to catch up.
 */
public interface IAccelerationPowerSync {
    /**
     * Called from {@link net.minecraft.world.entity.Entity}'s constructor right after {@code defineSynchedData}, since
     * vanilla (and modded) subclasses like {@code Fireball} and {@code WitherSkull} override it without calling super.
     */
    void progressivebosses$defineAccelerationPower(SynchedEntityData.Builder builder);

    void progressivebosses$setAccelerationPower(double accelerationPower);
}
