package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.boss.dragon.phase.LandingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LandingPhase.class)
public class MixinLandingPhase {

	@Inject(at = @At("HEAD"), method = "getMaxRiseOrFall()F", cancellable = true)
	private void getMaxRiseOrFall(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(12f);
	}
}
