package insane96mcp.progressivebosses.module.wither.entity;

import insane96mcp.progressivebosses.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.data.wither.WitherStats;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class PBWither extends WitherBoss {
    private static final EntityDataAccessor<Integer> CHARGING = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private WitherStats witherStats;

    public PBWither(EntityType<? extends WitherBoss> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("stats", this.witherStats.toNbt(new CompoundTag()));
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.witherStats = WitherStats.fromNbt(tag);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Nullable
    public static PBWither create(Level level, int lvl) {
        PBWither wither = PBEntities.WITHER.get().create(level);
        if (wither == null)
            return null;
        if (!WitherStatsReloadListener.STATS_MAP.containsKey(lvl))
            return null;
        wither.witherStats = WitherStatsReloadListener.STATS_MAP.get(lvl);
        return wither;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CHARGING, 0);
    }

    public int getChargingTicks() {
        return this.entityData.get(CHARGING);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 300.0d)
                .add(Attributes.FOLLOW_RANGE, 64.0d)
                .add(Attributes.MOVEMENT_SPEED, 0.6d)
                .add(Attributes.FLYING_SPEED, 0.6d)
                .add(Attributes.ARMOR, 4d);
    }
}
