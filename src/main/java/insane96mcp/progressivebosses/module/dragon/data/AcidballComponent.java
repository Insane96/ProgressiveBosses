package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.areaeffectcloud3d.entity.Cloud3DEntity;
import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.mixin.accessor.ProjectileAccessor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

@JsonAdapter(AcidballComponent.Serializer.class)
public class AcidballComponent implements DragonComponent {
    public DragonValue acidAmplifier;
    @Nullable
    public DragonValue speedMultiplier;
    @Nullable
    public DragonValue impactRange;
    public DragonValue impactDamage;
    public boolean is3DCloud;

    static ResourceKey<DamageType> DRAGON_FIREBALL_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ProgressiveBosses.id("dragon_fireball"));

    /// Returns true if the vanilla cloud must be canceled
    public boolean onAcidBallImpact(DragonFireball fireball, EnderDragon dragon, HitResult result) {
        onImpactExplosion(fireball, dragon, result);
        return onImpact3DCloud(fireball, dragon, result);
    }

    private void onImpactExplosion(DragonFireball fireball, EnderDragon dragon, HitResult result) {
        if (this.impactDamage == null || this.impactRange == null)
            return;

        float impactRange = this.impactRange.getValue(dragon);
        float sqrImpactRange = impactRange * impactRange;
        AABB axisAlignedBB = new AABB(result.getLocation(), result.getLocation()).inflate(impactRange);
        List<LivingEntity> livingEntities = fireball.level().getEntitiesOfClass(LivingEntity.class, axisAlignedBB);
        for (LivingEntity livingEntity : livingEntities) {
            if (livingEntity.distanceToSqr(fireball.position()) < sqrImpactRange)
                livingEntity.hurt(livingEntity.damageSources().source(DRAGON_FIREBALL_DAMAGE_TYPE, fireball, dragon), this.impactDamage.getValue(dragon));
        }
    }

    private boolean onImpact3DCloud(DragonFireball fireball, EnderDragon dragon, HitResult result) {
        if (!this.is3DCloud)
            return false;
        HitResult.Type hitResult$type = result.getType();
        if (hitResult$type == HitResult.Type.ENTITY)
            ((ProjectileAccessor)fireball).invokeOnHitEntity((EntityHitResult) result);
        else if (hitResult$type == HitResult.Type.BLOCK)
            ((ProjectileAccessor)fireball).invokeOnHitBlock((BlockHitResult) result);
        //noinspection DataFlowIssue - I check if the HitResult is an entity first so the cast shouldn't fail
        if (result.getType() != HitResult.Type.ENTITY || !((EntityHitResult)result).getEntity().is(dragon)) {
            if (!fireball.level().isClientSide) {
                List<LivingEntity> list = fireball.level().getEntitiesOfClass(LivingEntity.class, fireball.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
                Cloud3DEntity areaEffectCloud = new Cloud3DEntity(fireball.level(), fireball.getX(), fireball.getY(), fireball.getZ());
                areaEffectCloud.setOwner(dragon);
                areaEffectCloud.setParticle(ParticleTypes.DRAGON_BREATH);
                //TODO More configuration!
                areaEffectCloud.setRadius(3.0F);
                areaEffectCloud.setDuration(300);
                areaEffectCloud.setWaitTime(10);
                areaEffectCloud.setRadiusPerTick((7.0F - areaEffectCloud.getRadius()) / (float) areaEffectCloud.getDuration());
                areaEffectCloud.addEffect(new MobEffectInstance(MobEffects.HARM, 1, this.acidAmplifier.getIntValue(dragon)));
                if (!list.isEmpty()) {
                    for (LivingEntity livingentity : list) {
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

    public static class Serializer implements JsonDeserializer<AcidballComponent> {
        @Override
        public AcidballComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            AcidballComponent component = new AcidballComponent();
            JsonObject jObject = json.getAsJsonObject();
            component.acidAmplifier = GsonHelper.getAsObject(jObject, "acid_amplifier", context, DragonValue.class);
            component.speedMultiplier = GsonHelper.getAsObject(jObject, "speed_multiplier", null, context, DragonValue.class);
            component.impactRange = GsonHelper.getAsObject(jObject, "impact_range", context, DragonValue.class);
            component.impactDamage = GsonHelper.getAsObject(jObject, "impact_damage", context, DragonValue.class);
            component.is3DCloud = GsonHelper.getAsBoolean(jObject, "is_3d_cloud", false);
            return component;
        }
    }
}
