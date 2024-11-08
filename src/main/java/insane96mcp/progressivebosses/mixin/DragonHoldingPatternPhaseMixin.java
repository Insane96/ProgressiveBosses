package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.data.DragonAttack;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonHoldingPatternPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonHoldingPatternPhase.class)
public class DragonHoldingPatternPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow
	public Path currentPath;

	public DragonHoldingPatternPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@Inject(at = @At("HEAD"), method = "findNewTarget()V", cancellable = true)
	private void findNewTarget(CallbackInfo callback) {
		if (this.currentPath == null || !this.currentPath.isDone())
			return;

		if (DragonAttack.onPhaseEnd(this.dragon))
			callback.cancel();
	}

	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return EnderDragonPhase.HOLDING_PATTERN;
	}
}
