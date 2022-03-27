package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.insanelib.util.RandomHelper;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class WitherRangedAttackGoal extends Goal {
	private final WitherBoss wither;
	private LivingEntity target;
	private int attackTime = -1;
	private int seeTime;
	private final int attackInterval;
	private final float attackRadius;
	private final float attackRadiusSqr;
	//Increases the rate of attack of the middle head the closer the player is to the wither
	private final boolean increaseASOnNear;

	public WitherRangedAttackGoal(WitherBoss wither, int attackInterval, float attackRadius, boolean increaseASOnNear) {
		this.wither = wither;
		this.attackInterval = attackInterval;
		this.attackRadius = attackRadius;
		this.attackRadiusSqr = attackRadius * attackRadius;
		this.increaseASOnNear = increaseASOnNear;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		if (this.wither.getInvulnerableTicks() > 0)
			return false;

		int targetId = this.wither.getAlternativeTarget(0);
		Entity entity = this.wither.level.getEntity(targetId);
		if (entity == null)
			return false;
		if (!(entity instanceof LivingEntity livingEntity))
			return false;
		if (!livingEntity.isAlive())
			return false;

		this.target = livingEntity;
		return true;
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
		this.target = null;
		this.seeTime = 0;
		this.attackTime = -1;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		CompoundTag witherTags = wither.getPersistentData();

		double distanceSq = this.wither.distanceToSqr(this.target.getX(), this.target.getY(), this.target.getZ());
		boolean canSee = this.wither.getSensing().hasLineOfSight(this.target);
		int unseenPlayerTicks = witherTags.getInt(Strings.Tags.UNSEEN_PLAYER_TICKS);
		if (canSee) {
			++this.seeTime;
			if (unseenPlayerTicks > 0)
				witherTags.putInt(Strings.Tags.UNSEEN_PLAYER_TICKS, unseenPlayerTicks - 1);
		}
		else {
			this.seeTime = 0;
			if (this.target instanceof Player && unseenPlayerTicks < 300)
				witherTags.putInt(Strings.Tags.UNSEEN_PLAYER_TICKS, unseenPlayerTicks + 2);
		}

		if (distanceSq <= (double)this.attackRadiusSqr && this.seeTime > 0) {
			//Stops the wither from chasing the player
			this.wither.setDeltaMovement(0d, wither.getDeltaMovement().y, 0d);
		}
		else if (this.seeTime <= 0 && !this.wither.level.getBlockState(this.wither.blockPosition().below()).canOcclude() && !this.wither.level.canSeeSky(this.wither.blockPosition())) {
			this.wither.setDeltaMovement(wither.getDeltaMovement().x, -1.0d, wither.getDeltaMovement().z);
		}

		this.wither.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		int barrageAttackTick = witherTags.getInt(Strings.Tags.BARRAGE_ATTACK);
		if (barrageAttackTick > 0) {
			if (!canSee)
				return;
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, barrageAttackTick - 1);
			if (barrageAttackTick % 3 == 0) {
				this.wither.performRangedAttack(RandomHelper.getInt(this.wither.getRandom(), 0, 3), this.target.getX() + RandomHelper.getDouble(this.wither.getRandom(), -2d, 2d), this.target.getY() + (double)this.target.getEyeHeight() * 0.5D + RandomHelper.getDouble(this.wither.getRandom(), -2d, 2d), this.target.getZ() + RandomHelper.getDouble(this.wither.getRandom(), -2d, 2d), false);
			}
		}
		else if (--this.attackTime <= 0) {
			if (!canSee)
				return;
			if (RandomHelper.getFloat(this.wither.getRandom(), 0f, 1f) < .1f)
				for (int h = 0; h < 3; h++) {
					this.wither.performRangedAttack(h, this.target.getX() + RandomHelper.getDouble(this.wither.getRandom(), -1.25d, 1.25d), this.target.getY() + RandomHelper.getDouble(this.wither.getRandom(), -1.25d, 1.25d) + (double)this.target.getEyeHeight() * 0.5D, target.getZ() + RandomHelper.getDouble(this.wither.getRandom(), -1.25d, 1.25d), RandomHelper.getDouble(this.wither.getRandom(), 0d, 1d) < 0.001F);
				}
			else
				this.wither.performRangedAttack(0, this.target.getX(), this.target.getY() + (double)this.target.getEyeHeight() * 0.5D, target.getZ(), RandomHelper.getDouble(this.wither.getRandom(), 0d, 1d) < 0.001F);
			this.attackTime = this.attackInterval;

			if (this.increaseASOnNear) {
				float distance = this.wither.distanceTo(this.target);
				if (distance < this.attackRadius) {
					int nearBonusAS = (int) Math.round((this.attackInterval * 0.75d) * (1d - (distance / this.attackRadius)));
					this.attackTime -= nearBonusAS;
				}
			}
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}