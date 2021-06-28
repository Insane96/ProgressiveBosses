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
public class MixinStrafePlayerPhase extends Phase {
	@Shadow
	private int fireballCharge;
	@Shadow
	private Path currentPath;
	@Shadow
	private Vector3d targetLocation;
	@Shadow
	private LivingEntity attackTarget;

	public MixinStrafePlayerPhase(EnderDragonEntity dragonIn) {
		super(dragonIn);
	}

	@Override
	public void serverTick() {
		if (this.attackTarget == null) {
			LogHelper.warn("Skipping player strafe phase because no player was found");
			this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
		} else {
			if (this.currentPath != null && this.currentPath.isFinished()) {
				double d0 = this.attackTarget.getPosX();
				double d1 = this.attackTarget.getPosZ();
				double d2 = d0 - this.dragon.getPosX();
				double d3 = d1 - this.dragon.getPosZ();
				double d4 = MathHelper.sqrt(d2 * d2 + d3 * d3);
				double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
				this.targetLocation = new Vector3d(d0, this.attackTarget.getPosY() + d5, d1);
			}

			double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.squareDistanceTo(this.dragon.getPosX(), this.dragon.getPosY(), this.dragon.getPosZ());
			if (d12 < 100.0D || d12 > 22500.0D) {
				this.findNewTarget();
			}

			double d13 = 64.0D;
			if (this.attackTarget.getDistanceSq(this.dragon) < 4096.0D) {
				if (this.dragon.canEntityBeSeen(this.attackTarget)) {
					++this.fireballCharge;
					Vector3d vector3d1 = (new Vector3d(this.attackTarget.getPosX() - this.dragon.getPosX(), 0.0D, this.attackTarget.getPosZ() - this.dragon.getPosZ())).normalize();
					Vector3d vector3d = (new Vector3d((double) MathHelper.sin(this.dragon.rotationYaw * ((float)Math.PI / 180F)), 0.0D, (double)(-MathHelper.cos(this.dragon.rotationYaw * ((float)Math.PI / 180F))))).normalize();
					float f1 = (float)vector3d.dotProduct(vector3d1);
					float f = (float)(Math.acos((double)f1) * (double)(180F / (float)Math.PI));
					f = f + 0.5F;
					if (this.fireballCharge >= 5 && f >= 0.0F && f < 10.0F) {
						Modules.dragon.attack.fireFireball(this.dragon, this.attackTarget);
						this.fireballCharge = 0;
						if (this.currentPath != null) {
							while(!this.currentPath.isFinished()) {
								this.currentPath.incrementPathIndex();
							}
						}

						if (!Modules.dragon.attack.onPhaseEnd(this.dragon))
							this.dragon.getPhaseManager().setPhase(PhaseType.HOLDING_PATTERN);
					}
				} else if (this.fireballCharge > 0) {
					--this.fireballCharge;
				}
			} else if (this.fireballCharge > 0) {
				--this.fireballCharge;
			}

		}
	}

	@Shadow
	private void findNewTarget() {}

	@Override
	public PhaseType<? extends IPhase> getType() {
		return PhaseType.STRAFE_PLAYER;
	}
}
