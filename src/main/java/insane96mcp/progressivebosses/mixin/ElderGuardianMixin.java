package insane96mcp.progressivebosses.mixin;

import insane96mcp.progressivebosses.module.elderguardian.feature.AttackFeature;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ElderGuardian.class)
public class ElderGuardianMixin extends Guardian {

	public ElderGuardianMixin(EntityType<? extends Guardian> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(at = @At("HEAD"), method = "getAttackDuration()I", cancellable = true)
	private void getAttackDuration(CallbackInfoReturnable<Integer> callback) {
		if (!AttackFeature.shouldChangeAttackDuration((ElderGuardian) (Object) this))
			return;
		int attackDuration = AttackFeature.getAttackDuration((ElderGuardian) (Object) this);
		if (this.clientSideAttackTime > attackDuration)
			this.clientSideAttackTime = attackDuration;
		callback.setReturnValue(attackDuration);
	}
}
