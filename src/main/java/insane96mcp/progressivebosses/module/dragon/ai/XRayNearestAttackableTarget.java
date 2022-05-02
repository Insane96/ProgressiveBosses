package insane96mcp.progressivebosses.module.dragon.ai;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class XRayNearestAttackableTarget extends NearestAttackableTargetGoal<Player> {
    public XRayNearestAttackableTarget(Mob mob) {
        super(mob, Player.class, 0, false, false, null);
        this.targetConditions.ignoreLineOfSight();
    }

    public boolean canUse() {
        return this.mob.level.getDifficulty() != Difficulty.PEACEFUL && super.canUse();
    }
}
