package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.event.PBEventFactory;
import insane96mcp.progressivebosses.mixin.ProjectileInvoker;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@JsonAdapter(DragonAttack.Serializer.class)
public class DragonAttack {
    public float meleeDamage;
    public float meleeHeadDamage;
    public int acidAmplifier;
    public DragonValue chargeChance;
    public DragonValue strafeChance;
    public DragonValue blastChance;
    public float acidballSpeedMultiplier;
    public float acidballImpactDamage;
    public float blastDamage;
    public int minAcidballShot;
    public int maxAcidballShot;

    public DragonAttack(float meleeDamage, float meleeHeadDamage, int acidAmplifier, DragonValue chargeChance, DragonValue strafeChance, DragonValue blastChance, float acidballSpeedMultiplier, float acidballImpactDamage, float blastDamage, int minAcidballShot, int maxAcidballShot) {
        this.meleeDamage = meleeDamage;
        this.meleeHeadDamage = meleeHeadDamage;
        this.acidAmplifier = acidAmplifier;
        this.chargeChance = chargeChance;
        this.strafeChance = strafeChance;
        this.blastChance = blastChance;
        this.acidballSpeedMultiplier = acidballSpeedMultiplier;
        this.acidballImpactDamage = acidballImpactDamage;
        this.blastDamage = blastDamage;
        this.minAcidballShot = minAcidballShot;
        this.maxAcidballShot = maxAcidballShot;
    }

    public static class Serializer implements JsonSerializer<DragonAttack>, JsonDeserializer<DragonAttack> {
        @Override
        public DragonAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            return new DragonAttack(
                    GsonHelper.getAsFloat(jObject, "melee_damage"),
                    GsonHelper.getAsFloat(jObject, "melee_head_damage"),
                    GsonHelper.getAsInt(jObject, "acid_amplifier"),
                    context.deserialize(jObject.get("charge_chance"), DragonValue.class),
                    context.deserialize(jObject.get("strafe_chance"), DragonValue.class),
                    context.deserialize(jObject.get("blast_chance"), DragonValue.class),
                    GsonHelper.getAsFloat(jObject, "acidball_speed_multiplier"),
                    GsonHelper.getAsFloat(jObject, "acidball_impact_damage"),
                    GsonHelper.getAsFloat(jObject, "blast_damage"),
                    GsonHelper.getAsInt(jObject, "min_acidball_shot"),
                    GsonHelper.getAsInt(jObject, "max_acidball_shot")
            );
        }

        @Override
        public JsonElement serialize(DragonAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("melee_damage", src.meleeDamage);
            jsonObject.addProperty("melee_head_damage", src.meleeHeadDamage);
            jsonObject.addProperty("acid_amplifier", src.acidAmplifier);
            jsonObject.add("charge_chance", context.serialize(src.chargeChance));
            jsonObject.add("strafe_chance", context.serialize(src.strafeChance));
            jsonObject.add("blast_chance", context.serialize(src.blastChance));
            jsonObject.addProperty("acidball_speed_multiplier", src.acidballSpeedMultiplier);
            jsonObject.addProperty("acidball_impact_damage", src.acidballImpactDamage);
            jsonObject.addProperty("blast_damage", src.blastDamage);
            jsonObject.addProperty("min_acidball_shot", src.minAcidballShot);
            jsonObject.addProperty("max_acidball_shot", src.maxAcidballShot);
            return jsonObject;
        }
    }

    private static final String FORCE_CHARGE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_charge";
    private static final String FORCE_STRAFE_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_strafe";
    private static final String FORCE_BLAST_TAG = ProgressiveBosses.RESOURCE_PREFIX + "force_blast";
    public static final String LAST_BLAST_TAG = ProgressiveBosses.RESOURCE_PREFIX + "last_blast";

    public static float meleeDamage(EnderDragon dragon, float originalDamage) {
        DragonDefinition stats = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (stats == null || stats.attack == null)
            return originalDamage;

        return stats.attack.meleeDamage;
    }

    public static float meleeHeadDamage(EnderDragon dragon, float originalDamage) {
        DragonDefinition stats = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (stats == null || stats.attack == null)
            return originalDamage;

        return stats.attack.meleeHeadDamage;
    }

    public static void setAcidBallSpeedMultiplier(Entity entity) {
        if (!(entity instanceof DragonFireball acidball)
                || !(acidball.getOwner() instanceof EnderDragon dragon))
            return;
        Optional<DragonDefinition> stats = DragonFeature.getDragonDefinition(dragon);
        if (stats.isEmpty()
                || stats.get().attack == null)
            return;

        if (Math.abs(acidball.xPower) > 10 || Math.abs(acidball.yPower) > 10 || Math.abs(acidball.zPower) > 10) {
            entity.kill();
            return;
        }

        acidball.xPower *= stats.get().attack.acidballSpeedMultiplier;
        acidball.yPower *= stats.get().attack.acidballSpeedMultiplier;
        acidball.zPower *= stats.get().attack.acidballSpeedMultiplier;
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

    public static boolean shouldCharge(EnderDragon dragon, DragonDefinition stats) {
        if (isForcedToCharge(dragon))
            return true;

        double chance = stats.attack == null ? 0f : stats.attack.chargeChance.getValue(dragon);
        if (chance == 0f)
            return false;

        return dragon.getRandom().nextDouble() < chance;
    }

    public static void charge(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        event.setNewPhase(EnderDragonPhase.CHARGING_PLAYER);
        if (isForcedToCharge(dragon))
            setForcedToCharge(dragon, getForcedToCharge(dragon) - 1);
        if (forceBegin) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
            phase.begin();
            PBEventFactory.onDragonPhaseBegin(dragon, phase);
        }
    }

    public static void onChargeBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {
        if (event.getPhaseInstance().getPhase() != EnderDragonPhase.CHARGING_PLAYER)
            return;
        Player player = getRandomPlayer(dragon, dragon.level(), 96);
        if (player == null)
            return;
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

    public static boolean shouldStrafe(EnderDragon dragon, DragonDefinition stats) {
        if (isForcedToStrafe(dragon))
            return true;

        double chance = stats.attack == null ? 0f : stats.attack.strafeChance.getValue(dragon);
        if (chance == 0f)
            return false;
        return dragon.getRandom().nextDouble() < chance;
    }

    public static void strafe(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        event.setNewPhase(PBDragonStrafePlayerPhase.getPhaseType());
        if (isForcedToStrafe(dragon))
            setForcedToStrafe(dragon, getForcedToStrafe(dragon) - 1);
        if (forceBegin) {
            DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
            phase.begin();
            PBEventFactory.onDragonPhaseBegin(dragon, phase);
        }
    }

    public static void onStrafeBegin(DragonPhaseEvent.Begin event, EnderDragon dragon) {
        if (event.getPhaseInstance().getPhase() != PBDragonStrafePlayerPhase.getPhaseType())
            return;
        Player player = getRandomPlayer(dragon, dragon.level(), 96);
        if (player == null)
            return;
        dragon.getPhaseManager().getPhase(PBDragonStrafePlayerPhase.getPhaseType()).setTarget(player);
    }

    public static boolean isForcedToBlast(EnderDragon dragon) {
        return dragon.getPersistentData().getBoolean(FORCE_BLAST_TAG);
    }

    public static void setForcedToBlast(EnderDragon dragon, boolean forcedToBlast) {
        dragon.getPersistentData().putBoolean(FORCE_BLAST_TAG, forcedToBlast);
    }

    public static boolean shouldBlast(EnderDragon dragon, DragonDefinition stats) {
        if (isForcedToBlast(dragon))
            return true;
        if (DragonBlastAttackPhase.isInCooldown(dragon, dragon.level()) || stats.attack == null)
            return false;

        double chance = stats.attack.blastChance.getValue(dragon);
        if (chance == 0f)
            return false;
        return dragon.getRandom().nextDouble() < chance;
    }

    public static void blast(DragonPhaseEvent.Change event, EnderDragon dragon, boolean forceBegin) {
        if (event.getOldPhase() != EnderDragonPhase.HOVERING &&
                (dragon.getPhaseManager().getPhase(event.getOldPhase()).isSitting() || dragon.getPhaseManager().getPhase(event.getNewPhase()).isSitting())) {
            event.setNewPhase(DragonBlastAttackPhase.getPhaseType());
            if (forceBegin) {
                DragonPhaseInstance phase = dragon.getPhaseManager().getPhase(event.getNewPhase());
                phase.begin();
                PBEventFactory.onDragonPhaseBegin(dragon, phase);
            }
            setForcedToBlast(dragon, false);
        }
        else
            event.setNewPhase(EnderDragonPhase.LANDING);
    }

    @Nullable
    public static Player getRandomPlayer(EnderDragon dragon, Level level, int range) {
        List<Player> players = level.getEntitiesOfClass(Player.class, dragon.getBoundingBox().inflate(range));
        if (players.isEmpty())
            return null;

        return players.get(Mth.nextInt(level.random, 0, players.size() - 1));
    }

    static ResourceKey<DamageType> DRAGON_FIREBALL_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "dragon_fireball"));

    public static boolean onAcidBallImpact(DragonFireball fireball, @Nullable Entity shooter, HitResult result) {
        if (!(shooter instanceof EnderDragon dragon)
                || dragon.level().isClientSide)
            return false;
        DragonDefinition stats = DragonFeature.getDragonDefinition(dragon).orElse(null);
        if (stats == null)
            return false;

        onImpactExplosion(fireball, shooter, result, stats);
        return onImpact3DCloud(fireball, shooter, result, stats);
    }

    private static void onImpactExplosion(DragonFireball fireball, @Nullable Entity shooter, HitResult result, DragonDefinition stats) {
        if (stats.attack == null || stats.attack.acidballImpactDamage == 0f)
            return;

        AABB axisAlignedBB = new AABB(result.getLocation(), result.getLocation()).inflate(5d);
        List<LivingEntity> livingEntities = fireball.level().getEntitiesOfClass(LivingEntity.class, axisAlignedBB);
        for (LivingEntity livingEntity : livingEntities) {
            if (livingEntity.distanceToSqr(fireball.position()) < 25d)
                livingEntity.hurt(livingEntity.damageSources().source(DRAGON_FIREBALL_DAMAGE_TYPE, fireball, shooter), stats.attack.acidballImpactDamage);
        }
    }

    private static boolean onImpact3DCloud(DragonFireball fireball, @Nullable Entity shooter, HitResult result, DragonDefinition stats) {
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
                areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, stats.attack == null ? 1 : stats.attack.acidAmplifier));
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
