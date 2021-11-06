package insane96mcp.progressivebosses.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderDragonEntity.class)
public class EnderDragonEntityMixin extends MobEntity {
	protected EnderDragonEntityMixin(EntityType<? extends MobEntity> type, World worldIn) {
		super(type, worldIn);
	}

	@Inject(at = @At("HEAD"), method = "attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z", cancellable = true)
	private void onImpact(DamageSource source, float amount, CallbackInfoReturnable<Boolean> callback) {
		EnderDragonEntity $this = (EnderDragonEntity) (Object) this;
		if (source instanceof EntityDamageSource && !((EntityDamageSource)source).getIsThornsDamage()) {
			$this.attackEntityPartFrom($this.getDragonParts()[2], source, amount);
		}
	}
}
