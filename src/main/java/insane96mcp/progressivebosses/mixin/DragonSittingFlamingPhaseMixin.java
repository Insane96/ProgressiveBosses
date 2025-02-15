package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(DragonSittingFlamingPhase.class)
public abstract class DragonSittingFlamingPhaseMixin extends AbstractDragonPhaseInstance {

	@Shadow @Nullable private AreaEffectCloud flame;

	public DragonSittingFlamingPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=200", ordinal = 0))
	public int getRoarDuration(int original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats(this.dragon);
		return stats.map(dragonStats -> dragonStats.sittingFlamingTime).orElse(original);
	}

	@ModifyExpressionValue(method = "doServerTick", at = @At(value = "CONSTANT", args = "intValue=10", ordinal = 0))
	public int progressivebosses$timeBeforeSummonCloud(int original) {
		return 0;
	}

	@ModifyArg(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
	public MobEffectInstance cloudBreathEffect(MobEffectInstance pEffectInstance) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.strongerFlamingCloud)
			return pEffectInstance;
		return new MobEffectInstance(MobEffects.HARM, 1, 1);
	}

	@Inject(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/AreaEffectCloud;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)V", shift = At.Shift.AFTER))
	public void onCloudSpawn(CallbackInfo ci) {
		if (this.flame != null)
			this.flame.setWaitTime(0);
	}
}
