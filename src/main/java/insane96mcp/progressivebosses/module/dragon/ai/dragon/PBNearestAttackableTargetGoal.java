package insane96mcp.progressivebosses.module.dragon.ai.dragon;

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
        //allowUnseeable
        this.targetEntitySelector.setIgnoresLineOfSight();
    }

    public boolean shouldExecute() {
        return this.goalOwner.world.getDifficulty() != Difficulty.PEACEFUL && super.shouldExecute();
    }


    protected AxisAlignedBB getTargetableArea(double targetDistance) {
        Direction direction = ((ShulkerEntity) this.goalOwner).getAttachmentFacing();

        if (direction.getAxis() == Direction.Axis.X) {
            return this.goalOwner.getBoundingBox().grow(4.0D, targetDistance, targetDistance);
        } else {
            return direction.getAxis() == Direction.Axis.Z ? this.goalOwner.getBoundingBox().grow(targetDistance, targetDistance, 4.0D) : this.goalOwner.getBoundingBox().grow(targetDistance, 4.0D, targetDistance);
        }
    }
}
