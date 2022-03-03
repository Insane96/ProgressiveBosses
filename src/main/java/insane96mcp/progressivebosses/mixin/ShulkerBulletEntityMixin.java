package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.setup.Strings;
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

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V", cancellable = true)
	protected void onEntityHit(EntityHitResult rayTraceResult, CallbackInfo callbackInfo) {
		ShulkerBullet $this = (ShulkerBullet) (Object) this;
		if ($this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
			//if (Modules.dragon.minion.onBulletEntityHit())
			((LivingEntity)rayTraceResult.getEntity()).addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 150));
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo callback) {
		Modules.dragon.minion.onBulletTick((ShulkerBullet) (Object) this);
	}
}
