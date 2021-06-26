package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.boss.dragon.phase.HoverPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HoverPhase.class)
public class MixinHoverPhase {

	@Inject(at = @At("HEAD"), method = "getMaxRiseOrFall()F", cancellable = true)
	private void getMaxRiseOrFall(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(32f);
	}

	@Inject(at = @At("HEAD"), method = "getIsStationary()Z", cancellable = true)
	private void getIsStationary(CallbackInfoReturnable<Boolean> callback) {
		callback.setReturnValue(false);
	}
}
