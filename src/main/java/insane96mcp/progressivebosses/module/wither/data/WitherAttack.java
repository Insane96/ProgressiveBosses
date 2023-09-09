package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherAttack.Serializer.class)
public class WitherAttack {
    public float skullDamage;
    public float skullSpeedMultiplier;
    public float dangerousSkullChance;
    public int attackSpeedNear;
    public int attackSpeedFar;
    //TODO move charge and barrage to to its own class so it can be null
    public float maxChargeChance;
    public float chargeDamage;
    public int chargeTime;
    public float barrageChance;
    public int minBarrageDuration;
    public int maxBarrageDuration;

    public WitherAttack(float skullDamage, float skullSpeedMultiplier, float dangerousSkullChance, int attackSpeedNear, int attackSpeedFar, float maxChargeChance, float chargeDamage, int chargeTime, float barrageChance, int minBarrageDuration, int maxBarrageDuration) {
        this.skullDamage = skullDamage;
        this.skullSpeedMultiplier = skullSpeedMultiplier;
        this.dangerousSkullChance = dangerousSkullChance;
        this.attackSpeedNear = attackSpeedNear;
        this.attackSpeedFar = attackSpeedFar;
        this.maxChargeChance = maxChargeChance;
        this.chargeDamage = chargeDamage;
        this.chargeTime = chargeTime;
        this.barrageChance = barrageChance;
        this.minBarrageDuration = minBarrageDuration;
        this.maxBarrageDuration = maxBarrageDuration;
    }

    public static class Serializer implements JsonSerializer<WitherAttack>, JsonDeserializer<WitherAttack> {
        @Override
        public WitherAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherAttack(GsonHelper.getAsFloat(json.getAsJsonObject(), "skull_damage"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "skull_speed_multiplier"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "dangerous_skull_damage"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "attack_speed_near"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "attack_speed_far"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "max_charge_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "charge_damage"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "charge_time"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "barrage_chance"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "min_barrage_duration"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "max_barrage_duration"));
        }

        @Override
        public JsonElement serialize(WitherAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("skull_damage", src.skullDamage);
            jsonObject.addProperty("skull_speed_multiplier", src.skullSpeedMultiplier);
            jsonObject.addProperty("dangerous_skull_damage", src.dangerousSkullChance);
            jsonObject.addProperty("attack_speed_near", src.attackSpeedNear);
            jsonObject.addProperty("attack_speed_far", src.attackSpeedFar);
            jsonObject.addProperty("max_charge_chance", src.maxChargeChance);
            jsonObject.addProperty("charge_damage", src.chargeDamage);
            jsonObject.addProperty("charge_time", src.chargeTime);
            jsonObject.addProperty("barrage_chance", src.barrageChance);
            jsonObject.addProperty("min_barrage_duration", src.minBarrageDuration);
            jsonObject.addProperty("max_barrage_duration", src.maxBarrageDuration);
            return jsonObject;
        }
    }
}
