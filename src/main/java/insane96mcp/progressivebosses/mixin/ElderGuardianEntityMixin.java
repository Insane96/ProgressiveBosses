package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.monster.ElderGuardianEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElderGuardianEntity.class)
public class ElderGuardianEntityMixin {

	@Inject(at = @At("HEAD"), method = "getAttackDuration()I", cancellable = true)
	private void getAttackDuration(CallbackInfoReturnable<Integer> callback) {
		callback.setReturnValue(Modules.elderGuardian.attack.getAttackDuration((ElderGuardianEntity) (Object) this));
	}
}
