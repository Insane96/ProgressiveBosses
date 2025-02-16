package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.progressivebosses.module.dragon.data.DragonMinion;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {

	public ShulkerBulletMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	@WrapOperation(method = "onHitEntity", at = @At(value = "NEW", target = "(Lnet/minecraft/world/effect/MobEffect;I)Lnet/minecraft/world/effect/MobEffectInstance;"))
    private MobEffectInstance progressivebosses$levitationDuration(MobEffect effect, int amplifier, Operation<MobEffectInstance> original) {
		if (this.getOwner() != null && this.getOwner().getPersistentData().contains(DragonMinion.DRAGON_MINION))
			return new MobEffectInstance(MobEffects.LEVITATION, 500);
		return original.call(effect, amplifier);
	}
}
