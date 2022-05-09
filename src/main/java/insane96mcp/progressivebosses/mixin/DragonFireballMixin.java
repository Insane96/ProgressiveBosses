package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireball.class)
public class DragonFireballMixin extends AbstractHurtingProjectile {

	protected DragonFireballMixin(EntityType<? extends AbstractHurtingProjectile> p_i50173_1_, Level p_i50173_2_) {
		super(p_i50173_1_, p_i50173_2_);
	}

	@Inject(at = @At("HEAD"), method = "onHit(Lnet/minecraft/world/phys/HitResult;)V", cancellable = true)
	private void onHit(HitResult result, CallbackInfo callback) {
		if (Modules.dragon.attack.onFireballImpact((DragonFireball) (Object) this, this.getOwner(), result))
			callback.cancel();
	}
}
