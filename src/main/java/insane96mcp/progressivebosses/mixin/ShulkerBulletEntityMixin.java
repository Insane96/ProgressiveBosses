package insane96mcp.progressivebosses.mixin;

import com.google.common.base.MoreObjects;
import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletEntityMixin extends Projectile {

	public ShulkerBulletEntityMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	@Redirect(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
	private boolean onLevitationApply(LivingEntity livingEntity, MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
		if (this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
			return livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 150), MoreObjects.firstNonNull(entity, this));
		else
			return ???;
	}

	/*@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), method = "onHitEntity(Lnet/minecraft/world/phys/EntityHitResult;)V")
	private void onEntityHit(MobEffectInstance instance, Entity entity) {
		//ShulkerBullet $this = (ShulkerBullet) (Object) this;
		//if ($this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
		//	entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 150));
	}*/

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo callback) {
		Modules.dragon.minion.onBulletTick((ShulkerBullet) (Object) this);
	}
}
