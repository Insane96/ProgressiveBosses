package insane96mcp.progressivebosses.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractDragonSittingPhase.class)
public abstract class AbstractDragonSittingPhaseMixin extends AbstractDragonPhaseInstance {

	public AbstractDragonSittingPhaseMixin(EnderDragon pDragon) {
		super(pDragon);
	}

	@WrapOperation(method = "onHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setSecondsOnFire(I)V"))
	public void onProjectileSetOnFire(Entity instance, int pSeconds, Operation<Void> original, DamageSource source, float amount) {
		if (source.getDirectEntity() instanceof AbstractArrow abstractArrow) {
			abstractArrow.setYRot(abstractArrow.getYRot() + 180.0F);
			abstractArrow.yRotO += 180.0F;
			abstractArrow.setDeltaMovement(abstractArrow.getDeltaMovement().scale(5d));
		}
	}
}
