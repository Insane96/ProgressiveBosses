package insane96mcp.progressivebosses.module.dragon.corruptedendcrystal;

import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.setup.PBEntities;
import insane96mcp.progressivebosses.setup.PBItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.end.EndDragonFight;

public class CorruptedEndCrystal extends EndCrystal {
    public CorruptedEndCrystal(EntityType<? extends CorruptedEndCrystal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public CorruptedEndCrystal(Level pLevel, double pX, double pY, double pZ) {
        this(PBEntities.CORRUPTED_END_CRYSTAL.get(), pLevel);
        this.setPos(pX, pY, pZ);
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)
                || pSource.getEntity() instanceof EnderDragon
                || (pSource.is(DamageTypeTags.IS_EXPLOSION) && DragonFeature.explosionImmuneCrystals))
            return false;

        if (!this.isRemoved() && !this.level().isClientSide) {
            this.remove(Entity.RemovalReason.KILLED);
            if (!pSource.is(DamageTypeTags.IS_EXPLOSION)) {
                DamageSource damageSource = pSource.getEntity() != null ? this.damageSources().explosion(this, pSource.getEntity()) : null;
                this.level().explode(this, damageSource, null, this.getX(), this.getY(), this.getZ(), 9.0F, false, Level.ExplosionInteraction.BLOCK);
            }

            this.onDestroyedBy(pSource);
        }

        return true;
    }

    private void onDestroyedBy(DamageSource pSource) {
        if (this.level() instanceof ServerLevel) {
            EndDragonFight enddragonfight = ((ServerLevel)this.level()).getDragonFight();
            if (enddragonfight != null)
                enddragonfight.onCrystalDestroyed(this, pSource);
        }
    }

    public void tick() {
        ++this.time;
    }

    public ItemStack getPickResult() {
        return new ItemStack(PBItems.CORRUPTED_END_CRYSTAL.get());
    }
}
