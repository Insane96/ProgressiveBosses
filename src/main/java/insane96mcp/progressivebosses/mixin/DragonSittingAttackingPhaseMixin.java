package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingAttackingPhase;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(DragonSittingAttackingPhase.class)
public abstract class DragonSittingAttackingPhaseMixin extends AbstractDragonPhaseInstance {

	public DragonSittingAttackingPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@WrapOperation(method = "doClientTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
	public void onPlayGrowlSound(Level instance, double pX, double pY, double pZ, SoundEvent pSound, SoundSource pCategory, float pVolume, float pPitch, boolean pDistanceDelay, Operation<Void> original) {
		if (this.dragon.tickCount % 5 != 0
				|| !Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return;
		original.call(instance, pX, pY, pZ, pSound, pCategory, pVolume, pPitch, pDistanceDelay);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=40"))
	public int getRoarDuration(int original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
        return stats.map(dragonStats -> {
			int roarTime = dragonStats.roarTime;
			if (DragonAnger.isAngered(this.dragon))
				roarTime /= 2;
			return roarTime;
		}).orElse(original);
    }
}
