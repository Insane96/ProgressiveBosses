package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(EnderDragon.class)
@Debug(export = true)
public class EnderDragonMixin extends Mob {

	@Shadow public float oFlapTime;

	@Mutable
	@Shadow @Final private EnderDragonPart[] subEntities;

	@Mutable
	@Shadow @Final public EnderDragonPart head;
	@Shadow @Final private EnderDragonPart neck;
	@Shadow @Final private EnderDragonPart body;
	@Shadow @Final private EnderDragonPart tail1;
	@Shadow @Final private EnderDragonPart tail2;
	@Shadow @Final private EnderDragonPart tail3;
	@Shadow @Final private EnderDragonPart wing1;
	@Shadow @Final private EnderDragonPart wing2;

	protected EnderDragonMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@ModifyExpressionValue(method = "<init>", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float onHeadSize(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return 1.5f;
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;reallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER), method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void onReallyHurt(EnderDragonPart part, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
		EnderDragon $this = (EnderDragon) (Object) this;
		if (this.isDeadOrDying() && $this.getPhaseManager().getCurrentPhase().getPhase().equals(CrystalRespawnPhase.getPhaseType())) {
			$this.setHealth(1.0F);
			$this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
		}
	}

	@ModifyExpressionValue(method = "checkCrystals", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
	public float onCrystalHeal(float original) {
		Optional<DragonStats> stats = DragonFeature.getDragonStats((EnderDragon) (Object) this);
		//Divided by 2 because it's healed twice per second
		return stats.map(dragonStats -> dragonStats.health.crystalRegeneration / 2f).orElse(original);
	}

	@ModifyExpressionValue(method = "onCrystalDestroyed", at = @At(value = "CONSTANT", args = "floatValue=10.0"))
	public float onAttachedCrystalDamage(float original, EndCrystal pCrystal, BlockPos pPos, DamageSource pDamageSource) {
		if (!pCrystal.showsBottom())
			return original;
		return this.getMaxHealth() * 0.15f;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "doubleValue=0.01"))
	public double onYDeltaSpeed(double original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return 0.1d;
	}

	@ModifyVariable(method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z", at = @At(value = "STORE", ordinal = 0), argsOnly = true)
	public float onDamageAmount(float original, EnderDragonPart part, DamageSource source, float amount) {
		if (!part.name.equals("wing") && !part.name.equals("neck"))
			return original;
		return original * 1.5f;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 0))
	public float neckOffsetX(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=5.5", ordinal = 2))
	public float neckOffsetZ(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.neckOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 0))
	public float headOffsetX(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "CONSTANT", args = "floatValue=6.5", ordinal = 2))
	public float headOffsetZ(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetXZ();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 0))
	public float headOffsetSittingY(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetSittingY();
	}

	@ModifyReturnValue(method = "getHeadYOffset", at = @At(value = "RETURN", ordinal = 1))
	public float headOffsetY(float original) {
		if (!Feature.isEnabled(DragonFeature.class)
				|| !DragonFeature.enableFixes)
			return original;
		return DragonFeature.headOffsetY(original);
	}
}
