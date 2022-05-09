package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {

	public ShulkerBulletMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), index = 0)
	private MobEffectInstance applyBlindness(MobEffectInstance mobEffectInstance) {
		if (this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET))
			return new MobEffectInstance(MobEffects.BLINDNESS, Modules.dragon.minion.blindingDuration);
		else
			return mobEffectInstance;
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
