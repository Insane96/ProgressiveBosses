package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBulletEntity.class)
public abstract class MixinShulkerBulletEntity extends ProjectileEntity {

	public MixinShulkerBulletEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.noClip = true;
	}

	@Override
	protected void onEntityHit(EntityRayTraceResult rayTraceResult) {
		super.onEntityHit(rayTraceResult);
		Modules.dragon.minion.onBulletEntityHit((ShulkerBulletEntity) (Object) this, rayTraceResult);
	}

	@Inject(at = @At("HEAD"), method = "tick()V", cancellable = true)
	public void tick(CallbackInfo callback) {
		Modules.dragon.minion.onBulletTick((ShulkerBulletEntity) (Object) this);
	}
}
