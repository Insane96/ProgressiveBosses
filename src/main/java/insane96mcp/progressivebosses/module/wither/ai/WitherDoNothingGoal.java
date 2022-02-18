package insane96mcp.progressivebosses.module.wither.ai;

import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.wither.WitherBoss;

import java.util.EnumSet;

public class WitherDoNothingGoal extends Goal {
	WitherBoss wither;

	public WitherDoNothingGoal(WitherBoss wither) {
		this.wither = wither;
		this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP, Goal.Flag.LOOK));
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
	 * method as well.
	 */
	public boolean canUse() {
		CompoundTag tags = this.wither.getPersistentData();
		return this.wither.getInvulnerableTicks() > 0 && !tags.getBoolean(Strings.Tags.CHARGE_ATTACK);
	}
}