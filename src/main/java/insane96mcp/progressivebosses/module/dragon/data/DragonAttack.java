package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.mixin.ProjectileInvoker;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonAttack.Serializer.class)
public class DragonAttack {
    public float meleeDamageDealtMultiplier;
    public float acidDamageDealtMultiplier;
    public DragonValue chargeChance;
    public DragonValue strafeChance;
    public float acidballSpeedMultiplier;
    public float acidballImpactDamage;
    public int minAcidballShot;
    public int maxAcidballShot;

    public DragonAttack(float meleeDamageDealtMultiplier, float acidDamageDealtMultiplier, DragonValue chargeChance, DragonValue strafeChance, float acidballSpeedMultiplier, float acidballImpactDamage, int minAcidballShot, int maxAcidballShot) {
        this.meleeDamageDealtMultiplier = meleeDamageDealtMultiplier;
        this.acidDamageDealtMultiplier = acidDamageDealtMultiplier;
        this.chargeChance = chargeChance;
        this.strafeChance = strafeChance;
        this.acidballSpeedMultiplier = acidballSpeedMultiplier;
        this.acidballImpactDamage = acidballImpactDamage;
        this.minAcidballShot = minAcidballShot;
        this.maxAcidballShot = maxAcidballShot;
    }

    public static class Serializer implements JsonSerializer<DragonAttack>, JsonDeserializer<DragonAttack> {
        @Override
        public DragonAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new DragonAttack(
                    GsonHelper.getAsFloat(jObject, "melee_damage_dealt_multiplier"),
                    GsonHelper.getAsFloat(jObject, "acid_damage_dealt_multiplier"),
                    context.deserialize(jObject.get("charge_chance"), DragonValue.class),
                    context.deserialize(jObject.get("strafe_chance"), DragonValue.class),
                    GsonHelper.getAsFloat(jObject, "acidball_speed_multiplier"),
                    GsonHelper.getAsFloat(jObject, "acidball_impact_damage"),
                    GsonHelper.getAsInt(jObject, "min_acidball_shot"),
                    GsonHelper.getAsInt(jObject, "max_acidball_shot")
            );
        }

        @Override
        public JsonElement serialize(DragonAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("melee_damage_dealt_multiplier", src.meleeDamageDealtMultiplier);
            jsonObject.addProperty("acid_damage_dealt_multiplier", src.acidDamageDealtMultiplier);
            jsonObject.add("charge_chance", context.serialize(src.chargeChance));
            jsonObject.add("strafe_chance", context.serialize(src.strafeChance));
            jsonObject.addProperty("acidball_speed_multiplier", src.acidballSpeedMultiplier);
            jsonObject.addProperty("acidball_impact_damage", src.acidballImpactDamage);
            jsonObject.addProperty("min_acidball_shot", src.minAcidballShot);
            jsonObject.addProperty("max_acidball_shot", src.maxAcidballShot);
            return jsonObject;
        }
    }

    private static final String FORCE_CHARGE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_charge";
    private static final String FORCE_STRAFE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_strafe";
    private static final String FORCE_BLAST_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_blast";

    public static void onHurtLiving(LivingHurtEvent event) {
        onDirectDamage(event);
        onAcidDamage(event);
    }

    private static void onDirectDamage(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof EnderDragon dragon)
                || event.getEntity() instanceof EnderDragon)
            return;
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        event.setAmount(event.getAmount() * stats.get().attack.meleeDamageDealtMultiplier);
    }

    private static void onAcidDamage(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof EnderDragon dragon)
                || !(event.getSource().getDirectEntity() instanceof AreaEffectCloud))
            return;
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        event.setAmount(event.getAmount() * stats.get().attack.acidDamageDealtMultiplier);
    }

    public static void setAcidBallSpeedMultiplier(Entity entity) {
        if (!(entity instanceof DragonFireball acidball)
                || !(acidball.getOwner() instanceof EnderDragon dragon))
            return;
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return;

        if (Math.abs(acidball.xPower) > 10 || Math.abs(acidball.yPower) > 10 || Math.abs(acidball.zPower) > 10) {
            entity.kill();
            return;
        }

        acidball.xPower *= stats.get().attack.acidballSpeedMultiplier;
        acidball.yPower *= stats.get().attack.acidballSpeedMultiplier;
        acidball.zPower *= stats.get().attack.acidballSpeedMultiplier;
    }

    /**
     * Returns true if the dragon has either strafed or charged the player
     */
    public static boolean onHoldingPatternEnd(EnderDragon dragon, DragonStats stats) {
        boolean chargePlayer = shouldChargePlayer(dragon, stats);
        boolean strafe = shouldStrafe(dragon, stats);

        if (chargePlayer && strafe) {
            if (dragon.getRandom().nextBoolean())
                chargePlayer(dragon);
            else
                strafePlayer(dragon);
        }
        else if (chargePlayer)
            chargePlayer(dragon);
        else if (strafe)
            strafePlayer(dragon);

        return chargePlayer || strafe;
    }

    public static boolean isForcedToCharge(EnderDragon dragon) {
        return getForcedToCharge(dragon) > 0;
    }

    public static int getForcedToCharge(EnderDragon dragon) {
        return dragon.getPersistentData().getInt(FORCE_CHARGE_TAG);
    }

    public static void setForcedToCharge(EnderDragon dragon, int forcedToCharge) {
        dragon.getPersistentData().putInt(FORCE_CHARGE_TAG, forcedToCharge);
    }

    public static boolean shouldChargePlayer(EnderDragon dragon, DragonStats stats) {
        if (isForcedToCharge(dragon))
            return true;

        double chance = stats.attack.chargeChance.getValue(dragon);
        if (chance == 0f)
            return false;
        if (DragonAnger.isAngered(dragon))
            chance *= 2f;

        return dragon.getRandom().nextDouble() < chance;
    }

    public static void chargePlayer(EnderDragon dragon) {
        if (!isPlayerInRange(dragon.level(), 64))
            return;

        dragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
        Player player = getRandomPlayerWithCrystalPriority(dragon.level(), 64);
        if (player == null)
            return;
        if (isForcedToCharge(dragon))
            setForcedToCharge(dragon, getForcedToCharge(dragon) - 1);
        dragon.getPhaseManager().getPhase(EnderDragonPhase.CHARGING_PLAYER).setTarget(player.position());
    }

    public static boolean isForcedToStrafe(EnderDragon dragon) {
        return getForcedToStrafe(dragon) > 0;
    }

    public static int getForcedToStrafe(EnderDragon dragon) {
        return dragon.getPersistentData().getInt(FORCE_STRAFE_TAG);
    }

    public static void setForcedToStrafe(EnderDragon dragon, int forcedToStrafe) {
        dragon.getPersistentData().putInt(FORCE_STRAFE_TAG, forcedToStrafe);
    }

    public static boolean shouldStrafe(EnderDragon dragon, DragonStats stats) {
        if (isForcedToStrafe(dragon))
            return true;

        double chance = stats.attack.strafeChance.getValue(dragon);
        if (chance == 0f)
            return false;
        if (DragonAnger.isAngered(dragon))
            chance *= 2f;
        return dragon.getRandom().nextDouble() < chance;
    }

    public static void strafePlayer(EnderDragon dragon) {
        if (!isPlayerInRange(dragon.level(), 64))
            return;

        dragon.getPhaseManager().setPhase(PBDragonStrafePlayerPhase.getPhaseType());
        Player player = getRandomPlayerWithCrystalPriority(dragon.level(), 64);
        if (player == null)
            return;

        if (isForcedToStrafe(dragon))
            setForcedToStrafe(dragon, getForcedToStrafe(dragon) - 1);
        dragon.getPhaseManager().getPhase(PBDragonStrafePlayerPhase.getPhaseType()).setTarget(player);
    }

    public static boolean isForcedToBlast(EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(FORCE_BLAST_TAG);
    }

    public static void setForcedToBlast(EnderDragon dragon, boolean forcedToBlast) {
        dragon.getPersistentData().putBoolean(FORCE_BLAST_TAG, forcedToBlast);
    }

    public static void blast(DragonPhaseEvent.Change event, EnderDragon dragon) {
        if (event.getOldPhase() != EnderDragonPhase.HOVERING &&
                (dragon.getPhaseManager().getPhase(event.getOldPhase()).isSitting() || dragon.getPhaseManager().getPhase(event.getNewPhase()).isSitting())) {
            event.setNewPhase(DragonBlastAttackPhase.getPhaseType());
            setForcedToBlast(dragon, false);
        }
        else if (event.getNewPhase() != EnderDragonPhase.LANDING)
            event.setNewPhase(EnderDragonPhase.LANDING_APPROACH);
    }

    public static boolean isPlayerInRange(Level level, int range) {
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(range);
        List<Player> players = level.getEntitiesOfClass(Player.class, bb, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        return !players.isEmpty();
    }

    //Returns a random player that is at least 12 blocks near a Crystal or a random player if no players are near crystals
    @Nullable
    public static Player getRandomPlayerWithCrystalPriority(Level level, int range) {
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB boundingBox = new AABB(centerPodium).inflate(range);
        List<Player> players = level.getEntitiesOfClass(Player.class, boundingBox);
        if (players.isEmpty())
            return null;

        List<Player> playersNearCrystals = new ArrayList<>();

        for (Player player : players) {
            List<EndCrystal> endCrystals = player.level().getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(12d), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
            if (!endCrystals.isEmpty())
                playersNearCrystals.add(player);
        }

        int p;
        if (playersNearCrystals.isEmpty()) {
            p = Mth.nextInt(level.random, 0, players.size() - 1);
            return players.get(p);
        }

        p = Mth.nextInt(level.random, 0, playersNearCrystals.size() - 1);
        return playersNearCrystals.get(p);
    }

    static ResourceKey<DamageType> DRAGON_FIREBALL_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "dragon_fireball"));

    public static boolean onAcidBallImpact(DragonFireball fireball, @Nullable Entity shooter, HitResult result) {
        if (!(shooter instanceof EnderDragon dragon)
                || dragon.level().isClientSide)
            return false;
        Optional<DragonStats> stats = DragonFeature.getDragonStats(dragon);
        if (stats.isEmpty())
            return false;

        onImpactExplosion(fireball, shooter, result, stats.get());
        return onImpact3DCloud(fireball, shooter, result, stats.get());
    }

    private static void onImpactExplosion(DragonFireball fireball, @Nullable Entity shooter, HitResult result, DragonStats stats) {
        if (stats.attack.acidballImpactDamage == 0f)
            return;

        AABB axisAlignedBB = new AABB(result.getLocation(), result.getLocation()).inflate(5d);
        List<LivingEntity> livingEntities = fireball.level().getEntitiesOfClass(LivingEntity.class, axisAlignedBB);
        for (LivingEntity livingEntity : livingEntities) {
            if (livingEntity.distanceToSqr(fireball.position()) < 25d)
                livingEntity.hurt(livingEntity.damageSources().source(DRAGON_FIREBALL_DAMAGE_TYPE, fireball, shooter), stats.attack.acidballImpactDamage);
        }
    }

    private static boolean onImpact3DCloud(DragonFireball fireball, @Nullable Entity shooter, HitResult result, DragonStats stats) {
        HitResult.Type hitResult$type = result.getType();
        if (hitResult$type == HitResult.Type.ENTITY)
            ((ProjectileInvoker)fireball).invokeOnHitEntity((EntityHitResult) result);
        else if (hitResult$type == HitResult.Type.BLOCK)
            ((ProjectileInvoker)fireball).invokeOnHitBlock((BlockHitResult) result);
        //noinspection DataFlowIssue - I check if the HitResult is an entity first so the cast shouldn't fail
        if (shooter != null && (result.getType() != HitResult.Type.ENTITY || !((EntityHitResult)result).getEntity().is(shooter))) {
            if (!fireball.level().isClientSide) {
                List<LivingEntity> list = fireball.level().getEntitiesOfClass(LivingEntity.class, fireball.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
                AreaEffectCloud3DEntity areaEffectCloud = new AreaEffectCloud3DEntity(fireball.level(), fireball.getX(), fireball.getY(), fireball.getZ());
                if (shooter instanceof LivingEntity) {
                    areaEffectCloud.setOwner((LivingEntity)shooter);
                }

                areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                areaEffectCloud.setRadius(3.0F);
                areaEffectCloud.setDuration(300);
                areaEffectCloud.setWaitTime(10);
                areaEffectCloud.setRadiusPerTick((7.0F - areaEffectCloud.getRadius()) / (float) areaEffectCloud.getDuration());
                areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, 1));
                if (!list.isEmpty()) {
                    for(LivingEntity livingentity : list) {
                        double d0 = fireball.distanceToSqr(livingentity);
                        if (d0 < 16.0D) {
                            areaEffectCloud.setPos(livingentity.getX(), livingentity.getY(), livingentity.getZ());
                            break;
                        }
                    }
                }

                fireball.level().levelEvent(2006, fireball.blockPosition(), fireball.isSilent() ? -1 : 1);
                fireball.level().addFreshEntity(areaEffectCloud);
                fireball.discard();
            }
        }

        return true;
    }
}
