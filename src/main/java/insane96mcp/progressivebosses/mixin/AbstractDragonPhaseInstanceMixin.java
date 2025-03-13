package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.FlySpeedComponent;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractDragonPhaseInstance.class)
public abstract class AbstractDragonPhaseInstanceMixin {
	@Shadow @Final public EnderDragon dragon;

	@ModifyExpressionValue(method = "getTurnSpeed", at = @At(value = "CONSTANT", args = "floatValue=40.0"))
	public float progressivebosses$maxSpeedForTurning(float original) {
		return original * DragonFeature.getDragonDefinition(this.dragon)
				.flatMap(definition -> definition.getComponent(FlySpeedComponent.class))
				.map(flySpeedComponent -> flySpeedComponent.getFlySpeedMultiplier(this.dragon))
				.orElse(1f) * 1.5f;
	}

	@ModifyExpressionValue(method = "getTurnSpeed", at = @At(value = "CONSTANT", args = "floatValue=0.7"))
	public float progressivebosses$speedReductionForTurning(float original) {
		float flySpeedBonus = DragonFeature.getDragonDefinition(this.dragon)
				.flatMap(definition -> definition.getComponent(FlySpeedComponent.class))
				.map(flySpeedComponent -> flySpeedComponent.getFlySpeedMultiplier(this.dragon))
				.orElse(1f) - 1f;
		return original + (flySpeedBonus * 0.6f);
	}
}
