package insane96mcp.progressivebosses.mixin;

import insane96mcp.insanelib.utils.LogHelper;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class StrafePlayerPhaseMixin extends AbstractDragonPhaseInstance {
	@Shadow
	private int fireballCharge;
	@Shadow
	private Path currentPath;
	@Shadow
	private Vec3 targetLocation;
	@Shadow
	private LivingEntity attackTarget;

	public StrafePlayerPhaseMixin(EnderDragon dragonIn) {
		super(dragonIn);
	}

	@Override
	public void doServerTick() {
		if (this.attackTarget == null) {
			LogHelper.warn("Skipping player strafe phase because no player was found");
			this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
		} else {
			if (this.currentPath != null && this.currentPath.isDone()) {
				double d0 = this.attackTarget.getX();
				double d1 = this.attackTarget.getZ();
				double d2 = d0 - this.dragon.getX();
				double d3 = d1 - this.dragon.getZ();
				double d4 = Mth.sqrt((float) (d2 * d2 + d3 * d3));
				double d5 = Math.min((double)0.4F + d4 / 80.0D - 1.0D, 10.0D);
				this.targetLocation = new Vec3(d0, this.attackTarget.getY() + d5, d1);
			}

			double d12 = this.targetLocation == null ? 0.0D : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
			if (d12 < 100.0D || d12 > 22500.0D) {
				this.findNewTarget();
			}

			double d13 = 96.0d;
			if (this.attackTarget.distanceToSqr(this.dragon) < 9216d) {
				if (this.dragon.hasLineOfSight(this.attackTarget)) {
					++this.fireballCharge;
					Vec3 vector3d1 = (new Vec3(this.attackTarget.getX() - this.dragon.getX(), 0.0D, this.attackTarget.getZ() - this.dragon.getZ())).normalize();
					Vec3 vector3d = (new Vec3((double) Mth.sin(this.dragon.getRotationVector().y * ((float)Math.PI / 180F)), 0.0D, (double)(-Mth.cos(this.dragon.getRotationVector().y * ((float)Math.PI / 180F))))).normalize();
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
							this.dragon.getPhaseManager().setPhase(EnderDragonPhase.HOLDING_PATTERN);
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
	public EnderDragonPhase<? extends DragonPhaseInstance> getPhase() { return EnderDragonPhase.STRAFE_PLAYER; }
}
