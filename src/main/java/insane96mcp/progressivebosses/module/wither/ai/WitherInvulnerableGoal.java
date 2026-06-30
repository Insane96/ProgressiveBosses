package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class WitherInvulnerableGoal extends Goal {
    private final PBWither wither;

    public WitherInvulnerableGoal(PBWither wither) {
        this.wither = wither;
        this.setFlags(EnumSet.allOf(Flag.class));
    }

    public boolean canUse() {
        return this.wither.getDyingAnimationTicks() > 0 || this.wither.getInvulnerableTicks() > 0;
    }

    public void start() {
        super.start();
    }

    public void stop() {
        super.stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tick() {
        this.wither.getNavigation().stop();
        this.wither.setDeltaMovement(Vec3.ZERO);
        /*if (this.wither.getDyingAnimationTicks() > 0)
            this.wither.getLookControl().setLookAt(this.wither.getX() + Math.cos(this.wither.getDyingAnimationTicks() * 50), this.wither.getEyeY(), this.wither.getZ() + Math.sin(this.wither.getDyingAnimationTicks() * 50));*/
    }
}
