package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class WitherRangedAttackGoal extends Goal {
	private final PBWither wither;
	public final int[] headAttackTimes = new int[3];
	private int unseenTargetSeconds = 0;
	private final float attackRadiusSqr;

	public WitherRangedAttackGoal(PBWither wither, float attackRadius) {
		this.wither = wither;
		this.attackRadiusSqr = attackRadius * attackRadius;
		for (int i = 0; i < 3; i++) {
			this.headAttackTimes[i] = wither.getRandom().nextInt(30, 60);
		}
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

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

	public boolean canContinueToUse() {
		return this.canUse() || !this.wither.getNavigation().isDone();
	}

	public void stop() {
		/*this.seeTime = 0;*/
	}

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

			if (i == 0 && this.wither.tickCount % 20 == 0) {
				if (canSee) {
					if (this.unseenTargetSeconds > 0)
						this.unseenTargetSeconds--;
				}
				else if (this.wither.canCharge() && this.wither.stats.attack.charge != null && this.wither.stats.attack.charge.targetUnseen != null) {
                    this.unseenTargetSeconds++;
                    int seconds = this.unseenTargetSeconds - this.wither.stats.attack.charge.targetUnseen.secondsUnseen;
                    if (seconds > 0)
                        this.wither.tryCharge(Math.min(seconds * this.wither.stats.attack.charge.targetUnseen.chancePerSecond, this.wither.stats.attack.charge.targetUnseen.maxChance), WitherChargeAttackGoal.ChargeType.TARGET_UNSEEN);
                }
            }

			if (distanceSqr <= (double)this.attackRadiusSqr/* && this.seeTime > 0*/) {
				//Stops the wither from chasing the player
				this.wither.setDeltaMovement(0d, wither.getDeltaMovement().y, 0d);
			}
			/*else if (this.seeTime <= 0 && !this.wither.level().getBlockState(this.wither.blockPosition().below()).canOcclude() && !this.wither.level().canSeeSky(this.wither.blockPosition())) {
				this.wither.setDeltaMovement(wither.getDeltaMovement().x, -1.0d, wither.getDeltaMovement().z);
			}*/

			if (i == 0)
				this.wither.getLookControl().setLookAt(target, 30.0F, 30.0F);

			if (this.wither.barrageTicks > 0) {
				/*if (!canSee)
					return;*/
				//noinspection DataFlowIssue - Shouldn't be able to get in here if barrage doesn't exist
				if (this.wither.barrageTicks % (3 * this.wither.stats.attack.barrage.attackSpeed) == i * this.wither.stats.attack.barrage.attackSpeed) {
					float inaccuracy = this.wither.stats.attack.barrage.inaccuracy.getValue(this.wither);
					this.wither.performRangedAttack(i,
							target.getX() + Mth.nextDouble(this.wither.getRandom(), -inaccuracy, inaccuracy),
							target.getY() + (double)target.getEyeHeight() * 0.5D + Mth.nextDouble(this.wither.getRandom(), -inaccuracy, inaccuracy),
							target.getZ() + Mth.nextDouble(this.wither.getRandom(), -inaccuracy, inaccuracy),
							false);
				}
			}
			else if (distanceSqr <= (double)this.attackRadiusSqr && this.wither.getBarrageChargeUpTicks() <= 0 && --this.headAttackTimes[i] <= 0) {
				if (!canSee)
					return;
				this.wither.performRangedAttack(i, target);
				int attackSpeedDelta = this.wither.stats.attack.attackSpeedFar - this.wither.stats.attack.attackSpeedNear;
				double distanceRatio = distanceSqr / this.attackRadiusSqr;
				this.headAttackTimes[i] = (int) (this.wither.stats.attack.attackSpeedNear + (attackSpeedDelta * distanceRatio)) + this.wither.getRandom().nextInt(-5, 6);
				if (i != 0 && !this.wither.needsHealing())
					this.headAttackTimes[i] = (int) (this.headAttackTimes[i] * this.wither.stats.attack.sideHeadsAttackSpeedMultiplier);
			}
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}