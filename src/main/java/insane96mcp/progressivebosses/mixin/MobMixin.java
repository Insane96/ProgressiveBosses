package insane96mcp.progressivebosses.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity {

	protected MobMixin(EntityType<? extends AbstractGolem> p_27508_, Level p_27509_) {
		super(p_27508_, p_27509_);
	}

	@Inject(method = "dropFromLootTable", at = @At("HEAD"), cancellable = true)
	public void progressiveBosses$preventDragonLoot(DamageSource pDamageSource, boolean pAttackedRecently, CallbackInfo ci) {
		if (this.getType() == EntityType.ENDER_DRAGON)
			ci.cancel();
	}
}
