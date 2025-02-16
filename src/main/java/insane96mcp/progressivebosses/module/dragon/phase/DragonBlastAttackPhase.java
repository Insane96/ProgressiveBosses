package insane96mcp.progressivebosses.module.dragon.phase;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import insane96mcp.progressivebosses.module.dragon.data.DragonAttack;
import insane96mcp.progressivebosses.module.dragon.data.DragonStats;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonSittingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonBlastAttackPhase extends AbstractDragonSittingPhase {
    private static EnderDragonPhase<DragonBlastAttackPhase> PHASE;
    private int prepareBlowUpTime;

    private static final int BLAST_TIME = 80;
    private int blastTime;

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
            for (int i = 0; i < 20 + (blastTime - this.prepareBlowUpTime) * 0.1; i++) {
                double r = 24;
                double v = r / 2f;
                double x1 = x + random.nextFloat() * r - v;
                double y1 = y + random.nextFloat() * r - v;
                double z1 = z + random.nextFloat() * r - v;
                Vec3 dir = new Vec3(x1 - x, y1 - y, z1 - z).normalize().scale(-0.008f * ((blastTime - this.prepareBlowUpTime)));
                this.dragon.level().addParticle(ParticleTypes.DRAGON_BREATH, true, x1, y1, z1, dir.x, dir.y, dir.z);
            }
        }
        if (this.prepareBlowUpTime > 9)
            this.dragon.flapTime = 0.8f - (blastTime - this.prepareBlowUpTime + 3) * 0.005f;
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
            List<Entity> entities = this.dragon.level().getEntities((Entity) null, this.dragon.getBoundingBox().inflate(56d), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            float damage = 20f;
            DragonStats stats = DragonFeature.getDragonStats(this.dragon).orElse(null);
            if (stats != null)
                damage = stats.attack.blastDamage;
            for (Entity entity : entities) {
                if (entity == this.dragon || this.dragon.distanceToSqr(entity) > 3136)
                    continue;
                double distanceX = entity.getX() - this.dragon.getX();
                double distanceY = entity.getY() - this.dragon.getY();
                double distanceZ = entity.getZ() - this.dragon.getZ();
                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
                double multiplier = entity instanceof Player ? 16d : 8d;
                double knockbackReduction = entity instanceof LivingEntity living ? 1.0D - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 1d;
                entity.push((distanceX / distance) * multiplier * knockbackReduction, Math.max(1f, distanceY / distance * multiplier * 0.5d * knockbackReduction), (distanceZ / distance) * multiplier * knockbackReduction);
                if (entity instanceof LivingEntity living)
                    living.hurtMarked = true;
                entity.hurt(this.dragon.damageSources().explosion(this.dragon, this.dragon), damage);
            }
            for (int i = 0; i < 8; i++) {
                this.dragon.level().playSound(null, this.dragon.getX() + this.dragon.getRandom().nextFloat() * 48f - 24f, this.dragon.getY() + this.dragon.getRandom().nextFloat() * 48f - 24f, this.dragon.getZ() + this.dragon.getRandom().nextFloat() * 48f - 24f, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 4f, 0.7f);
            }
        }
        else if (this.prepareBlowUpTime <= -10) {
            DragonAnger.setAngered(this.dragon, true);
            this.dragon.getPersistentData().putLong(DragonAttack.LAST_BLAST_TAG, this.dragon.level().getGameTime());
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.TAKEOFF);
        }
        else {
            if (this.prepareBlowUpTime % 5 == 0 && this.prepareBlowUpTime > 20) {
                this.dragon.level().playSound(null, this.dragon, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 4f, 0.8f + (blastTime - this.prepareBlowUpTime) * 0.025f);
            }
            this.dragon.flapTime = 1f - (blastTime - this.prepareBlowUpTime) / (float) blastTime;
        }
    }

    /**
     * Called when this phase is set to active
     */
    public void begin() {
        this.prepareBlowUpTime = BLAST_TIME;
        if (DragonAnger.isAngered(this.dragon))
            this.prepareBlowUpTime -= 15;
        this.blastTime = this.prepareBlowUpTime;
    }

    public static boolean isInCooldown(EnderDragon dragon, Level level) {
        return level.getGameTime() - dragon.getPersistentData().getLong(DragonAttack.LAST_BLAST_TAG) < 600;
    }

    public @NotNull EnderDragonPhase<DragonBlastAttackPhase> getPhase() {
        return PHASE;
    }

    public static EnderDragonPhase<DragonBlastAttackPhase> getPhaseType() {
        return PHASE;
    }

    public static void init() {
        PHASE = EnderDragonPhase.create(DragonBlastAttackPhase.class, "BlastAttack");
    }
}