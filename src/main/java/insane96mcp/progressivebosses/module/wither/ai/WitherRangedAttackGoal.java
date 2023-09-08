package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class WitherRangedAttackGoal extends Goal {
	private final PBWither wither;
	private final int[] headAttackTimes = new int[3];
	private final int[] unseenTargetTicks = new int[3];
	private int seeTime;
	private final float attackRadiusSqr;

	public WitherRangedAttackGoal(PBWither wither, float attackRadius) {
		this.wither = wither;
		this.attackRadiusSqr = attackRadius * attackRadius;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		if (this.wither.getInvulnerableTicks() > 0)
			return false;

		boolean anyHeadHasTarget = false;
		for (int i = 0; i < 3; i++) {
			int targetId = this.wither.getAlternativeTarget(i);
			if (targetId == 0)
				continue;
			anyHeadHasTarget = true;
		}

		return anyHeadHasTarget;
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean canContinueToUse() {
		return this.canUse() || !this.wither.getNavigation().isDone();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void stop() {
		this.seeTime = 0;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		for (int i = 0; i < 3; i++) {
			int targetId = this.wither.getAlternativeTarget(i);
			if (targetId <= 0)
				continue;

			LivingEntity target = (LivingEntity) this.wither.level().getEntity(targetId);
			if (target == null || target.isDeadOrDying())
				continue;
			double distanceSqr = this.wither.distanceToSqr(target.getX(), target.getY(), target.getZ());
			boolean canSee = this.wither.getSensing().hasLineOfSight(target);
			//TODO Charge unseen this.wither.tryCharge(wither, this.unseenTargetTicks[i] / 20f);
			int unseenPlayerTicks = this.unseenTargetTicks[i];
			if (canSee) {
				++this.seeTime;
				if (unseenPlayerTicks > 0)
					this.unseenTargetTicks[i] = unseenPlayerTicks - 1;
			}
			else {
				this.seeTime = 0;
				if (unseenPlayerTicks < 300)
					this.unseenTargetTicks[i] = unseenPlayerTicks + 2;
			}

			if (distanceSqr <= (double)this.attackRadiusSqr && this.seeTime > 0) {
				//Stops the wither from chasing the player
				this.wither.setDeltaMovement(0d, wither.getDeltaMovement().y, 0d);
			}
			/*else if (this.seeTime <= 0 && !this.wither.level().getBlockState(this.wither.blockPosition().below()).canOcclude() && !this.wither.level().canSeeSky(this.wither.blockPosition())) {
				this.wither.setDeltaMovement(wither.getDeltaMovement().x, -1.0d, wither.getDeltaMovement().z);
			}*/

			if (i == 0)
				this.wither.getLookControl().setLookAt(target, 30.0F, 30.0F);

			int barrageAttackTick = this.wither.barrageTicks;
			if (barrageAttackTick > 0) {
				if (!canSee)
					return;
				if (i == 0)
					this.wither.barrageTicks--;
				if (this.wither.barrageTicks % 4 == 0) {
					this.wither.performRangedAttack(i, target.getX() + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), target.getY() + (double)target.getEyeHeight() * 0.5D + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), target.getZ() + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), false);
				}
			}
			else if (distanceSqr <= (double)this.attackRadiusSqr && --this.headAttackTimes[i] <= 0) {
				if (!canSee)
					return;
				this.wither.performRangedAttack(i, target);
				int attackSpeedDelta = this.wither.stats.attackStats.attackSpeedFar - this.wither.stats.attackStats.attackSpeedNear;
				double distanceRatio = distanceSqr / this.attackRadiusSqr;
				this.headAttackTimes[i] = (int) (this.wither.stats.attackStats.attackSpeedNear + (attackSpeedDelta * distanceRatio));
				this.headAttackTimes[i] += this.wither.getRandom().nextInt(-5, 6);
			}
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}