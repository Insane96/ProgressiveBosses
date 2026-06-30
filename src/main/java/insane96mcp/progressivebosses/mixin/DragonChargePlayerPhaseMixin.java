package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import insane96mcp.progressivebosses.module.dragon.data.PhaseChanger;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = DragonChargePlayerPhase.class, priority = 1001)
public abstract class DragonChargePlayerPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow public abstract EnderDragonPhase<DragonChargePlayerPhase> getPhase();

	public DragonChargePlayerPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=10"))
	public int progressivebosses$timeBeforeHoldingPattern(int original) {
		return 20;
	}

	@WrapOperation(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhaseManager;setPhase(Lnet/minecraft/world/entity/boss/enderdragon/phases/EnderDragonPhase;)V"))
	public void progressivebosses$onPhaseChange(EnderDragonPhaseManager instance, EnderDragonPhase<?> pPhase, Operation<Void> original) {
		DragonDefinition definition = DragonFeature.getDragonDefinition(this.dragon).orElse(null);
		if (definition == null || !PhaseChanger.trySetNewPhase(this.dragon, definition, this.getPhase()))
			original.call(instance, pPhase);
	}
}
