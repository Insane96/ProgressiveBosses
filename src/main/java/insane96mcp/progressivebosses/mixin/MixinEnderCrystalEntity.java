package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderCrystalEntity.class)
public class MixinEnderCrystalEntity {

	@Inject(at = @At("HEAD"), method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", cancellable = true)
	private void onImpact(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
		if (Modules.dragon.crystal.onDamageFromExplosion((EnderCrystalEntity) (Object) this, source))
			callback.setReturnValue(false);
	}
}
