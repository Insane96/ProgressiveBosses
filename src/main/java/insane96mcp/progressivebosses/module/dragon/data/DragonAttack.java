package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.entity.AreaEffectCloud3DEntity;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.event.DragonPhaseEvent;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import insane96mcp.progressivebosses.setup.Reflection;
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
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
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

    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_PHASES_TO_CHARGE = List.of(EnderDragonPhase.CHARGING_PLAYER, EnderDragonPhase.HOLDING_PATTERN);
    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> VALID_PHASES_TO_STRAFE = List.of(EnderDragonPhase.CHARGING_PLAYER, EnderDragonPhase.HOLDING_PATTERN);

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

    public static boolean onPhaseChange(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        //Replace vanilla Strafe Phase with PB one's
        if (event.getNewPhase().equals(EnderDragonPhase.STRAFE_PLAYER))
            event.setNewPhase(PBDragonStrafePlayerPhase.getPhaseType());

        if (event.getOldPhase() == null)
            return false;

        boolean chargePlayer = shouldChargePlayer(event, dragon, stats);
        boolean strafePlayer = shouldStrafePlayer(event, dragon, stats);

        if (chargePlayer && strafePlayer)
            if (dragon.getRandom().nextBoolean())
                chargePlayer(event, dragon, stats);
            else
                strafePlayer(event, dragon, stats);
        else if (chargePlayer)
            chargePlayer(event, dragon, stats);
        else if (strafePlayer)
            strafePlayer(event, dragon, stats);

        return chargePlayer || strafePlayer;
    }

    public static void onPhaseBegin(DragonPhaseEvent.Begin event, EnderDragon dragon, DragonStats stats) {
        BlockPos centerPodium = dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB boundingBox = new AABB(centerPodium).inflate(64d);
        if (event.getPhaseInstance() instanceof DragonChargePlayerPhase chargePhase) {
            Player player = getRandomPlayerWithCrystalPriority(dragon.level(), boundingBox);
            if (player == null)
                return;
            chargePhase.setTarget(player.position());
        }
        else if (event.getPhaseInstance() instanceof PBDragonStrafePlayerPhase strafePhase) {
            Player player = getRandomPlayerWithCrystalPriority(dragon.level(), boundingBox);
            if (player == null)
                return;
            strafePhase.setTarget(player);
        }
    }

    private static boolean shouldChargePlayer(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        if (!VALID_PHASES_TO_CHARGE.contains(event.getOldPhase()))
            return false;
        double chance = stats.attack.chargeChance.getValue(dragon);
        if (chance == 0f)
            return false;

        BlockPos centerPodium = dragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB boundingBox = new AABB(centerPodium).inflate(64d);
        List<Player> players = dragon.level().getEntitiesOfClass(Player.class, boundingBox, EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        for (Player player : players) {
            List<EndCrystal> endCrystals = player.level().getEntitiesOfClass(EndCrystal.class, player.getBoundingBox().inflate(12d));
            if (!endCrystals.isEmpty()) {
                chance = 1f;
                break;
            }
        }

        return dragon.getRandom().nextDouble() < chance;
    }

    private static void chargePlayer(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        if (!isPlayerInRange(dragon.level(), 64))
            return;

        event.setNewPhase(EnderDragonPhase.CHARGING_PLAYER);
    }

    private static boolean shouldStrafePlayer(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        if (!VALID_PHASES_TO_STRAFE.contains(event.getOldPhase()))
            return false;
        double chance = stats.attack.strafeChance.getValue(dragon);
        if (chance == 0f)
            return false;

        return dragon.getRandom().nextDouble() < chance;
    }

    private static void strafePlayer(DragonPhaseEvent.Change event, EnderDragon dragon, DragonStats stats) {
        if (!isPlayerInRange(dragon.level(), 64))
            return;

        event.setNewPhase(PBDragonStrafePlayerPhase.getPhaseType());
    }

    public static boolean isPlayerInRange(Level level, int range) {
        BlockPos centerPodium = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.END_PODIUM_LOCATION);
        AABB bb = new AABB(centerPodium).inflate(range);
        List<Player> players = level.getEntitiesOfClass(Player.class, bb, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        return !players.isEmpty();
    }

    //Returns a random player that is at least 12 blocks near a Crystal or a random player if no players are near crystals
    @Nullable
    public static Player getRandomPlayerWithCrystalPriority(Level world, AABB boundingBox) {
        List<Player> players = world.getEntitiesOfClass(Player.class, boundingBox);
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
            p = Mth.nextInt(world.random, 0, players.size() - 1);
            return players.get(p);
        }

        p = Mth.nextInt(world.random, 0, playersNearCrystals.size() - 1);
        return playersNearCrystals.get(p);
    }

    static ResourceKey<DamageType> DRAGON_FIREBALL_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ProgressiveBosses.MOD_ID, "dragon_fireball"));

    public static boolean onAcidBallImpact(DragonFireball fireball, @Nullable Entity shooter, HitResult result) {
        if (!(shooter instanceof EnderDragon dragon))
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
            if (livingEntity.distanceToSqr(fireball.position()) < 20.25d)
                livingEntity.hurt(livingEntity.damageSources().source(DRAGON_FIREBALL_DAMAGE_TYPE, fireball, shooter), stats.attack.acidballImpactDamage);
        }
    }

    private static boolean onImpact3DCloud(DragonFireball fireball, @Nullable Entity shooter, HitResult result, DragonStats stats) {
        HitResult.Type hitResult$type = result.getType();
        //TODO Accessors
        if (hitResult$type == HitResult.Type.ENTITY) {
            Reflection.Projectile_onHitEntity(fireball, (EntityHitResult)result);
        }
        else if (hitResult$type == HitResult.Type.BLOCK) {
            Reflection.Projectile_onHitBlock(fireball, (BlockHitResult)result);
        }
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
