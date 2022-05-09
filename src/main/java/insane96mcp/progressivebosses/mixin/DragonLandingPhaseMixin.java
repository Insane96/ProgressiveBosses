package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonLandingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(DragonLandingPhase.class)
public class DragonLandingPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow @Nullable private Vec3 targetLocation;

	public DragonLandingPhaseMixin(EnderDragon dragon) {
		super(dragon);
	}

	@Inject(at = @At("HEAD"), method = "getFlySpeed()F", cancellable = true)
	private void getFlySpeed(CallbackInfoReturnable<Float> callback) {
		if (Modules.dragon.attack.increaseMaxRiseAndFall)
			callback.setReturnValue(12f);
	}

	@Inject(at = @At("HEAD"), method = "getTurnSpeed()F", cancellable = true)
	private void getTurnSpeed(CallbackInfoReturnable<Float> callback) {
		float f = (float)this.dragon.getDeltaMovement().horizontalDistance() + 1.0F;
		float f1 = Math.min(f, 40.0F);
		callback.setReturnValue(0.925f / f1 / f);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V", shift = At.Shift.AFTER), method = "doServerTick")
	private void setCorrectSittingPosition(CallbackInfo ci) {
		//noinspection ConstantConditions since I call it after the dragon reaches the center podium it shouldn't be null
		this.dragon.setPos(this.targetLocation);
	}

	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() {
		return EnderDragonPhase.LANDING;
	}
}
