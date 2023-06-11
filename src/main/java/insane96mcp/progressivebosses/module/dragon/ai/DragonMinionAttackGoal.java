package insane96mcp.progressivebosses.module.dragon.ai;

import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.EnumSet;

public class DragonMinionAttackGoal extends Goal {

    private int attackTime;
    private final Shulker shulker;

    private final int baseAttackInterval;

    public DragonMinionAttackGoal(Shulker shulker, int attackInterval) {
        this.shulker = shulker;
        this.baseAttackInterval = attackInterval / 2;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        LivingEntity livingentity = shulker.getTarget();
        if (livingentity != null && livingentity.isAlive()) {
            return shulker.level().getDifficulty() != Difficulty.PEACEFUL;
        } else {
            return false;
        }
    }

    public void start() {
        this.attackTime = this.baseAttackInterval;
        shulker.setRawPeekAmount(100);
    }

    public void stop() {
        shulker.setRawPeekAmount(0);
    }

    public void tick() {
        if (shulker.level().getDifficulty() == Difficulty.PEACEFUL)
            return;

        --this.attackTime;
        LivingEntity livingentity = shulker.getTarget();
        if (livingentity == null)
            return;
        shulker.getLookControl().setLookAt(livingentity, 180.0F, 180.0F);
        double d0 = shulker.distanceToSqr(livingentity.position());
        if (d0 < 9216d) { //96 blocks
            if (this.attackTime <= 0) {
                this.attackTime = this.baseAttackInterval + shulker.getRandom().nextInt(10) * this.baseAttackInterval / 2;
                ShulkerBullet bullet = new ShulkerBullet(shulker.level(), shulker, livingentity, shulker.getAttachFace().getAxis());
                if (DragonMinionHelper.isBlindingMinion(this.shulker)) {
                    CompoundTag nbt = bullet.getPersistentData();
                    nbt.putBoolean(Strings.Tags.BLINDNESS_BULLET, true);
                }
                shulker.level().addFreshEntity(bullet);
                shulker.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (shulker.level().random.nextFloat() - shulker.level().random.nextFloat()) * 0.2F + 1.0F);
            }
        } else {
            shulker.setTarget(null);
        }

        super.tick();
    }
}
