package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinition;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(DragonSittingScanningPhase.class)
public abstract class DragonSittingScanningPhaseMixin extends AbstractDragonPhaseInstance {

	public DragonSittingScanningPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=100"))
	public int getSittingScanningIdleTime(int original) {
		if (this.dragon.getPhaseManager().getPhase(EnderDragonPhase.SITTING_FLAMING).flameCount == 0)
			return original;
		Optional<DragonDefinition> stats = DragonFeature.getDragonStats(this.dragon);
		return stats.map(dragonStats -> {
			int sittingScanningIdleTime = dragonStats.sittingScanningIdleTime;
			if (DragonAnger.isAngered(this.dragon))
				sittingScanningIdleTime /= 2;
			return sittingScanningIdleTime;
		}).orElse(original);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "floatValue=0.7"))
	public float getMaxRotation(float original) {
		return 2.5f;
	}
}
