package insane96mcp.progressivebosses.module.dragon.ai;

import net.minecraft.core.Direction;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class PBNearestAttackableTargetGoal extends NearestAttackableTargetGoal<Player> {
    public PBNearestAttackableTargetGoal(Mob mob) {
        super(mob, Player.class, 0, false, false, null);
        //allowUnseeable
        this.targetConditions.ignoreLineOfSight();
    }

    public boolean canUse() {
        return this.mob.level.getDifficulty() != Difficulty.PEACEFUL && super.canUse();
    }


    protected AABB getTargetableArea(double targetDistance) {
        Direction direction = ((Shulker) this.mob).getAttachFace();

        if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, targetDistance, targetDistance);
        } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(targetDistance, targetDistance, 4.0D) : this.mob.getBoundingBox().inflate(targetDistance, 4.0D, targetDistance);
        }
    }
}
