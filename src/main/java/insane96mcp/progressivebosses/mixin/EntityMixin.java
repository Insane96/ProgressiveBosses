package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import insane96mcp.progressivebosses.duck.IAccelerationPowerSync;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Not injected in AbstractHurtingProjectile#defineSynchedData since subclasses (Fireball, WitherSkull, other mods' projectiles) override it without calling super
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", shift = At.Shift.AFTER))
    private void progressivebosses$defineAccelerationPower(EntityType<?> entityType, Level level, CallbackInfo ci, @Local SynchedEntityData.Builder builder) {
        if (this instanceof IAccelerationPowerSync accelerationPowerSync)
            accelerationPowerSync.progressivebosses$defineAccelerationPower(builder);
    }
}
