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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EnderDragonPhaseManager.class)
public abstract class EnderDragonPhaseManagerMixin {
	@Shadow @Final private EnderDragon dragon;

	@Shadow public abstract DragonPhaseInstance getCurrentPhase();

	@ModifyVariable(method = "setPhase", at = @At(value = "HEAD", ordinal = 0), argsOnly = true)
	public EnderDragonPhase<?> progressivebosses$setPhaseEvent(EnderDragonPhase<?> pPhase) {
		if (this.dragon.level().isClientSide)
			return pPhase;
		return PBEventFactory.onDragonChangePhase(this.dragon, this.getCurrentPhase() == null ? null : this.getCurrentPhase().getPhase(), pPhase);
	}
}
