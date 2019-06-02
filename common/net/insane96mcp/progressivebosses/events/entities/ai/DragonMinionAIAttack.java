package net.insane96mcp.progressivebosses.events.entities.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.EnumDifficulty;

public class DragonMinionAIAttack extends EntityAIBase
{
    private int attackTime;
    protected final EntityShulker shulker;

    public DragonMinionAIAttack(EntityShulker shulker)
    {
    	this.shulker = shulker;
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        EntityLivingBase entitylivingbase = shulker.getAttackTarget();

        if (entitylivingbase != null && entitylivingbase.isEntityAlive())
        {
            return shulker.world.getDifficulty() != EnumDifficulty.PEACEFUL;
        }
        else
        {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
        this.attackTime = 20;
        shulker.updateArmorModifier(100);
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask()
    {
    	shulker.updateArmorModifier(0);
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void updateTask()
    {
        if (shulker.world.getDifficulty() != EnumDifficulty.PEACEFUL)
        {
            --this.attackTime;
            EntityLivingBase entitylivingbase = shulker.getAttackTarget();
            shulker.getLookHelper().setLookPositionWithEntity(entitylivingbase, 180.0F, 180.0F);
            double d0 = shulker.getDistance(entitylivingbase);

            if (d0 < 64.0D)
            {
                if (this.attackTime <= 0)
                {
                    this.attackTime = 20 + shulker.world.rand.nextInt(10) * 20 / 2;
                    EntityShulkerBullet entityshulkerbullet = new EntityShulkerBullet(shulker.world, shulker, entitylivingbase, shulker.getAttachmentFacing().getAxis());
                    shulker.world.spawnEntity(entityshulkerbullet);
                    shulker.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (shulker.world.rand.nextFloat() - shulker.world.rand.nextFloat()) * 0.2F + 1.0F);
                }
            }
            else
            {
                shulker.setAttackTarget((EntityLivingBase)null);
            }

            super.updateTask();
        }
    }
}
