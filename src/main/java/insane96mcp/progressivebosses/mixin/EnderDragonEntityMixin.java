package insane96mcp.progressivebosses.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragon.class)
public class EnderDragonEntityMixin extends Mob {
	protected EnderDragonEntityMixin(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	@Inject(at = @At("HEAD"), method = "hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	private void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
		EnderDragon $this = (EnderDragon) (Object) this;
		if (source instanceof EntityDamageSource && !((EntityDamageSource)source).isThorns()) {
			$this.hurt($this.getSubEntities()[2], source, amount);
		}
	}
}
