package insane96mcp.progressivebosses.module.dragon.ai;

import insane96mcp.progressivebosses.mixin.accessor.ShulkerAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.EnumSet;

public class DragonMinionAttackGoal extends Goal {

    private int attackTime;
    private int toShoot;
    private final Shulker shulker;

    private final int cooldown;

    private final int TO_SHOOT = 3;

    public DragonMinionAttackGoal(Shulker shulker, int cooldown) {
        this.shulker = shulker;
        this.cooldown = cooldown;
        this.toShoot = TO_SHOOT;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (shulker.level().getDifficulty() == Difficulty.PEACEFUL)
            return false;

        LivingEntity livingentity = shulker.getTarget();
        return livingentity != null && livingentity.isAlive();
    }

    public void start() {
        ((ShulkerAccessor) shulker).callSetRawPeekAmount(100);
    }

    public void stop() {
        ((ShulkerAccessor) shulker).callSetRawPeekAmount(0);
    }

    public void tick() {
        --this.attackTime;
        LivingEntity livingentity = shulker.getTarget();
        if (livingentity == null)
            return;
        shulker.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
        double d0 = shulker.distanceToSqr(livingentity.position());
        if (d0 < 9216d) { //96 blocks
            if (this.attackTime <= 0) {
                this.attackTime = 5;
                ShulkerBullet bullet = new ShulkerBullet(shulker.level(), shulker, livingentity, shulker.getAttachFace().getAxis());
                shulker.level().addFreshEntity(bullet);
                shulker.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (shulker.level().random.nextFloat() - shulker.level().random.nextFloat()) * 0.2F + 1.0F);
                if (--this.toShoot == 0) {
                    this.attackTime = this.cooldown;
                    this.toShoot = TO_SHOOT;
                }
            }
        } else {
            shulker.setTarget(null);
        }

        super.tick();
    }
}
