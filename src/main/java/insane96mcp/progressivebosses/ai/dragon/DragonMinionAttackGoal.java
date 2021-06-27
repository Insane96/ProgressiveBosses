package insane96mcp.progressivebosses.ai.dragon;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.utils.DragonMinionHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.Difficulty;

import java.util.EnumSet;

public class DragonMinionAttackGoal extends Goal {

    private int attackTime;
    private final ShulkerEntity shulker;

    private final int baseAttackInterval;

    public DragonMinionAttackGoal(ShulkerEntity shulker, int attackInterval) {
        this.shulker = shulker;
        this.baseAttackInterval = attackInterval;
        this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean shouldExecute() {
        LivingEntity livingentity = shulker.getAttackTarget();
        if (livingentity != null && livingentity.isAlive()) {
            return shulker.world.getDifficulty() != Difficulty.PEACEFUL;
        } else {
            return false;
        }
    }

    public void startExecuting() {
        this.attackTime = this.baseAttackInterval;
        shulker.updateArmorModifier(100);
    }

    public void resetTask() {
        shulker.updateArmorModifier(0);
    }

    public void tick() {
        if (shulker.world.getDifficulty() == Difficulty.PEACEFUL)
            return;

        --this.attackTime;
        LivingEntity livingentity = shulker.getAttackTarget();
        if (livingentity == null)
            return;
        shulker.getLookController().setLookPositionWithEntity(livingentity, 180.0F, 180.0F);
        double d0 = shulker.getDistanceSq(livingentity.getPositionVec());
        if (d0 < 9216d) { //96 blocks
            if (this.attackTime <= 0) {
                this.attackTime = this.baseAttackInterval + shulker.getRNG().nextInt(10) * this.baseAttackInterval / 2;
                ShulkerBulletEntity bullet = new ShulkerBulletEntity(shulker.world, shulker, livingentity, shulker.getAttachmentFacing().getAxis());
                if (DragonMinionHelper.isBlindingMinion(this.shulker)) {
                    CompoundNBT nbt = bullet.getPersistentData();
                    nbt.putBoolean(Strings.Tags.BLINDNESS_BULLET, true);
                }
                shulker.world.addEntity(bullet);
                shulker.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (shulker.world.rand.nextFloat() - shulker.world.rand.nextFloat()) * 0.2F + 1.0F);
            }
        } else {
            shulker.setAttackTarget(null);
        }

        super.tick();
    }
}
