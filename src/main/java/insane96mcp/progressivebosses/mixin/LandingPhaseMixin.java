package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonLandingPhase.class)
public class LandingPhaseMixin {

	@Inject(at = @At("HEAD"), method = "getFlySpeed()F", cancellable = true)
	private void getFlySpeed(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(12f);
	}
}
