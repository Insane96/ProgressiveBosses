package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragon.class)
public class EnderDragonMixin extends Mob {
	protected EnderDragonMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@Inject(at = @At("HEAD"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
		EnderDragon $this = (EnderDragon) (Object) this;
		if (source.is(DamageTypes.THORNS)) {
			$this.hurt($this.getSubEntities()[2], source, amount);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/enderdragon/EnderDragon;reallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z", shift = At.Shift.AFTER), method = "hurt(Lnet/minecraft/world/entity/boss/EnderDragonPart;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void onReallyHurt(EnderDragonPart part, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> callbackInfo) {
		EnderDragon $this = (EnderDragon) (Object) this;
		if (this.isDeadOrDying() && $this.getPhaseManager().getCurrentPhase().getPhase().equals(CrystalRespawnPhase.getPhaseType())) {
			$this.setHealth(1.0F);
			$this.getPhaseManager().setPhase(EnderDragonPhase.DYING);
		}
	}
}
