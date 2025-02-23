package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

@JsonAdapter(DragonVulnerabilities.Serializer.class)
public class DragonVulnerabilities {
    private static final List<EnderDragonPhase<? extends DragonPhaseInstance>> CENTER_PODIUM_PHASES = Arrays.asList(EnderDragonPhase.SITTING_SCANNING, EnderDragonPhase.SITTING_ATTACKING, EnderDragonPhase.SITTING_FLAMING, EnderDragonPhase.TAKEOFF);

    public float meleeDamageMultiplierWhenSitting;
    public float meleeDamageMultiplierWhenNotSitting;
    public float rangedDamageMultiplier;
    public float explosionDamageMultiplier;
    public float respawningCrystalDamageMultiplier;

    public DragonVulnerabilities(float meleeDamageMultiplierWhenSitting, float meleeDamageMultiplierWhenNotSitting, float rangedDamageMultiplier, float explosionDamageMultiplier, float respawningCrystalDamageMultiplier) {
        this.meleeDamageMultiplierWhenSitting = meleeDamageMultiplierWhenSitting;
        this.meleeDamageMultiplierWhenNotSitting = meleeDamageMultiplierWhenNotSitting;
        this.rangedDamageMultiplier = rangedDamageMultiplier;
        this.explosionDamageMultiplier = explosionDamageMultiplier;
        this.respawningCrystalDamageMultiplier = respawningCrystalDamageMultiplier;
    }

    public static class Serializer implements JsonSerializer<DragonVulnerabilities>, JsonDeserializer<DragonVulnerabilities> {
        @Override
        public DragonVulnerabilities deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonVulnerabilities(
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "melee_damage_multiplier_when_sitting"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "melee_damage_multiplier_when_not_sitting"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "ranged_damage_multiplier"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "explosion_damage_multiplier"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "respawning_crystal_damage_multiplier"));
        }

        @Override
        public JsonElement serialize(DragonVulnerabilities src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("melee_damage_multiplier_when_sitting", src.meleeDamageMultiplierWhenSitting);
            jsonObject.addProperty("melee_damage_multiplier_when_not_sitting", src.meleeDamageMultiplierWhenNotSitting);
            jsonObject.addProperty("ranged_damage_multiplier", src.rangedDamageMultiplier);
            jsonObject.addProperty("explosion_damage_multiplier", src.explosionDamageMultiplier);
            jsonObject.addProperty("respawning_crystal_damage_multiplier", src.respawningCrystalDamageMultiplier);
            return jsonObject;
        }
    }

    public static void damageMultipliers(LivingHurtEvent event, EnderDragon dragon, DragonDefinition stats) {
        meleeDamageMultiplier(event, dragon, stats);
        rangedDamageMultiplier(event, dragon, stats);
        explosionDamageMultiplier(event, dragon, stats);
    }

    private static void meleeDamageMultiplier(LivingHurtEvent event, EnderDragon dragon, DragonDefinition stats) {
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity))
            return;
        if (CENTER_PODIUM_PHASES.contains(dragon.getPhaseManager().getCurrentPhase().getPhase()))
            event.setAmount(event.getAmount() * stats.vulnerabilities.meleeDamageMultiplierWhenSitting);
        else
            event.setAmount(event.getAmount() * stats.vulnerabilities.meleeDamageMultiplierWhenNotSitting);
    }

    private static void rangedDamageMultiplier(LivingHurtEvent event, EnderDragon dragon, DragonDefinition stats) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile))
            return;
        event.setAmount(event.getAmount() * stats.vulnerabilities.rangedDamageMultiplier);
    }

    private static void explosionDamageMultiplier(LivingHurtEvent event, EnderDragon dragon, DragonDefinition stats) {
        if (!(event.getSource().is(DamageTypeTags.IS_EXPLOSION) && !event.getSource().is(DamageTypes.FIREWORKS)))
            return;
        event.setAmount(event.getAmount() * stats.vulnerabilities.explosionDamageMultiplier);
    }
}
