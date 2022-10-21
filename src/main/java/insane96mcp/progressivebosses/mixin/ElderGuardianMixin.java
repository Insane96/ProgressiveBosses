package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.elderguardian.feature.AttackFeature;
import net.minecraft.world.entity.monster.ElderGuardian;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElderGuardian.class)
public class ElderGuardianMixin {

	@Inject(at = @At("HEAD"), method = "getAttackDuration()I", cancellable = true)
	private void getAttackDuration(CallbackInfoReturnable<Integer> callback) {
		callback.setReturnValue(AttackFeature.getAttackDuration((ElderGuardian) (Object) this));
	}
}
