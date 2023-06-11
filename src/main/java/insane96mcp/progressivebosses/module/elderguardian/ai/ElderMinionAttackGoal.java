package insane96mcp.progressivebosses.module.elderguardian.ai;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.Guardian;

import java.util.EnumSet;

public class ElderMinionAttackGoal extends Goal {
	private final Guardian guardian;
	private int attackTime;
	private final boolean elder;

	public ElderMinionAttackGoal(Guardian p_i45833_1_) {
		this.guardian = p_i45833_1_;
		this.elder = p_i45833_1_ instanceof ElderGuardian;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		LivingEntity livingentity = this.guardian.getTarget();
		return livingentity != null && livingentity.isAlive();
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return super.canContinueToUse() && (this.elder || this.guardian.distanceToSqr(this.guardian.getTarget()) > 9.0D);
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	public void start() {
		this.attackTime = -10;
		this.guardian.getNavigation().stop();
		this.guardian.getLookControl().setLookAt(this.guardian.getTarget(), 90.0F, 90.0F);
		this.guardian.hasImpulse = true;
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.guardian.setActiveAttackTarget(0);
		this.guardian.setTarget(null);
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		LivingEntity livingentity = this.guardian.getTarget();
		this.guardian.getNavigation().stop();
		this.guardian.getLookControl().setLookAt(livingentity, 90.0F, 90.0F);
		++this.attackTime;
		if (this.attackTime == 0) {
			this.guardian.setActiveAttackTarget(this.guardian.getTarget().getId());
			if (!this.guardian.isSilent()) {
				this.guardian.level().broadcastEntityEvent(this.guardian, (byte)21);
			}
		} else if (this.attackTime >= this.guardian.getAttackDuration()) {
			float f = 1.0F;
			if (this.guardian.level().getDifficulty() == Difficulty.HARD) {
				f += 2.0F;
			}

			if (this.elder) {
				f += 2.0F;
			}

			livingentity.hurt(livingentity.damageSources().indirectMagic(this.guardian, this.guardian), f);
			livingentity.hurt(livingentity.damageSources().mobAttack(this.guardian), (float)this.guardian.getAttributeValue(Attributes.ATTACK_DAMAGE));
			this.guardian.setTarget(null);
		}

		super.tick();
	}
}