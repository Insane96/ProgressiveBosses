package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSkeleton.class)
public interface AbstractSkeletonAccessor {
    @Accessor
    MeleeAttackGoal getMeleeGoal();

    @Accessor
    RangedBowAttackGoal<AbstractSkeleton> getBowGoal();
}
