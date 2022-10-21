package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.feature.AttackFeature;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonHoldingPatternPhase.class)
public class DragonHoldingPatternPhaseMixin {

	@Shadow
	public Path currentPath;

	@Inject(at = @At("HEAD"), method = "findNewTarget()V", cancellable = true)
	private void findNewTarget(CallbackInfo callback) {
		if (this.currentPath == null || !this.currentPath.isDone())
			return;

		if (AttackFeature.onPhaseEnd(((DragonHoldingPatternPhase)(Object)this).dragon))
			callback.cancel();
	}
}
