package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(DragonVulnerabilities.Serializer.class)
public class DragonVulnerabilities {
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
}
