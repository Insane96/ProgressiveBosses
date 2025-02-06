package insane96mcp.progressivebosses.module.dragon.phase;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonSecondPhase extends AbstractDragonSittingPhase {
    private static EnderDragonPhase<DragonSecondPhase> SECOND_PHASE;
    private int prepareBlowUpTime;

    public DragonSecondPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    public void doServerTick() {
        if (--this.prepareBlowUpTime <= 0) {
            List<Entity> entities = this.dragon.level().getEntities((Entity) null, this.dragon.getBoundingBox().inflate(32d), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (Entity entity : entities) {
                if (entity == this.dragon)
                    continue;
                double distanceX = entity.getX() - this.dragon.getX();
                double distanceZ = entity.getZ() - this.dragon.getZ();
                double distance = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
                entity.push((distanceX / distance) * 32d, 1f, (distanceZ / distance) * 32d);
                if (entity instanceof LivingEntity living)
                    living.hurtMarked = true;
                entity.hurt(this.dragon.damageSources().explosion(this.dragon, this.dragon), 10f);
            }
            ((ServerLevel) this.dragon.level()).sendParticles(ParticleTypes.POOF, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 1000, 12, 12, 12, 1.0D);
            this.dragon.playSound(SoundEvents.GENERIC_EXPLODE, 4f, 0.7f);
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
        }
    }

    /**
     * Called when this phase is set to active
     */
    public void begin() {
        this.prepareBlowUpTime = 100;
    }

    @Override
    public float onHurt(DamageSource p_31199_, float p_31200_) {
        return 0f;
    }

    public @NotNull EnderDragonPhase<DragonSecondPhase> getPhase() {
        return SECOND_PHASE;
    }

    public static EnderDragonPhase<DragonSecondPhase> getPhaseType() {
        return SECOND_PHASE;
    }

    public static void init() {
        SECOND_PHASE = EnderDragonPhase.create(DragonSecondPhase.class, "SecondPhase");
    }
}