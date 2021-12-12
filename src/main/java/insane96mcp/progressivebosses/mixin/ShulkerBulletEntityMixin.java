package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBulletEntity.class)
public abstract class ShulkerBulletEntityMixin extends ProjectileEntity {

	public ShulkerBulletEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	/*@Override
	protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
		super.onEntityHit(rayTraceResult);
		Modules.dragon.minion.onBulletEntityHit((ShulkerBulletEntity) (Object) this, rayTraceResult);
	}*/

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;addEffect(Lnet/minecraft/potion/EffectInstance;)Z"), method = "onHitEntity(Lnet/minecraft/util/math/EntityRayTraceResult;)V")
	protected void onEntityHit(EntityRayTraceResult rayTraceResult, CallbackInfo callbackInfo) {
		ShulkerBulletEntity $this = (ShulkerBulletEntity) (Object) this;
		if ($this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
			((LivingEntity)rayTraceResult.getEntity()).addEffect(new EffectInstance(Effects.BLINDNESS, 150));
	}

	@Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
	public void tick(CallbackInfo callback) {
		Modules.dragon.minion.onBulletTick((ShulkerBulletEntity) (Object) this);
	}
}
