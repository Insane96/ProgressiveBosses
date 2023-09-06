package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class WitherRangedAttackGoal extends Goal {
	private final PBWither wither;
	private LivingEntity target;
	private int attackTime = -1;
	private int seeTime;
	private final float attackRadius;
	private final float attackRadiusSqr;

	public WitherRangedAttackGoal(PBWither wither, float attackRadius) {
		this.wither = wither;
		this.attackRadius = attackRadius;
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

		int targetId = this.wither.getAlternativeTarget(0);
		Entity entity = this.wither.level().getEntity(targetId);
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
		else if (this.seeTime <= 0 && !this.wither.level().getBlockState(this.wither.blockPosition().below()).canOcclude() && !this.wither.level().canSeeSky(this.wither.blockPosition())) {
			this.wither.setDeltaMovement(wither.getDeltaMovement().x, -1.0d, wither.getDeltaMovement().z);
		}

		this.wither.getLookControl().setLookAt(this.target, 30.0F, 30.0F);

		int barrageAttackTick = witherTags.getInt(Strings.Tags.BARRAGE_ATTACK);
		if (barrageAttackTick > 0) {
			if (!canSee)
				return;
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, barrageAttackTick - 1);
			if (barrageAttackTick % 4 == 0) {
				this.wither.performRangedAttack(Mth.nextInt(this.wither.getRandom(), 0, 2), this.target.getX() + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), this.target.getY() + (double)this.target.getEyeHeight() * 0.5D + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), this.target.getZ() + Mth.nextDouble(this.wither.getRandom(), -2d, 2d), false);
			}
		}
		else if (--this.attackTime <= 0) {
			if (!canSee)
				return;
			this.wither.performRangedAttack(0, this.target.getX(), this.target.getY() + (double)this.target.getEyeHeight() * 0.5D, target.getZ(), this.wither.getRandom().nextDouble() < 0.001d);
			//TODO attackSpeedNear
			this.attackTime = this.wither.stats.attackStats.attackSpeedFar;
		}
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}
}