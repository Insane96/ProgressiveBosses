package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.ChargingPlayerPhase;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.util.math.vector.Vector3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChargingPlayerPhase.class)
public class MixinChargingPlayerPhase extends Phase {

	@Shadow
	@Final
	private static Logger LOGGER = LogManager.getLogger();
	@Shadow
	private int timeSinceCharge;
	@Shadow
	private Vector3d targetLocation;

	public MixinChargingPlayerPhase(EnderDragonEntity dragonIn) {
		super(dragonIn);
	}

	@Override
	public void serverTick() {
		if (this.targetLocation == null) {
			LOGGER.warn("Aborting charge player as no target was set.");
			this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
		} else if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 10) {
			if (!Modules.dragon.attack.onPhaseEnd(this.dragon))
				this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
		} else {
			double d0 = this.targetLocation.squareDistanceTo(this.dragon.getPosX(), this.dragon.getPosY(), this.dragon.getPosZ());
			if (d0 < 100.0D || d0 > 22500.0D || this.dragon.collidedHorizontally || this.dragon.collidedVertically) {
				++this.timeSinceCharge;
			}

		}
	}

	@Inject(at = @At("HEAD"), method = "getMaxRiseOrFall()F", cancellable = true)
	private void getMaxRiseOrFall(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(24f);
	}

	@Override
	public PhaseType<? extends IPhase> getType() {
		return PhaseType.CHARGING_PLAYER;
	}
}
