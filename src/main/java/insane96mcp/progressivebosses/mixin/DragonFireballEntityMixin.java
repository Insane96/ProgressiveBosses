package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonFireballEntity.class)
public class DragonFireballEntityMixin extends DamagingProjectileEntity {

	protected DragonFireballEntityMixin(EntityType<? extends DamagingProjectileEntity> p_i50173_1_, World p_i50173_2_) {
		super(p_i50173_1_, p_i50173_2_);
	}

	@Inject(at = @At("HEAD"), method = "onImpact(Lnet/minecraft/util/math/RayTraceResult;)V", cancellable = true)
	private void onImpact(RayTraceResult result, CallbackInfo callback) {
		if (Modules.dragon.attack.onFireballImpact((DragonFireballEntity) (Object) this, this.getShooter(), result))
			callback.cancel();
	}
}
