package insane96mcp.progressivebosses.mixin;

import insane96mcp.insanelib.base.Feature;
import insane96mcp.progressivebosses.module.elderguardian.ElderGuardianFeature;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStats;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ElderGuardian.class)
public class ElderGuardianMixin extends Guardian {

	public ElderGuardianMixin(EntityType<? extends Guardian> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(at = @At("HEAD"), method = "getAttackDuration()I", cancellable = true)
	private void getAttackDuration(CallbackInfoReturnable<Integer> callback) {
		Optional<ElderGuardianStats> stats = ElderGuardianFeature.getStats((ElderGuardian) (Object) this);
		if (stats.isEmpty())
			return;
		int attackDuration = stats.get().attackDuration;
		if (this.clientSideAttackTime > attackDuration)
			this.clientSideAttackTime = attackDuration;
		callback.setReturnValue(attackDuration);
	}

	@ModifyConstant(method = "customServerAiStep", constant = @Constant(doubleValue = 50d))
	public double onMiningFatigueRange(double range) {
		if (Feature.isEnabled(ElderGuardianFeature.class) && ElderGuardianFeature.adventure)
			return 0;
		return range;
	}
}
