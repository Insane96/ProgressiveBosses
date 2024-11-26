package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.progressivebosses.module.dragon.data.DragonMinion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {

	public ShulkerBulletMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	@WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
	public boolean onTryApplyLevitation(LivingEntity instance, MobEffectInstance mobEffectInstance, Entity pEffectInstance, Operation<Boolean> original) {
        if (!this.getPersistentData().contains("CustomPotionEffects"))
            return original.call(instance, mobEffectInstance, pEffectInstance);

        boolean hasAppliedAtLeastOneEffect = false;
        for (MobEffectInstance customEffectInstance : PotionUtils.getCustomEffects(this.getPersistentData())) {
            hasAppliedAtLeastOneEffect |= instance.addEffect(customEffectInstance);
        }
        return hasAppliedAtLeastOneEffect;
    }

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo callback) {
		DragonMinion.onBulletTick((ShulkerBullet) (Object) this);
	}
}
