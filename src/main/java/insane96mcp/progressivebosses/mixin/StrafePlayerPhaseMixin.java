package insane96mcp.progressivebosses.mixin;

import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.IPhase;
import net.minecraft.entity.boss.dragon.phase.Phase;
import net.minecraft.entity.boss.dragon.phase.PhaseType;
import net.minecraft.entity.boss.dragon.phase.StrafePlayerPhase;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StrafePlayerPhase.class)
public abstract class StrafePlayerPhaseMixin extends Phase {
	@Shadow
	private int fireballCharge;
	@Shadow
	private Path currentPath;
	@Shadow
	private Vector3d targetLocation;
	@Shadow
	private LivingEntity attackTarget;

	public StrafePlayerPhaseMixin(EnderDragonEntity dragonIn) {
		super(dragonIn);
	}

	@Override
	public void doServerTick() {
		if (this.attackTarget == null) {
			LogHelper.warn("Skipping player strafe phase because no player was found");
			this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
		} else {
			if (this.currentPath != null && this.currentPath.isDone()) {
				double d0 = this.attackTarget.getX();
				double d1 = this.attackTarget.getZ();
				double d2 = d0 - this.dragon.getX();
				double d3 = d1 - this.dragon.getZ();
				double d4 = MathHelper.sqrt(d2 * d2 + d3 * d3);
				double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
				this.targetLocation = new Vector3d(d0, this.attackTarget.getY() + d5, d1);
			}

			double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
			if (d12 < 100.0D || d12 > 22500.0D) {
				this.findNewTarget();
			}

			double d13 = 96.0d;
			if (this.attackTarget.distanceToSqr(this.dragon) < 9216d) {
				if (this.dragon.canSee(this.attackTarget)) {
					++this.fireballCharge;
					Vector3d vector3d1 = (new Vector3d(this.attackTarget.getX() - this.dragon.getX(), 0.0D, this.attackTarget.getZ() - this.dragon.getZ())).normalize();
					Vector3d vector3d = (new Vector3d((double) MathHelper.sin(this.dragon.yRot * ((float)Math.PI / 180F)), 0.0D, (double)(-MathHelper.cos(this.dragon.yRot * ((float)Math.PI / 180F))))).normalize();
					float f1 = (float)vector3d.dot(vector3d1);
					float f = (float)(Math.acos(f1) * (double)(180F / (float)Math.PI));
					f = f + 0.5F;
					if (this.fireballCharge >= 5 && f >= 0.0F && f < 10.0F) {
						Modules.dragon.attack.fireFireball(this.dragon, this.attackTarget);
						this.fireballCharge = 0;
						if (this.currentPath != null) {
							while(!this.currentPath.isDone()) {
								this.currentPath.advance();
							}
						}

						//If must not charge or fireball then go back to holding pattern
						if (!Modules.dragon.attack.onPhaseEnd(this.dragon))
							this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
						//Otherwise reset the phase, in case she fireballs again
						else
							//Can't use initPhase() otherwise the target is reset. Also making the dragon fire slower when chaining fireballs
							this.fireballCharge = -5;
					}
				}
				else if (this.fireballCharge > 0) {
					--this.fireballCharge;
				}
			}
			else if (this.fireballCharge > 0) {
				--this.fireballCharge;
			}

		}
	}

	@Shadow
	private void findNewTarget() {}

	@Shadow public abstract void begin();

	@Override
	public PhaseType<? extends IPhase> getPhase() { return PhaseType.STRAFE_PLAYER; }
}
