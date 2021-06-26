package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.boss.dragon.phase.HoldingPatternPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HoldingPatternPhase.class)
public class MixinHoldingPatternPhase {

	@Inject(at = @At("HEAD"), method = "findNewTarget()V", cancellable = true)
	private void findNewTarget(CallbackInfo callback) {
		if (Modules.dragon.attack.onHoldingPatternFindNewTarget((HoldingPatternPhase)(Object)this))
			callback.cancel();
	}
}
