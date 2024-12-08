package insane96mcp.progressivebosses.module.dragon.ai;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.projectile.ShulkerBullet;

import java.util.EnumSet;
import java.util.Optional;

public class DragonMinionAttackGoal extends Goal {

    private int attackTime;
    private int toShoot;
    private final Shulker shulker;

    private final int cooldown;

    public DragonMinionAttackGoal(Shulker shulker, int cooldown) {
        this.shulker = shulker;
        this.cooldown = cooldown;
        this.toShoot = 4;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public boolean canUse() {
        if (shulker.level().getDifficulty() == Difficulty.PEACEFUL)
            return false;

        LivingEntity livingentity = shulker.getTarget();
        return livingentity != null && livingentity.isAlive();
    }

    public void start() {
        shulker.setRawPeekAmount(100);
    }

    public void stop() {
        shulker.setRawPeekAmount(0);
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

                EnderDragon dragon = DragonFeature.findDragon((ServerLevel) shulker.level());
                if (dragon != null) {
                    Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
                    if (stats.isPresent() && stats.get().minion != null && this.shulker.getRandom().nextFloat() < stats.get().minion.blindingChance) {
                        ListTag effectListTag = new ListTag();
                        effectListTag.add(new MobEffectInstance(MobEffects.BLINDNESS, 150).save(new CompoundTag()));
                        bullet.getPersistentData().put("CustomPotionEffects", effectListTag);
                    }
                }
                shulker.level().addFreshEntity(bullet);
                shulker.playSound(SoundEvents.SHULKER_SHOOT, 2.0F, (shulker.level().random.nextFloat() - shulker.level().random.nextFloat()) * 0.2F + 1.0F);
                if (--this.toShoot == 0) {
                    this.attackTime = this.cooldown;
                    this.toShoot = 4;
                }
            }
        } else {
            shulker.setTarget(null);
        }

        super.tick();
    }
}
