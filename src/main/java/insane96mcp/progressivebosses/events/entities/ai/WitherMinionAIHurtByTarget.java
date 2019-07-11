package net.insane96mcp.progressivebosses.events.entities.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.boss.EntityWither;

public class WitherMinionAIHurtByTarget extends EntityAIHurtByTarget{

	private int revengeTimerOld;

	public WitherMinionAIHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn,
			Class<?>... excludedReinforcementTypes) {
		super(creatureIn, entityCallsForHelpIn, excludedReinforcementTypes);
	}

	@Override
	public boolean shouldExecute() {
        int i = this.taskOwner.getRevengeTimer();
        EntityLivingBase entitylivingbase = this.taskOwner.getRevengeTarget();
        return i != this.revengeTimerOld && entitylivingbase != null && this.isSuitableTarget(entitylivingbase, false) && !(entitylivingbase instanceof EntityWither);
	}
}
