package insane96mcp.progressivebosses.module.wither.entity;

import insane96mcp.insanelib.ai.ILNearestAttackableTargetGoal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class WitherNearestAttackableTargetGoal<T extends LivingEntity> extends ILNearestAttackableTargetGoal<T> {
    public WitherNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean ignoreLineOfSight) {
        super(goalOwnerIn, targetClassIn, false, false, PBWither.NO_UNDEAD_SELECTOR);
        if (ignoreLineOfSight)
            this.targetEntitySelector = TargetingConditions.forCombat().ignoreLineOfSight().range(48d).selector(PBWither.NO_UNDEAD_SELECTOR);
    }
}
