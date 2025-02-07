package insane96mcp.progressivebosses.module.dragon.phase;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
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
        if (--this.prepareBlowUpTime > 6) {
            for (int i = 0; i < 10 + (100 - this.prepareBlowUpTime) * 0.1; i++) {
                double r = 8;
                double v = r / 2f;
                double x1 = x + random.nextFloat() * r - v;
                double y1 = y + random.nextFloat() * r - v;
                double z1 = z + random.nextFloat() * r - v;
                Vec3 dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(-0.003f * ((100 - this.prepareBlowUpTime)));
                this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, x1, y1, z1, dir.x, dir.y, dir.z);
            }
            /*for (double theta = 0.0; theta < (Math.PI * 2d); theta += 0.0314159265d) {
                for (double phi = 0.0d; phi < Math.PI; phi += 0.031415927d) {
                    double r = 5.0D; // radius of the sphere
                    double px = x + r * Math.sin(phi) * Math.cos(theta);
                    double py = y + r * Math.cos(phi);
                    double pz = z + r * Math.sin(phi) * Math.sin(theta);
                    double vx = Math.sin(phi) * Math.cos(theta) * -0.2;
                    double vy = Math.cos(phi) * -0.2;
                    double vz = Math.sin(phi) * Math.sin(theta) * -0.2;

                    this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, px, py, pz, vx, vy, vz);
                }
                //this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, this.dragon.getX() + Math.cos(angle) * 5.0D, this.dragon.getY() + this.dragon.getRandom().nextFloat() * 10 - 5, this.dragon.getZ() + Math.sin(angle) * 5.0D, Math.cos(angle) * 0.02f, 0d, Math.sin(angle) * 0.02f);
            }*/
            this.dragon.flapTime = 0.8f - (100 - this.prepareBlowUpTime) * 0.005f;
        }
        else if (this.prepareBlowUpTime != 1) {
            this.dragon.flapTime = 0.333f - (6 - this.prepareBlowUpTime) * 0.08f;
        }
        else if (this.prepareBlowUpTime == 1) {
            for (double theta = 0.0d; theta < (Math.PI * 2d); theta += 0.031415927d * 2) {
                for (double phi = 0.0d; phi < Math.PI; phi += 0.031415927d * 2) {
                    double r = 5.0D; // radius of the sphere
                    double px = x + r * Math.sin(phi) * Math.cos(theta);
                    double py = y + r * Math.cos(phi);
                    double pz = z + r * Math.sin(phi) * Math.sin(theta);
                    double vx = Math.sin(phi) * Math.cos(theta) * 6f;
                    double vy = Math.cos(phi) * 6f;
                    double vz = Math.sin(phi) * Math.sin(theta) * 6f;

                    this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, px, py, pz, vx, vy, vz);
                }
            }
        }
        if (this.prepareBlowUpTime % 2 == 0) {
            //this.dragon.level().playSound(null, this.dragon, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4f, 0.8f);
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
                double multiplier = entity instanceof Player ? 32d : 8d;
                entity.push((distanceX / distance) * multiplier, Math.max(1f, distanceY / distance * multiplier * 0.5d), (distanceZ / distance) * multiplier);
                if (entity instanceof LivingEntity living)
                    living.hurtMarked = true;
                entity.hurt(this.dragon.damageSources().explosion(this.dragon, this.dragon), 12f);
            }
            //((ServerLevel) this.dragon.level()).sendParticles(ParticleTypes.DRAGON_BREATH, this.dragon.getX(), this.dragon.getY(), this.dragon.getZ(), 4000, 12, 12, 12, 1.0D);
            for (int i = 0; i < 5; i++)
                this.dragon.playSound(SoundEvents.GENERIC_EXPLODE, 4f, 1f);
        }
        else if (this.prepareBlowUpTime <= -10)
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
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

    @Override
    public float onHurt(DamageSource p_31199_, float p_31200_) {
        return 0f;
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