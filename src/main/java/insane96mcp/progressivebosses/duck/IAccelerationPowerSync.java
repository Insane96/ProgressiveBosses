package insane96mcp.progressivebosses.duck;

/**
 * Duck interface implemented (via mixin) by {@link net.minecraft.world.entity.projectile.AbstractHurtingProjectile}
 * to set its acceleration power AND immediately mirror it into synched entity data, so the value is present in the
 * spawn packet and the client doesn't have to wait for a motion update to catch up.
 */
public interface IAccelerationPowerSync {
    void progressivebosses$setAccelerationPower(double accelerationPower);
}
