package insane96mcp.progressivebosses.ai.wither;

import insane96mcp.insanelib.utils.RandomHelper;
import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.EnumSet;

public class WitherRangedAttackGoal extends Goal {
	private final WitherEntity wither;
	private LivingEntity target;
	private int attackTime = -1;
	private int seeTime;
	private final int attackInterval;
	private final float attackRadius;
	private final float attackRadiusSqr;
	//Multiplied by the rate of attack when health drops below half
	private final double attackSpeedMultiplier;
	//Increases the rate of attack of the middle head the closer the player is to the wither
	private final boolean increaseASOnNear;

	public WitherRangedAttackGoal(WitherEntity wither, int attackInterval, float attackRadius, double attackSpeedMultiplier, boolean increaseASOnNear) {
		this.wither = wither;
		this.attackInterval = attackInterval;
		this.attackRadius = attackRadius;
		this.attackRadiusSqr = attackRadius * attackRadius;
		this.attackSpeedMultiplier = attackSpeedMultiplier;
		this.increaseASOnNear = increaseASOnNear;
		this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean shouldExecute() {
		if (this.wither.getInvulTime() > 0) {
			return false;
		}
		int targetId = this.wither.getWatchedTargetId(0);
		Entity entity = this.wither.world.getEntityByID(targetId);
		if (entity == null)
			return false;
		if (!(entity instanceof LivingEntity))
			return false;

		LivingEntity livingEntity = (LivingEntity) entity;
		if (livingEntity.isAlive()) {
			this.target = livingEntity;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	public boolean shouldContinueExecuting() {
		return this.shouldExecute() || !this.wither.getNavigator().noPath();
	}

	/**
	 * Reset the task's internal state. Called when this task is interrupted by another one
	 */
	public void resetTask() {
		this.target = null;
		this.seeTime = 0;
		this.attackTime = -1;
	}

	/**
	 * Keep ticking a continuous task that has already been started
	 */
	public void tick() {
		double distanceSq = this.wither.getDistanceSq(this.target.getPosX(), this.target.getPosY(), this.target.getPosZ());
		boolean canSee = this.wither.getEntitySenses().canSee(this.target);
		if (canSee) {
			++this.seeTime;
		}
		else {
			this.seeTime = 0;
		}

		if (distanceSq <= (double)this.attackRadiusSqr && this.seeTime >= 5) {
			//Stops the wither from chasing the player
			this.wither.setMotion(0d, wither.getMotion().y, 0d);
		}

		this.wither.getLookController().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

		CompoundNBT witherTags = wither.getPersistentData();
		int barrageAttackTick = witherTags.getInt(Strings.Tags.BARRAGE_ATTACK);
		if (barrageAttackTick > 0) {
			if (!canSee)
				return;
			witherTags.putInt(Strings.Tags.BARRAGE_ATTACK, barrageAttackTick - 1);
			if (barrageAttackTick % 2 == 0) {
				this.wither.launchWitherSkullToCoords(0, this.target.getPosX() + RandomHelper.getDouble(this.wither.getRNG(), -2d, 2d), this.target.getPosY() + (double)this.target.getEyeHeight() * 0.5D + RandomHelper.getDouble(this.wither.getRNG(), -2d, 2d), this.target.getPosZ() + RandomHelper.getDouble(this.wither.getRNG(), -2d, 2d), false);
			}
		}
		else if (--this.attackTime <= 0) {
			if (!canSee)
				return;
			if (RandomHelper.getFloat(this.wither.getRNG(), 0f, 1f) < .1f)
				for (int h = 0; h < 3; h++) {
					this.wither.launchWitherSkullToCoords(h, this.target.getPosX() + RandomHelper.getDouble(this.wither.getRNG(), -0.75d, 0.75d), this.target.getPosY() + RandomHelper.getDouble(this.wither.getRNG(), -0.75d, 0.75d) + (double)this.target.getEyeHeight() * 0.5D, target.getPosZ() + RandomHelper.getDouble(this.wither.getRNG(), -0.75d, 0.75d), RandomHelper.getDouble(this.wither.getRNG(), 0d, 1d) < 0.001F);
				}
			else
				this.wither.launchWitherSkullToCoords(0, this.target.getPosX(), this.target.getPosY() + (double)this.target.getEyeHeight() * 0.5D, target.getPosZ(), RandomHelper.getDouble(this.wither.getRNG(), 0d, 1d) < 0.001F);
			this.attackTime = this.attackInterval;

			if (this.wither.isCharged() && this.attackSpeedMultiplier > 0d)
				this.attackTime *= this.attackSpeedMultiplier;

			if (this.increaseASOnNear) {
				float distance = this.wither.getDistance(this.target);
				if (distance < this.attackRadius) {
					int nearBonusAS = (int) Math.round((this.attackInterval * 0.3d) * (1d - (distance / this.attackRadius)));
					this.attackTime -= nearBonusAS;
				}
			}
		}
	}

	/*public void launchWitherSkullToCoords(int head, double x, double y, double z, boolean invulnerable) {
		if (!this.wither.isSilent()) {
			this.wither.world.playEvent((PlayerEntity)null, 1024, this.wither.getPosition(), 0);
		}

		double d0 = this.wither.getHeadX(head);
		double d1 = this.wither.getHeadY(head);
		double d2 = this.wither.getHeadZ(head);
		double d3 = x - d0;
		double d4 = y - d1;
		double d5 = z - d2;
		CompoundNBT compoundNBT = this.wither.getPersistentData();
		float difficulty = compoundNBT.getFloat(Strings.Tags.DIFFICULTY);
		WitherSkullEntity witherskullentity = new WitherSkullEntity(this.wither.world, this.wither, d3, d4, d5);
		witherskullentity.setShooter(this.wither);
		witherskullentity.accelerationX *= 1 + (Modules.witherModule.attackFeature.skullBonusVelocity * difficulty);
		witherskullentity.accelerationY *= 1 + (Modules.witherModule.attackFeature.skullBonusVelocity * difficulty);
		witherskullentity.accelerationZ *= 1 + (Modules.witherModule.attackFeature.skullBonusVelocity * difficulty);
		if (invulnerable) {
			witherskullentity.setSkullInvulnerable(true);
		}

		witherskullentity.setRawPosition(d0, d1, d2);
		this.wither.world.addEntity(witherskullentity);
	}*/
}