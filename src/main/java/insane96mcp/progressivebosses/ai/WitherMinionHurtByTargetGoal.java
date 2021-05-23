package insane96mcp.progressivebosses.ai;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.WitherSkeletonEntity;

import java.util.function.Predicate;

public class WitherMinionHurtByTargetGoal extends HurtByTargetGoal {

    private static final Predicate<LivingEntity> NON_WITHER = livingEntity -> !(livingEntity instanceof WitherEntity) && !(livingEntity instanceof WitherSkeletonEntity);
    private static final EntityPredicate TARGETING_CONDITIONS = (new EntityPredicate()).setLineOfSiteRequired().setUseInvisibilityCheck().setCustomPredicate(NON_WITHER);

    public WitherMinionHurtByTargetGoal(CreatureEntity p_i50317_1_, Class<?>... p_i50317_2_) {
        super(p_i50317_1_, p_i50317_2_);
    }

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
            LivingEntity livingEntity = this.goalOwner.getRevengeTarget();
            return livingEntity != null && this.isSuitableTarget(livingEntity, TARGETING_CONDITIONS) && !(livingEntity instanceof WitherEntity);
        }
        return false;
    }
}
