package insane96mcp.progressivebosses.module.wither.ai.minion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class MinionNearestAttackableTargetGoal extends NearestAttackableTargetGoal<Player> {
	public MinionNearestAttackableTargetGoal(Mob goalOwner, Class<Player> targetClass, int targetChance, boolean checkSight, boolean nearbyOnly, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwner, targetClass, targetChance, checkSight, nearbyOnly, targetPredicate);
		this.targetConditions = TargetingConditions.DEFAULT.range(this.getFollowDistance()).ignoreLineOfSight().selector(targetPredicate);
	}
}
