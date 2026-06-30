package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import insane96mcp.progressivebosses.module.dragon.data.PhaseChanger;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DragonTakeoffPhase.class)
public abstract class DragonTakeoffPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow public abstract EnderDragonPhase<DragonChargePlayerPhase> getPhase();

	public DragonTakeoffPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@WrapOperation(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V"))
	public void progressivebosses$onPhaseChange(EnderDragonPhaseManager instance, EnderDragonPhase<?> pPhase, Operation<Void> original) {
		DragonDefinition definition = DragonFeature.getDragonDefinition(this.dragon).orElse(null);
		if (definition == null || !PhaseChanger.trySetNewPhase(this.dragon, definition, this.getPhase()))
			original.call(instance, pPhase);
	}
}
