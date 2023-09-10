package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

@JsonAdapter(WitherAttack.Serializer.class)
public class WitherAttack {
    public float skullDamage;
    public float skullSpeedMultiplier;
    public float dangerousSkullChance;
    public int attackSpeedNear;
    public int attackSpeedFar;
    @Nullable
    public WitherCharge charge;
    @Nullable
    public WitherBarrage barrage;

    public WitherAttack(float skullDamage, float skullSpeedMultiplier, float dangerousSkullChance, int attackSpeedNear, int attackSpeedFar, float maxChargeChance, float chargeDamage, int chargeTime, float barrageChance, int minBarrageDuration, int maxBarrageDuration) {
        this.skullDamage = skullDamage;
        this.skullSpeedMultiplier = skullSpeedMultiplier;
        this.dangerousSkullChance = dangerousSkullChance;
        this.attackSpeedNear = attackSpeedNear;
        this.attackSpeedFar = attackSpeedFar;
        this.charge = new WitherCharge(maxChargeChance, chargeDamage, chargeTime);
        this.barrage = new WitherBarrage(barrageChance, minBarrageDuration, maxBarrageDuration);
    }

    public static class Serializer implements JsonSerializer<WitherAttack>, JsonDeserializer<WitherAttack> {
        @Override
        public WitherAttack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            WitherCharge witherCharge = jObject.has("charge") ? context.deserialize(jObject.get("charge"), WitherCharge.class) : null;
            WitherBarrage witherBarrage = jObject.has("barrage") ? context.deserialize(jObject.get("barrage"), WitherBarrage.class) : null;
            WitherAttack witherAttack = new WitherAttack(GsonHelper.getAsFloat(jObject, "skull_damage"),
                    GsonHelper.getAsFloat(jObject, "skull_speed_multiplier"),
                    GsonHelper.getAsFloat(jObject, "dangerous_skull_damage"),
                    GsonHelper.getAsInt(jObject, "attack_speed_near"),
                    GsonHelper.getAsInt(jObject, "attack_speed_far"), 0, 0, 0, 0, 0, 0);
            witherAttack.charge = witherCharge;
            witherAttack.barrage = witherBarrage;
            return witherAttack;
        }

        @Override
        public JsonElement serialize(WitherAttack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("skull_damage", src.skullDamage);
            jsonObject.addProperty("skull_speed_multiplier", src.skullSpeedMultiplier);
            jsonObject.addProperty("dangerous_skull_damage", src.dangerousSkullChance);
            jsonObject.addProperty("attack_speed_near", src.attackSpeedNear);
            jsonObject.addProperty("attack_speed_far", src.attackSpeedFar);
            if (src.charge != null)
                jsonObject.add("charge", context.serialize(src.charge));
            if (src.barrage != null)
                jsonObject.add("barrage", context.serialize(src.barrage));
            return jsonObject;
        }
    }

    public static class WitherCharge {
        @SerializedName("max_chance")
        public float maxChance;
        @SerializedName("damage")
        public float damage;
        @SerializedName("time")
        public int time;

        public WitherCharge(float maxChance, float damage, int time) {
            this.maxChance = maxChance;
            this.damage = damage;
            this.time = time;
        }
    }

    public static class WitherBarrage {
        @SerializedName("chance")
        public float chance;
        @SerializedName("min_duration")
        public int minDuration;
        @SerializedName("max_duration")
        public int maxDuration;

        public WitherBarrage(float chance, int minDuration, int maxDuration) {
            this.chance = chance;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }
    }
}
