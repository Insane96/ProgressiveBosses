package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.base.Modules;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChargingPlayerPhase.class)
public class MixinChargingPlayerPhase {

	@Inject(at = @At("HEAD"), method = "getMaxRiseOrFall()F", cancellable = true)
	private void getMaxRiseOrFall(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragonModule.attackFeature.increaseMaxRiseAndFall)
			callback.setReturnValue(24f);
	}
}
