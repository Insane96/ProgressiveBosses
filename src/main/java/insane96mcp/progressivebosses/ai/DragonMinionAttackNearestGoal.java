package insane96mcp.progressivebosses.ai;

import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Difficulty;

public class DragonMinionAttackNearestGoal extends NearestAttackableTargetGoal<PlayerEntity> {
    public DragonMinionAttackNearestGoal(ShulkerEntity shulkerEntity) {
        super(shulkerEntity, PlayerEntity.class, false);
    }

    public boolean shouldExecute() {
        return this.goalOwner.world.getDifficulty() == Difficulty.PEACEFUL ? false : super.shouldExecute();
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
