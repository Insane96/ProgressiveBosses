package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(DragonSittingFlamingPhase.class)
public abstract class DragonSittingFlamingPhaseMixin extends AbstractDragonPhaseInstance {

	public DragonSittingFlamingPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=200", ordinal = 0))
	public int getRoarDuration(int original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
        return stats.map(dragonStats -> dragonStats.sittingFlamingTime).orElse(original);
    }
}
