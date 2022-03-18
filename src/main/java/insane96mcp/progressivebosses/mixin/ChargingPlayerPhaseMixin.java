package insane96mcp.progressivebosses.mixin;

import com.mojang.logging.LogUtils;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DragonChargePlayerPhase.class)
public abstract class ChargingPlayerPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow
	@Final
	private static Logger LOGGER = LogUtils.getLogger();
	@Shadow
	private int timeSinceCharge;
	@Shadow
	private Vec3 targetLocation;

	public ChargingPlayerPhaseMixin(EnderDragon dragonIn) {
		super(dragonIn);
	}

	@Override
	public void doServerTick() {
		if (this.targetLocation == null) {
			LOGGER.warn("Aborting charge player as no target was set.");
			this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
		} else if (this.timeSinceCharge > 0 && this.timeSinceCharge++ >= 10) {
			//If must not charge or fireball then go back to holding pattern
			if (!Modules.dragon.attack.onPhaseEnd(this.dragon))
				this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
			//Otherwise reset the phase, in case she charges again
			else
				//Can't use initPhase() otherwise the target is reset. Also making the dragon take more time to restart the charging
				this.timeSinceCharge = -10;
		} else {
			double d0 = this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
			if (d0 < 100.0D || d0 > 22500.0D || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
				++this.timeSinceCharge;
			}

		}
	}

	@Inject(at = @At("HEAD"), method = "getFlySpeed()F", cancellable = true)
	private void getFlySpeed(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(24f);
	}

	@Shadow public abstract void begin();

	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() { return EnderDragonPhase.CHARGING_PLAYER; }
}
