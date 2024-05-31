package insane96mcp.progressivebosses.module.wither.entity;

import com.google.common.collect.ImmutableList;
import insane96mcp.progressivebosses.module.ILvl;
import insane96mcp.progressivebosses.module.wither.ai.WitherChargeAttackGoal;
import insane96mcp.progressivebosses.module.wither.ai.WitherRangedAttackGoal;
import insane96mcp.progressivebosses.module.wither.data.WitherStats;
import insane96mcp.progressivebosses.module.wither.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.module.wither.entity.skull.PBWitherSkull;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class PBWither extends Monster implements PowerableMob, RangedAttackMob, ILvl {
    public static final int CHARGE_ATTACK_TICK_CHARGE = 30;
    public static final int BARRAGE_CHARGE_UP_TICKS = 30;
    private static final EntityDataAccessor<Integer> DATA_TARGET_A = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_B = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TARGET_C = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final List<EntityDataAccessor<Integer>> DATA_TARGETS = ImmutableList.of(DATA_TARGET_A, DATA_TARGET_B, DATA_TARGET_C);
    private static final EntityDataAccessor<Integer> DATA_ID_INV = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LVL = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CHARGING = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> BARRAGE_CHARGE_UP = SynchedEntityData.defineId(PBWither.class, EntityDataSerializers.INT);
    private static final int INVULNERABLE_TICKS = 220;
    private final float[] xRotHeads = new float[2];
    private final float[] yRotHeads = new float[2];
    private final float[] xRotOHeads = new float[2];
    private final float[] yRotOHeads = new float[2];
    private final int[] nextHeadUpdate = new int[2];
    //private final int[] idleHeadUpdates = new int[2];
    public int destroyBlocksTick;
    public final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    public static final Predicate<LivingEntity> NO_UNDEAD_SELECTOR = (livingEntity) -> livingEntity.getMobType() != MobType.UNDEAD && livingEntity.attackable();
    private static final TargetingConditions TARGETING_CONDITIONS = TargetingConditions.forCombat().range(48d).selector(NO_UNDEAD_SELECTOR);
    private static final TargetingConditions TARGETING_CONDITIONS_NEEDS_HEALING = TargetingConditions.forCombat().range(48d).selector(LivingEntity::attackable);
    public int barrageTicks;
    public WitherStats stats;
    public int minionCooldown;
    public boolean chargeBelow;
    public int shotSkulls;
    private int forceChargeTicks = 20;
    private int secondPhaseCharge;

    public PBWither(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setHealth(this.getMaxHealth());
    }

    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    protected void registerGoals() {
        //this.goalSelector.addGoal(0, new WitherDoNothingGoal());
        this.goalSelector.addGoal(1, new WitherChargeAttackGoal(this));
        this.goalSelector.addGoal(2, new WitherRangedAttackGoal(this, 32f));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new WitherNearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new WitherNearestAttackableTargetGoal<>(this, LivingEntity.class, false));
    }

    @Override
    public Component getName() {
        Component component = this.getCustomName();
        if (component != null)
            return super.getName();
        return Component.translatable(Util.makeDescriptionId("entity", ForgeRegistries.ENTITY_TYPES.getKey(this.getType())) + "." + this.getLvl());
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        this.stats.finalizeSpawn(this);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Invul", this.getInvulnerableTicks());
        tag.putInt("lvl", this.getLvl());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setInvulnerableTicks(tag.getInt("Invul"));
        this.setLvl(tag.getInt("lvl"));
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_A, 0);
        this.entityData.define(DATA_TARGET_B, 0);
        this.entityData.define(DATA_TARGET_C, 0);
        this.entityData.define(DATA_ID_INV, 0);
        this.entityData.define(LVL, 0);
        this.entityData.define(CHARGING, 0);
        this.entityData.define(BARRAGE_CHARGE_UP, 0);
    }

    public int getChargingTicks() {
        return this.entityData.get(CHARGING);
    }
    public boolean isCharging() {
        return getChargingTicks() > 0;
    }
    public boolean isChargingInCooldown() {
        return getChargingTicks() < 0;
    }
    public void tickCharging() {
        int ticks = this.entityData.get(CHARGING);
        if (ticks > 0)
            ticks--;
        else if (ticks < 0)
            ticks++;
        this.entityData.set(CHARGING, ticks);
    }
    public boolean initCharging() {
        if (this.isCharging())
            return false;
        int chargeTime = 40;
        if (this.stats.attack.charge != null)
            chargeTime = this.stats.attack.charge.time;
        double missingHealthPercentage = 1d - this.getHealth() / this.getMaxHealth();
        chargeTime -= (int) (20 * missingHealthPercentage);
        this.entityData.set(CHARGING, chargeTime + CHARGE_ATTACK_TICK_CHARGE);
        return true;
    }
    public void stopCharging() {
        int cooldown = 40;
        if (this.needsHealing())
            cooldown = 120;
        this.entityData.set(CHARGING, -cooldown);
    }
    public void tryCharge(float damageAmount) {
        if (this.stats.attack.charge == null
                || this.isCharging()
                || this.getBarrageChargeUpTicks() > 0)
            return;
        double chance = this.stats.attack.charge.maxChance.getValue(this) * (damageAmount / 10f);
        if (!this.isPowered() && !this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(5d)).isEmpty())
            chance = 0.25f;
        if (this.getRandom().nextDouble() < chance)
            this.initCharging();
    }

    private void updateStats(boolean wasPowered) {
        if (this.stats.resistancesWeaknesses == null)
            return;
        if (wasPowered != this.isPowered()) {
            AttributeInstance instance = this.getAttribute(Attributes.ARMOR);
            if (instance != null)
                instance.setBaseValue(this.stats.resistancesWeaknesses.armor.getValue(this));
            instance = this.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (instance != null)
                instance.setBaseValue(this.stats.resistancesWeaknesses.toughness.getValue(this));
        }
    }

    private void tickBarrage() {
        int barrage = this.getBarrageChargeUpTicks();
        if (barrage > 0) {
            barrage--;
            if (barrage % 5 == 0 && barrage > 10)
                this.playSound(SoundEvents.WITHER_AMBIENT, 4f, 0.75f);
            if (barrage == 5)
                initBarrage();
            this.setBarrageChargeUpTicks(barrage);
        }
        if (this.barrageTicks > 0)
            this.barrageTicks--;
    }

    public int getBarrageChargeUpTicks() {
        return this.entityData.get(BARRAGE_CHARGE_UP);
    }

    public void setBarrageChargeUpTicks(int ticks) {
        this.entityData.set(BARRAGE_CHARGE_UP, ticks);
    }

    public void tryBarrage(float damageAmount) {
        if (this.stats.attack.barrage == null
                || this.getBarrageChargeUpTicks() > 0
                || this.barrageTicks > 0
                || this.isCharging()
                /*|| this.level().getNearestPlayer(this, 4d) == null*/)
            return;
        double chance = this.stats.attack.barrage.chance.getValue(this) * (damageAmount / 10f);
        if (this.getRandom().nextDouble() < chance) {
            this.initBarrageChargeUp();
        }
    }

    public boolean initBarrageChargeUp() {
        this.setBarrageChargeUpTicks(30);
        return true;
    }

    public void initBarrage() {
        double missingHealthPercentage = 1d - this.getHealth() / this.getMaxHealth();
        this.barrageTicks = (int) (((this.stats.attack.barrage.maxDuration - this.stats.attack.barrage.minDuration) * missingHealthPercentage) + this.stats.attack.barrage.minDuration);
    }

    private void tickMinion() {
        if (this.stats.minion != null && --this.minionCooldown <= 0) {
            this.stats.minion.trySpawnMinion(this, false);
        }
    }

    /**+
     * Called when spawning the wither from the "corrupted soul sand"
     */
    public void setLvl(int lvl) {
        this.entityData.set(LVL, lvl);
        if (WitherStatsReloadListener.STATS_MAP.containsKey(lvl)) {
            this.stats = WitherStatsReloadListener.STATS_MAP.get(lvl);
            this.stats.apply(this);
        }
        else {
            this.stats = WitherStats.getDefaultStats();
        }
        this.bossEvent.setName(this.getDisplayName());
    }

    public int getLvl() {
        return this.entityData.get(LVL);
    }

    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.WITHER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    public void aiStep() {
        if (this.getInvulnerableTicks() > 0 || this.getBarrageChargeUpTicks() > 5) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        else if (!this.isCharging()) {
            Vec3 vec3 = this.getDeltaMovement().multiply(1.0D, 0D, 1.0D);
            if (!this.level().isClientSide) {
                if (this.getTarget() != null) {
                    if (this.getTarget() != null) {
                        double d0 = 0d;
                        float f = !this.isPowered() ? 5 : 0;
                        if (this.getY() >= this.getTarget().getY() + f + 1)
                            d0 = -0.15d;
                        if ((this.getY() < this.getTarget().getY() || (this.getY() < this.getTarget().getY() + f))) {
                            d0 = 0.3D;
                        }

                        vec3 = new Vec3(vec3.x, d0, vec3.z);
                        Vec3 vec31 = new Vec3(this.getTarget().getX() - this.getX(), 0.0D, this.getTarget().getZ() - this.getZ());
                        if (vec31.horizontalDistanceSqr() > 9.0D) {
                            Vec3 vec32 = vec31.normalize();
                            vec3 = vec3.add(vec32.x * 0.3D - vec3.x * 0.6D, 0.0D, vec32.z * 0.3D - vec3.z * 0.6D);
                        }
                    }
                }
                else if (this.isInWall()) {
                    this.chargeBelow = true;
                    this.initCharging();
                }
            }

            this.setDeltaMovement(vec3);
            /*if (vec3.horizontalDistanceSqr() > 0.05D) {
                this.setYRot((float)Mth.atan2(vec3.z, vec3.x) * (180F / (float)Math.PI) - 90.0F);
            }*/
        }

        super.aiStep();

        for(int head = 0; head < 2; ++head) {
            this.yRotOHeads[head] = this.yRotHeads[head];
            this.xRotOHeads[head] = this.xRotHeads[head];
        }

        for(int head = 0; head < 2; ++head) {
            int targetId = this.getAlternativeTarget(head + 1);
            Entity target = null;
            if (targetId > 0) {
                target = this.level().getEntity(targetId);
            }

            if (target != null) {
                double headX = this.getHeadX(head + 1);
                double headY = this.getHeadY(head + 1);
                double headZ = this.getHeadZ(head + 1);
                double diffX = target.getX() - headX;
                double diffY = target.getEyeY() - headY;
                double diffZ = target.getZ() - headZ;
                double distance = Math.sqrt(diffX * diffX + diffZ * diffZ);
                float xRot = (float)(Mth.atan2(diffZ, diffX) * (double)(180F / (float)Math.PI)) - 90.0F;
                float yRot = (float)(-(Mth.atan2(diffY, distance) * (double)(180F / (float)Math.PI)));
                this.xRotHeads[head] = this.rotLerp(this.xRotHeads[head], yRot, 40.0F);
                this.yRotHeads[head] = this.rotLerp(this.yRotHeads[head], xRot, 10.0F);
            } else {
                this.yRotHeads[head] = this.rotLerp(this.yRotHeads[head], this.yBodyRot, 10.0F);
            }
        }

        boolean isPowered = this.isPowered();

        for(int l = 0; l < 3; ++l) {
            double headX = this.getHeadX(l);
            double headY = this.getHeadY(l);
            double headZ = this.getHeadZ(l);
            this.level().addParticle(ParticleTypes.SMOKE, headX + this.random.nextGaussian() * 0.3f, headY + this.random.nextGaussian() * 0.3f, headZ + this.random.nextGaussian() * 0.3f, 0.0D, 0.0D, 0.0D);
            if (isPowered && this.level().random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, headX + this.random.nextGaussian() * 0.3f, headY + this.random.nextGaussian() * 0.3f, headZ + this.random.nextGaussian() * 0.3f, 0.7F, 0.7F, 0.5D);
            }
        }

        if (this.getInvulnerableTicks() > 0) {
            for(int i = 0; i < 3; ++i) {
                this.level().addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + this.random.nextGaussian(), this.getY() + (double)(this.random.nextFloat() * 3.3F), this.getZ() + this.random.nextGaussian(), 0.7F, 0.7F, 0.9F);
            }
        }

    }

    protected int findNewTarget() {
        if (!this.needsHealing()) {
            List<Player> playersNearby = this.level().getNearbyEntities(Player.class, TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(32d, 24d, 32d));
            if (!playersNearby.isEmpty()) {
                if (playersNearby.size() == 1)
                    return playersNearby.get(0).getId();
                Player player = playersNearby.get(this.random.nextInt(playersNearby.size()));
                return player.getId();
            }
            if (this.getTarget() != null)
                return this.getTarget().getId();
        }
        List<LivingEntity> livingsNearby = this.level().getNearbyEntities(LivingEntity.class, this.needsHealing() ? TARGETING_CONDITIONS_NEEDS_HEALING : TARGETING_CONDITIONS, this, this.getBoundingBox().inflate(32d, 24d, 32d));
        if (!livingsNearby.isEmpty()) {
            LivingEntity livingEntity = livingsNearby.get(this.random.nextInt(livingsNearby.size()));
            return livingEntity.getId();
        }
        return 0;

    }

    protected void customServerAiStep() {
        if (this.getInvulnerableTicks() > 0) {
            int newInvulTicks = this.getInvulnerableTicks() - 1;
            this.bossEvent.setProgress(1.0F - (float)newInvulTicks / INVULNERABLE_TICKS);
            if (newInvulTicks <= 0) {
                this.level().explode(this, this.getX(), this.getEyeY(), this.getZ(), this.stats.misc.explosionPower, this.stats.misc.explosionCausesFire, Level.ExplosionInteraction.MOB);
                if (!this.isSilent()) {
                    this.level().globalLevelEvent(LevelEvent.SOUND_WITHER_BOSS_SPAWN, this.blockPosition(), 0);
                }
                this.setHealth(this.getMaxHealth());
            }

            this.setInvulnerableTicks(newInvulTicks);
        }
        else {
            super.customServerAiStep();

            //Update Heads targets
            for (int i = 0; i < 2; i++) {
                if (this.tickCount < this.nextHeadUpdate[i])
                    continue;

                this.nextHeadUpdate[i] = this.tickCount + 15 + this.random.nextInt(15);
                int targetId = this.getAlternativeTarget(i + 1);
                if (targetId > 0) {
                    LivingEntity targetEntity = (LivingEntity) this.level().getEntity(targetId);
                    if (targetEntity == null || targetEntity.isDeadOrDying() || !this.canAttack(targetEntity) || !this.getSensing().hasLineOfSight(targetEntity) || targetId == this.getAlternativeTarget(0) || targetEntity.distanceToSqr(this) > 625f) {
                        this.setAlternativeTarget(i + 1, this.findNewTarget());
                    }
                }
                else {
                    this.setAlternativeTarget(i + 1, this.findNewTarget());
                }
            }

            if (this.getTarget() != null)
                this.setAlternativeTarget(0, this.getTarget().getId());
            else
                this.setAlternativeTarget(0, 0);

            if (this.destroyBlocksTick > 0) {
                --this.destroyBlocksTick;
                if (this.destroyBlocksTick == 0 && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this)) {
                    int y = Mth.floor(this.getY());
                    int x = Mth.floor(this.getX());
                    int z = Mth.floor(this.getZ());
                    boolean hasDestroyedBlock = false;

                    for (int x1 = -1; x1 <= 1; ++x1) {
                        for (int z1 = -1; z1 <= 1; ++z1) {
                            for (int y1 = this.isPowered() ? 0 : -1; y1 <= 4; ++y1) {
                                int xToBreak = x + x1;
                                int yToBreak = y + y1;
                                int zToBreak = z + z1;
                                BlockPos blockPos = new BlockPos(xToBreak, yToBreak, zToBreak);
                                BlockState blockState = this.level().getBlockState(blockPos);
                                if (canDestroyBlock(blockPos, blockState) && net.minecraftforge.event.ForgeEventFactory.onEntityDestroyBlock(this, blockPos, blockState)) {
                                    hasDestroyedBlock = this.level().destroyBlock(blockPos, true, this) || hasDestroyedBlock;
                                }
                            }
                        }
                    }

                    if (hasDestroyedBlock) {
                        this.level().levelEvent(null, LevelEvent.SOUND_WITHER_BLOCK_BREAK, this.blockPosition(), 0);
                    }
                }
            }

            float regen = this.stats.health.regeneration / 20f;
            if (this.stats.health.regenWhenHit != this.stats.health.regeneration && this.tickCount > this.getLastHurtByMobTimestamp() && this.tickCount - this.getLastHurtByMobTimestamp() < this.stats.health.regenWhenHitDuration)
                regen = this.stats.health.regenWhenHit / 20f;
            this.heal(regen);

            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

            if (!this.isCharging() && this.secondPhaseCharge > 0 && this.forceChargeTicks > 0) {
                this.forceChargeTicks--;
                if (this.forceChargeTicks == 0) {
                    this.initCharging();
                    this.entityData.set(CHARGING, this.getChargingTicks() - (3 - this.secondPhaseCharge) * 8);
                    this.secondPhaseCharge--;
                    this.forceChargeTicks = this.random.nextInt(6) + 3;
                }
            }
            if (!this.isCharging())
                this.tickMinion();
            this.tickCharging();
            this.tickBarrage();
        }
    }

    public boolean needsHealing() {
        return this.getHealth() / this.getMaxHealth() < this.stats.attack.attackToHealThreshold;
    }



    public boolean canDestroyBlock(BlockPos pos, BlockState state) {
        if (this.stats.misc.ignoreWitherProofBlocks)
            return !state.isAir() && state.getDestroySpeed(this.level(), pos) >= 0f;
        else
            return !state.isAir() && !state.is(BlockTags.WITHER_IMMUNE);
    }

    /**
     * @deprecated Forge: Use {@link BlockState#canEntityDestroy(net.minecraft.world.level.BlockGetter, BlockPos, Entity)} instead.
     */
    @Deprecated
    public static boolean canDestroy(BlockState pState) {
        return !pState.isAir() && !pState.is(BlockTags.WITHER_IMMUNE);
    }

    /**
     * Initializes this Wither's explosion sequence and makes it invulnerable. Called immediately after spawning.
     */
    public void makeInvulnerable() {
        this.setInvulnerableTicks(INVULNERABLE_TICKS);
        this.bossEvent.setProgress(0.0F);
        //this.setHealth(this.getMaxHealth() / 3.0F);
    }

    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier) {
    }

    /**
     * Add the given player to the list of players tracking this entity. For instance, a player may track a boss in order
     * to view its associated boss bar.
     */
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    /**
     * Removes the given player from the list of players tracking this entity. See {@link Entity#startSeenByPlayer(ServerPlayer)} for
     * more information on tracking.
     */
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    public double getHeadX(int pHead) {
        if (pHead <= 0) {
            return this.getX();
        } else {
            float f = (this.yBodyRot + (float)(180 * (pHead - 1))) * ((float)Math.PI / 180F);
            float f1 = Mth.cos(f);
            return this.getX() + (double)f1 * 1.3D;
        }
    }

    public double getHeadY(int pHead) {
        return pHead <= 0 ? this.getY() + 3.0D : this.getY() + 2.2D;
    }

    public double getHeadZ(int pHead) {
        if (pHead <= 0) {
            return this.getZ();
        } else {
            float f = (this.yBodyRot + (float)(180 * (pHead - 1))) * ((float)Math.PI / 180F);
            float f1 = Mth.sin(f);
            return this.getZ() + (double)f1 * 1.3D;
        }
    }

    private float rotLerp(float pAngle, float p_31444_, float p_31445_) {
        float f = Mth.wrapDegrees(p_31444_ - pAngle);
        if (f > p_31445_) {
            f = p_31445_;
        }

        if (f < -p_31445_) {
            f = -p_31445_;
        }

        return pAngle + f;
    }

    public void performRangedAttack(int head, LivingEntity target) {
        this.performRangedAttack(
                head,
                target.getX(),
                target.getY() + (double)target.getEyeHeight() * 0.5D,
                target.getZ(),
                head == 0 && this.random.nextFloat() < this.stats.attack.dangerousSkullChance.getValue(this) * (this.shotSkulls / 10f)
        );
    }

    /**
     * Launches a Wither skull toward (pX, pY, pZ)
     */
    public void performRangedAttack(int head, double pX, double pY, double pZ, boolean pIsDangerous) {
        if (head == 0)
            this.shotSkulls++;
        if (!this.isSilent())
            this.level().playSound(null, this.blockPosition(), SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 4.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

        double d0 = this.getHeadX(head);
        double d1 = this.getHeadY(head);
        double d2 = this.getHeadZ(head);
        double d3 = pX - d0;
        double d4 = pY - d1;
        double d5 = pZ - d2;
        PBWitherSkull witherskull = new PBWitherSkull(this.level(), this, d3, d4, d5);
        witherskull.setOwner(this);
        if (pIsDangerous) {
            witherskull.setDangerous(true);
            this.shotSkulls = 0;
        }

        witherskull.setPosRaw(d0, d1, d2);
        this.level().addFreshEntity(witherskull);
    }

    /**
     * Attack the specified entity using a ranged attack.
     */
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        this.performRangedAttack(0, pTarget);
    }

    @Override
    public void heal(float healAmount) {
        if (this.isPowered() && this.getHealth() + healAmount > this.getMaxHealth() / 2f) {
            healAmount = this.getMaxHealth() / 2f - this.getHealth();
        }
        boolean wasPowered = this.isPowered();
        super.heal(healAmount);
        updateStats(wasPowered);
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource))
            return false;

        if (pSource.is(DamageTypeTags.WITHER_IMMUNE_TO) || (pSource.getEntity() instanceof PBWither && pSource.getDirectEntity() instanceof PBWitherSkull skull && !skull.isDangerous()))
            return false;

        if (this.getInvulnerableTicks() > 0 && !pSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;

        if (this.isPowered()) {
            Entity entity = pSource.getDirectEntity();
            if (entity instanceof AbstractArrow) {
                return false;
            }
        }

        Entity damagingEntity = pSource.getEntity();
        if (!(damagingEntity instanceof Player) && damagingEntity instanceof LivingEntity && ((LivingEntity) damagingEntity).getMobType() == this.getMobType()) {
            return false;
        }
        else {
            if (this.destroyBlocksTick <= 0)
                this.destroyBlocksTick = 10;

            this.nextHeadUpdate[this.random.nextInt(2)] -= 8;

            boolean wasPowered = this.isPowered();
            boolean hurt = super.hurt(pSource, pAmount);
            if (hurt && !wasPowered && this.isPowered()) {
                this.initCharging();
                this.secondPhaseCharge = 2;
                this.forceChargeTicks = this.random.nextInt(6) + 3;
            }
            return hurt;
        }
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        //If it's magic damage
        if (damageSource.is(DamageTypes.MAGIC) || damageSource.is(DamageTypes.INDIRECT_MAGIC))
            damageAmount *= 1.5f;
        if (damageSource.getDirectEntity() instanceof PBWitherSkull witherSkull && witherSkull.isDangerous())
            damageAmount *= this.getMaxHealth() * 0.008f;
        boolean wasPowered = this.isPowered();
        super.actuallyHurt(damageSource, damageAmount);
        updateStats(wasPowered);

        if (!this.isDeadOrDying()) {
            tryCharge(damageAmount);
            tryBarrage(damageAmount);
        }
    }

    /**
     * Makes the entity despawn if requirements are reached
     */
    public void checkDespawn() {
        if ((this.level().getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful())
                || this.getY() < this.level().getMinBuildHeight() - 32) {
            this.discard();
        } else {
            this.noActionTime = 0;
        }
    }

    public boolean addEffect(MobEffectInstance pEffectInstance, @javax.annotation.Nullable Entity pEntity) {
        return false;
    }

    public float getHeadYRot(int pHead) {
        return this.yRotHeads[pHead];
    }

    public float getHeadXRot(int pHead) {
        return this.xRotHeads[pHead];
    }

    public int getInvulnerableTicks() {
        return this.entityData.get(DATA_ID_INV);
    }

    public void setInvulnerableTicks(int pInvulnerableTicks) {
        this.entityData.set(DATA_ID_INV, pInvulnerableTicks);
    }

    /**
     * Returns the target entity ID if present, or 0 if not
     * @param pHead The target offset, should be from 0-2
     */
    public int getAlternativeTarget(int pHead) {
        return this.entityData.get(DATA_TARGETS.get(pHead));
    }

    /**
     * Updates the target entity ID
     */
    public void setAlternativeTarget(int pTargetOffset, int pNewId) {
        this.entityData.set(DATA_TARGETS.get(pTargetOffset), pNewId);
    }

    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() / 2.0F;
    }

    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    protected boolean canRide(Entity pEntity) {
        return false;
    }

    /**
     * Returns false if this Entity can't move between dimensions. True if it can.
     */
    public boolean canChangeDimensions() {
        return false;
    }

    public boolean canBeAffected(MobEffectInstance pPotioneffect) {
        return pPotioneffect.getEffect() != MobEffects.WITHER && super.canBeAffected(pPotioneffect);
    }

    public static class WaterAvoidingRandomFlyingGoal extends RandomStrollGoal {
        public WaterAvoidingRandomFlyingGoal(PathfinderMob pathfinderMob) {
            super(pathfinderMob, 2f, 7);
        }

        @Nullable
        protected Vec3 getPosition() {
            Vec3 vec3 = this.mob.getViewVector(0.0F);
            Vec3 vec31 = HoverRandomPos.getPos(this.mob, 16, 14, vec3.x, vec3.z, ((float)Math.PI / 2F), 6, 2);
            return vec31 != null ? vec31 : AirAndWaterRandomPos.getPos(this.mob, 16, 8, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 300.0d)
                .add(Attributes.FOLLOW_RANGE, 64.0d)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1d)
                .add(Attributes.MOVEMENT_SPEED, 0.6d)
                .add(Attributes.FLYING_SPEED, 0.6d);
    }
}
