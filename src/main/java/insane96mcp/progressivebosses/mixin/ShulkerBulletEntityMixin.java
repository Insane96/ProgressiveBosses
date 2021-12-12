package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletEntityMixin extends Projectile {

	public ShulkerBulletEntityMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	/*@Override
	protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
		super.onEntityHit(rayTraceResult);
		Modules.dragon.minion.onBulletEntityHit((ShulkerBulletEntity) (Object) this, rayTraceResult);
	}*/

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V")
	protected void onEntityHit(EntityHitResult rayTraceResult, CallbackInfo callbackInfo) {
		ShulkerBullet $this = (ShulkerBullet) (Object) this;
		if ($this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
			((LivingEntity)rayTraceResult.getEntity()).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 150));
	}

	@Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
	public void tick(CallbackInfo callback) {
		Modules.dragon.minion.onBulletTick((ShulkerBullet) (Object) this);
	}
}
