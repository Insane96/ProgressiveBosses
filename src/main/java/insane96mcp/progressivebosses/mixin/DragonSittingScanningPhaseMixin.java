package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingScanningPhase;
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
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
        return stats.map(dragonStats -> dragonStats.sittingScanningIdleTime).orElse(original);
    }
}
