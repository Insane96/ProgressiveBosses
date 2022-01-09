package insane96mcp.progressivebosses.module.dragon.ai;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;

public class PBNearestAttackableTargetGoal extends NearestAttackableTargetGoal<PlayerEntity> {
    public PBNearestAttackableTargetGoal(MobEntity shulkerEntity) {
        super(shulkerEntity, PlayerEntity.class, 0, false, false, null);
        this.targetConditions.allowUnseeable();
    }

    public boolean canUse() {
        return this.mob.level.getDifficulty() != Difficulty.PEACEFUL && super.canUse();
    }


    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        Direction direction = ((ShulkerEntity) this.mob).getAttachFace();

        if (direction.getAxis() == Direction.Axis.X) {
            return this.mob.getBoundingBox().inflate(4.0D, targetDistance, targetDistance);
        } else {
            return direction.getAxis() == Direction.Axis.Z ? this.mob.getBoundingBox().inflate(targetDistance, targetDistance, 4.0D) : this.mob.getBoundingBox().inflate(targetDistance, 4.0D, targetDistance);
        }
    }
}
