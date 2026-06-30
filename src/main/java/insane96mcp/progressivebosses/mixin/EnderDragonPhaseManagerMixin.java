package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.event.PBEventFactory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(EnderDragonPhaseManager.class)
public abstract class EnderDragonPhaseManagerMixin {
	@Shadow @Final private EnderDragon dragon;

	@Shadow public abstract <T extends DragonPhaseInstance> T getPhase(EnderDragonPhase<T> pPhase);

	@Shadow @Nullable private DragonPhaseInstance currentPhase;

	@ModifyVariable(method = "setPhase", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
	public EnderDragonPhase<?> progressivebosses$setPhaseEvent(EnderDragonPhase<?> pPhase) {
        if (this.dragon.level().isClientSide || this.currentPhase == null)
			return pPhase;
		return PBEventFactory.onDragonChangePhase(this.dragon, this.currentPhase.getPhase(), pPhase);
	}

	@Inject(method = "setPhase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;end()V", shift = At.Shift.AFTER))
	public void progressivebosses$phaseEndEvent(EnderDragonPhase<?> pPhase, CallbackInfo ci) {
		if (this.dragon.level().isClientSide)
			return;
		PBEventFactory.onDragonPhaseEnd(this.dragon, this.currentPhase);
	}

	@Inject(method = "setPhase", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/DragonPhaseInstance;begin()V", shift = At.Shift.AFTER))
	public void progressivebosses$phaseBeginEvent(EnderDragonPhase<?> pPhase, CallbackInfo ci) {
		if (this.dragon.level().isClientSide || this.currentPhase == null)
			return;
		PBEventFactory.onDragonPhaseBegin(this.dragon, this.getPhase(pPhase));
	}
}
