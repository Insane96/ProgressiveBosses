package insane96mcp.progressivebosses.mixin;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EnderDragonEntity.class)
public class MixinEnderDragonEntity {

	/*@Inject(at = @At("HEAD"), method = "onImpact(Lnet/minecraft/util/math/RayTraceResult;)V", cancellable = true)
	private void onImpact(RayTraceResult result, CallbackInfo callback) {
		if (Modules.dragon.attack.onFireballImpact((DragonFireballEntity) (Object) this, this.func_234616_v_(), result))
			callback.cancel();
	}*/
}
