package insane96mcp.progressivebosses.events.entities.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.monster.ShulkerEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.Difficulty;

import java.util.EnumSet;

public class DragonMinionAttackGoal extends Goal {

    private int attackTime;
    private final ShulkerEntity shulker;

    private int baseAttackTime = 40;

    public DragonMinionAttackGoal(ShulkerEntity shulker) {
        this.shulker = shulker;
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
        this.attackTime = this.baseAttackTime;
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
        double d0 = shulker.getDistance(livingentity);
        if (d0 < 96.0) {
            if (this.attackTime <= 0) {
                this.attackTime = this.baseAttackTime + shulker.world.rand.nextInt(10) * this.baseAttackTime / 2;
                shulker.world.addEntity(new ShulkerBulletEntity(shulker.world, shulker, livingentity, shulker.getAttachmentFacing().getAxis()));
                shulker.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (shulker.world.rand.nextFloat() - shulker.world.rand.nextFloat()) * 0.2F + 1.0F);
            }
        } else {
            shulker.setAttackTarget(null);
        }

        super.tick();
    }
}
