package net.insane96mcp.progressivebosses.events.entities.ai;

import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;

public class DragonMinionAIAttackNearest extends EntityAINearestAttackableTarget<EntityPlayer>
{
    public DragonMinionAIAttackNearest(EntityShulker shulker)
    {
        super(shulker, EntityPlayer.class, false);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        return this.taskOwner.world.getDifficulty() == EnumDifficulty.PEACEFUL ? false : super.shouldExecute();
    }

    protected AxisAlignedBB getTargetableArea(double targetDistance)
    {
        EnumFacing enumfacing = ((EntityShulker)this.taskOwner).getAttachmentFacing();

        if (enumfacing.getAxis() == EnumFacing.Axis.X)
        {
            return this.taskOwner.getEntityBoundingBox().grow(4.0D, targetDistance, targetDistance);
        }
        else
        {
            return enumfacing.getAxis() == EnumFacing.Axis.Z ? this.taskOwner.getEntityBoundingBox().grow(targetDistance, targetDistance, 4.0D) : this.taskOwner.getEntityBoundingBox().grow(targetDistance, 4.0D, targetDistance);
        }
    }
}