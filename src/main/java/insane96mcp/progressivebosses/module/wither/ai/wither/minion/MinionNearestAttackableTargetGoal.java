package insane96mcp.progressivebosses.module.wither.ai.wither.minion;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class MinionNearestAttackableTargetGoal extends NearestAttackableTargetGoal<PlayerEntity> {
	public MinionNearestAttackableTargetGoal(MobEntity goalOwner, Class<PlayerEntity> targetClass, int targetChance, boolean checkSight, boolean nearbyOnly, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwner, targetClass, targetChance, checkSight, nearbyOnly, targetPredicate);
		this.targetEntitySelector = (new EntityPredicate()).setDistance(this.getTargetDistance()).setIgnoresLineOfSight().setCustomPredicate(targetPredicate);
	}
}
