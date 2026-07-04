package insane96mcp.progressivebosses.mixin;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.module.dragon.data.MinionComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Shulker.class)
public abstract class ShulkerMixin extends AbstractGolem implements Enemy {

	protected ShulkerMixin(EntityType<? extends AbstractGolem> p_27508_, Level p_27509_) {
		super(p_27508_, p_27509_);
	}

	@Inject(at = @At("HEAD"), method = "hitByShulkerBullet()V", cancellable = true)
	public void progressivebosses$preventMinionSelfLevitation(CallbackInfo callback) {
		if (ModNBTData.get(this, MinionComponent.DRAGON_MINION, Boolean.class))
			callback.cancel();
	}
}
