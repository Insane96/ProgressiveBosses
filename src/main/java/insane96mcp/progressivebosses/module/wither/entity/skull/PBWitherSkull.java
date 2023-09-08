package insane96mcp.progressivebosses.module.wither.entity.skull;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PBWitherSkull extends AbstractHurtingProjectile {
    ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "wither_skull"));

    private static final EntityDataAccessor<Boolean> DATA_DANGEROUS = SynchedEntityData.defineId(PBWitherSkull.class, EntityDataSerializers.BOOLEAN);
    public PBWitherSkull(EntityType<? extends PBWitherSkull> pEntityType, Level pLevel) {
        super(PBEntities.WITHER_SKULL.get(), pLevel);
    }

    public PBWitherSkull(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ) {
        super(PBEntities.WITHER_SKULL.get(), pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
        if (pShooter instanceof PBWither wither) {
            this.xPower *= wither.stats.attackStats.skullSpeedMultiplier;
            this.yPower *= wither.stats.attackStats.skullSpeedMultiplier;
            this.zPower *= wither.stats.attackStats.skullSpeedMultiplier;
        }
    }

    /**
     * Return the motion factor for this projectile. The factor is multiplied by the original motion.
     */
    protected float getInertia() {
        return this.isDangerous() ? 0.73F : super.getInertia();
    }

    /**
     * Returns {@code true} if the entity is on fire. Used by render to add the fire effect on rendering.
     */
    public boolean isOnFire() {
        return false;
    }

    /**
     * Called when the arrow hits an entity
     */
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level().isClientSide) {
            Entity entityHit = pResult.getEntity();
            Entity owner = this.getOwner();
            boolean hasHurtEntity;
            if (owner instanceof LivingEntity livingOwner) {
                float damage = 8f;
                if (owner instanceof PBWither wither)
                    damage = wither.stats.attackStats.skullDamage;
                hasHurtEntity = entityHit.hurt(this.damageSources().source(DAMAGE_TYPE, this, livingOwner), damage);
                if (hasHurtEntity) {
                    if (entityHit.isAlive()) {
                        this.doEnchantDamageEffects(livingOwner, entityHit);
                    }
                    else {
                        livingOwner.heal(5.0F);
                    }
                }
            }
            else {
                hasHurtEntity = entityHit.hurt(this.damageSources().magic(), 5.0F);
            }

            if (hasHurtEntity && entityHit instanceof LivingEntity livingEntityHit) {
                int i = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    i = 10;
                }
                else if (this.level().getDifficulty() == Difficulty.HARD) {
                    i = 40;
                }

                if (i > 0) {
                    livingEntityHit.addEffect(new MobEffectInstance(MobEffects.WITHER, 20 * i, 1), this.getEffectSource());
                }
            }

        }
    }

    /**
     * Called when this EntityFireball hits a block or entity.
     */
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        if (!this.level().isClientSide) {
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.isDangerous() ? 2.0f : 1.0F, false, Level.ExplosionInteraction.MOB);
            this.discard();
        }
    }

    @Override
    public boolean isPickable() {
        return this.isDangerous();
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isDangerous() && !(pSource.getDirectEntity() instanceof PBWitherSkull))
            return super.hurt(pSource, pAmount);
        return false;
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_DANGEROUS, false);
    }

    /**
     * Return whether this skull comes from an invulnerable (aura) wither boss.
     */
    public boolean isDangerous() {
        return this.entityData.get(DATA_DANGEROUS);
    }

    /**
     * Set whether this skull comes from an invulnerable (aura) wither boss.
     */
    public void setDangerous(boolean pInvulnerable) {
        this.entityData.set(DATA_DANGEROUS, pInvulnerable);
    }

    protected boolean shouldBurn() {
        return false;
    }
}
