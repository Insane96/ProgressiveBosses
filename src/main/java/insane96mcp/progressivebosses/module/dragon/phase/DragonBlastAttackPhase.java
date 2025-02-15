package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.module.dragon.data.DragonCrystal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonBlastAttackPhase extends AbstractDragonSittingPhase {
    private static EnderDragonPhase<DragonBlastAttackPhase> BLAST_ATTACK;
    private int prepareBlowUpTime;

    public DragonBlastAttackPhase(EnderDragon pDragon) {
        super(pDragon);
    }

    @Override
    public void doClientTick() {
        RandomSource random = this.dragon.getRandom();
        double x = this.dragon.getX();
        double y = this.dragon.getY() + 2;
        double z = this.dragon.getZ();
        if (--this.prepareBlowUpTime > 30) {
            for (int i = 0; i < 20 + (100 - this.prepareBlowUpTime) * 0.1; i++) {
                double r = 24;
                double v = r / 2f;
                double x1 = x + random.nextFloat() * r - v;
                double y1 = y + random.nextFloat() * r - v;
                double z1 = z + random.nextFloat() * r - v;
                Vec3 dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(-0.008f * ((100 - this.prepareBlowUpTime)));
                this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, x1, y1, z1, dir.x, dir.y, dir.z);
            }
        }
        if (this.prepareBlowUpTime > 9)
            this.dragon.flapTime = 0.8f - (100 - this.prepareBlowUpTime + 3) * 0.005f;
        else if (this.prepareBlowUpTime >= 4) {
            this.dragon.flapTime = 0.333f - (8 - this.prepareBlowUpTime + 1) * 0.08f;
        }
        else {
            this.dragon.flapTime = 0.8f - (4 - this.prepareBlowUpTime) * 0.05f;
        }
        if (this.prepareBlowUpTime == 4) {
            for (int i = 0; i < 4000; i++) {
                double r = 8;
                double v = r / 2f;
                double x1 = x + this.dragon.level().random.nextFloat() * r - v;
                double y1 = y + this.dragon.level().random.nextFloat() * r - v;
                double z1 = z + this.dragon.level().random.nextFloat() * r - v;
                Vec3 dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(5f);
                this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, x1, y1, z1, dir.x, dir.y, dir.z);
            }
        }
    }

    public void doServerTick() {
        if (--this.prepareBlowUpTime == 0) {
            List<Entity> entities = this.dragon.level().getEntities((Entity) null, this.dragon.getBoundingBox().inflate(48d), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            for (Entity entity : entities) {
                if (entity == this.dragon || this.dragon.distanceToSqr(entity) > 2304)
                    continue;
                double distanceX = entity.getX() - this.dragon.getX();
                double distanceY = entity.getY() - this.dragon.getY();
                double distanceZ = entity.getZ() - this.dragon.getZ();
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
                double multiplier = entity instanceof Player ? 16d : 8d;
                entity.push((distanceX / distance) * multiplier, Math.max(1f, distanceY / distance * multiplier * 0.5d), (distanceZ / distance) * multiplier);
                if (entity instanceof LivingEntity living)
                    living.hurtMarked = true;
                entity.hurt(this.dragon.damageSources().explosion(this.dragon, this.dragon), 12f);
            }
            for (int i = 0; i < 8; i++) {
                this.dragon.level().playSound(null, this.dragon.getX() + this.dragon.getRandom().nextFloat() * 48f - 24f, this.dragon.getY() + this.dragon.getRandom().nextFloat() * 48f - 24f, this.dragon.getZ() + this.dragon.getRandom().nextFloat() * 48f - 24f, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4f, 0.7f);
            }
        }
        else if (this.prepareBlowUpTime <= -10) {
            if (!DragonCrystal.tryRespawnCrystals(this.dragon))
                this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
        }
        else {
            if (this.prepareBlowUpTime % 5 == 0 && this.prepareBlowUpTime > 20) {
                this.dragon.level().playSound(null, this.dragon, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4f, 0.8f + (100 - this.prepareBlowUpTime) * 0.01f);
            }
            this.dragon.flapTime = 1f - (100 - this.prepareBlowUpTime) / 100f;
        }
    }

    /**
     * Called when this phase is set to active
     */
    public void begin() {
        this.prepareBlowUpTime = 100;
    }

    public @NotNull EnderDragonPhase<DragonBlastAttackPhase> getPhase() {
        return BLAST_ATTACK;
    }

    public static EnderDragonPhase<DragonBlastAttackPhase> getPhaseType() {
        return BLAST_ATTACK;
    }

    public static void init() {
        BLAST_ATTACK = EnderDragonPhase.create(DragonBlastAttackPhase.class, "BlastAttack");
    }
}