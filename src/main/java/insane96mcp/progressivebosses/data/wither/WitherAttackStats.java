package insane96mcp.progressivebosses.data.wither;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherAttackStats.Serializer.class)
public class WitherAttackStats {
    public float skullDamage;
    public float skullSpeedMultiplier;
    public int attackSpeedNear;
    public int attackSpeedFar;
    public float maxChargeChance;
    public float chargeDamage;
    public float chargeTime;
    public float barrageChance;
    public int barrageDuration;

    public WitherAttackStats(float skullDamage, float skullSpeedMultiplier, int attackSpeedNear, int attackSpeedFar, float maxChargeChance, float chargeDamage, int chargeTime, float barrageChance, int barrageDuration) {
        this.skullDamage = skullDamage;
        this.skullSpeedMultiplier = skullSpeedMultiplier;
        this.attackSpeedNear = attackSpeedNear;
        this.attackSpeedFar = attackSpeedFar;
        this.maxChargeChance = maxChargeChance;
        this.chargeDamage = chargeDamage;
        this.chargeTime = chargeTime;
        this.barrageChance = barrageChance;
        this.barrageDuration = barrageDuration;
    }

    public CompoundTag toNbt(CompoundTag tag) {
        tag.putFloat("skull_damage", skullDamage);
        tag.putFloat("skull_speed_multiplier", skullSpeedMultiplier);
        tag.putInt("attack_speed_near", attackSpeedNear);
        tag.putInt( "attack_speed_far", attackSpeedFar);
        tag.putFloat( "max_charge_chance", maxChargeChance);
        tag.putFloat("charge_damage", chargeDamage);
        tag.putFloat("charge_time", chargeTime);
        tag.putFloat("barrage_chance", barrageChance);
        tag.putInt("barrage_duration", barrageDuration);
        return tag;
    }

    public static WitherAttackStats fromNbt(CompoundTag tag) {
        return new WitherAttackStats(tag.getFloat("skull_damage"),
                tag.getFloat("skull_speed_multiplier"),
                tag.getInt("attack_speed_near"),
                tag.getInt("attack_speed_far"),
                tag.getFloat("max_charge_chance"),
                tag.getFloat("charge_damage"),
                tag.getInt("charge_time"),
                tag.getFloat("barrage_chance"),
                tag.getInt("barrage_duration"));
    }

    public static class Serializer implements JsonSerializer<WitherAttackStats>, JsonDeserializer<WitherAttackStats> {
        @Override
        public WitherAttackStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherAttackStats(GsonHelper.getAsFloat(json.getAsJsonObject(), "skull_damage"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "skull_speed_multiplier"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "attack_speed_near"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "attack_speed_far"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "max_charge_chance"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "charge_damage"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "charge_time"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "barrage_chance"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "barrage_duration"));
        }

        @Override
        public JsonElement serialize(WitherAttackStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("skull_damage", src.skullDamage);
            jsonObject.addProperty("skull_speed_multiplier", src.skullSpeedMultiplier);
            jsonObject.addProperty("attack_speed_near", src.attackSpeedNear);
            jsonObject.addProperty("attack_speed_far", src.attackSpeedFar);
            jsonObject.addProperty("max_charge_chance", src.maxChargeChance);
            jsonObject.addProperty("charge_damage", src.chargeDamage);
            jsonObject.addProperty("charge_time", src.chargeTime);
            jsonObject.addProperty("barrage_chance", src.barrageChance);
            jsonObject.addProperty("barrage_duration", src.barrageDuration);
            return jsonObject;
        }
    }
}
