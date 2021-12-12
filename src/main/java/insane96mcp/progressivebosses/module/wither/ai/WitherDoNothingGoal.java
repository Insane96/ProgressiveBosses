package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.nbt.CompoundNBT;

import java.util.EnumSet;

public class WitherDoNothingGoal extends Goal {
	WitherEntity wither;

	public WitherDoNothingGoal(WitherEntity wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		CompoundNBT tags = this.wither.getPersistentData();
		return this.wither.getInvulnerableTicks() > 0 && !tags.contains(Strings.Tags.CHARGE_ATTACK);
	}
}