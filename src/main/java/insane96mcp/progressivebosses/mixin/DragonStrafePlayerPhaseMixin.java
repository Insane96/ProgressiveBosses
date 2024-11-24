package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.dragon.data.DragonAttack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonStrafePlayerPhaseMixin extends AbstractDragonPhaseInstance {
	@Shadow
	private LivingEntity attackTarget;

	public DragonStrafePlayerPhaseMixin(EnderDragon dragonIn) {
		super(dragonIn);
	}

	@Inject(method = "doServerTick", at= @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
	public void doServerTick(CallbackInfo ci) {
		DragonAttack.fireFireball(dragon, attackTarget);
	}

	@Shadow public abstract void begin();

	@Override
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() { return EnderDragonPhase.STRAFE_PLAYER; }
}
