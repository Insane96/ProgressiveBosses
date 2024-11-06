package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends Projectile {

	public ShulkerBulletMixin(EntityType<? extends Projectile> entityType, Level world) {
		super(entityType, world);
		this.noPhysics = true;
	}

	@ModifyArg(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"), index = 0)
	private MobEffectInstance applyBlindness(MobEffectInstance mobEffectInstance) {
		if (this.getPersistentData().getBoolean(Strings.Tags.BLINDNESS_BULLET)) {
			int duration = 150;
			if (this.getOwner() != null) {
				Optional<DragonStats> stats = DragonFeature.getDragonStats(this.getOwner().getPersistentData().getByte(DragonFeature.LEVEL));
				if (stats.isPresent())
					duration = stats.get().minion.blindingDuration;
			}
			return new MobEffectInstance(MobEffects.BLINDNESS, duration);
		}
		else
			return mobEffectInstance;
	}

	@Inject(at = @At("HEAD"), method = "tick()V")
	public void tick(CallbackInfo callback) {
		DragonFeature.onBulletTick((ShulkerBullet) (Object) this);
	}
}
