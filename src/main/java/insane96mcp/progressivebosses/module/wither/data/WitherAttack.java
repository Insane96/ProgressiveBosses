package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import insane96mcp.progressivebosses.data.Difficulty;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class WitherAttack {
    @SerializedName("skull_damage")
    public float skullDamage = 8f;
    @SerializedName("skull_speed_multiplier")
    public float skullSpeedMultiplier = 1f;
    @SerializedName("dangerous_skull_chance")
    public PoweredValue dangerousSkullChance = PoweredValue.of(0.05f);
    @SerializedName("attack_speed_near")
    public int attackSpeedNear = 40;
    @SerializedName("attack_speed_far")
    public int attackSpeedFar = 40;
    @SerializedName("side_heads_attack_speed_multiplier")
    public float sideHeadsAttackSpeedMultiplier = 1f;
    @SerializedName("effect_amplifier")
    public int effectAmplifier = 0;
    @SerializedName("effect_duration")
    public Difficulty effectDuration = Difficulty.of(10, 10, 20);
    @SerializedName("attack_to_heal_threshold")
    public float attackToHealThreshold;
    @SerializedName("heal_on_skull_kill")
    public float healOnSkullKill = 5;
    @SerializedName("charge")
    @Nullable
    public WitherCharge charge;
    @SerializedName("barrage")
    @Nullable
    public WitherBarrage barrage;

    /*private WitherAttack(float skullDamage, float skullSpeedMultiplier, PoweredValue dangerousSkullChance, int attackSpeedNear, int attackSpeedFar, float sideHeadsAttackSpeedMultiplier, int effectAmplifier, Difficulty effectDuration, float attackToHealThreshold, float healOnSkullKill, @Nullable WitherCharge charge, @Nullable WitherBarrage barrage) {
        this.skullDamage = skullDamage;
        this.skullSpeedMultiplier = skullSpeedMultiplier;
        this.dangerousSkullChance = dangerousSkullChance;
        this.attackSpeedNear = attackSpeedNear;
        this.attackSpeedFar = attackSpeedFar;
        this.sideHeadsAttackSpeedMultiplier = sideHeadsAttackSpeedMultiplier;
        this.effectAmplifier = effectAmplifier;
        this.effectDuration = effectDuration;
        this.attackToHealThreshold = attackToHealThreshold;
        this.healOnSkullKill = healOnSkullKill;
        this.charge = charge;
        this.barrage = barrage;
    }*/

    public static class Builder {
        private WitherAttack instance = new WitherAttack();

        public Builder skullDamage(float skullDamage) {
            instance.skullDamage = skullDamage;
            return this;
        }

        public Builder skullSpeedMultiplier(float skullSpeedMultiplier) {
            instance.skullSpeedMultiplier = skullSpeedMultiplier;
            return this;
        }

        public Builder dangerousSkullChance(PoweredValue dangerousSkullChance) {
            instance.dangerousSkullChance = dangerousSkullChance;
            return this;
        }

        public Builder attackSpeedNear(int attackSpeedNear) {
            instance.attackSpeedNear = attackSpeedNear;
            return this;
        }

        public Builder attackSpeedFar(int attackSpeedFar) {
            instance.attackSpeedFar = attackSpeedFar;
            return this;
        }

        public Builder sideHeadsAttackSpeedMultiplier(float sideHeadsAttackSpeedMultiplier) {
            instance.sideHeadsAttackSpeedMultiplier = sideHeadsAttackSpeedMultiplier;
            return this;
        }

        public Builder effectAmplifier(int effectAmplifier) {
            instance.effectAmplifier = effectAmplifier;
            return this;
        }

        public Builder effectDuration(Difficulty effectDuration) {
            instance.effectDuration = effectDuration;
            return this;
        }

        public Builder attackToHealThreshold(float attackToHealThreshold) {
            instance.attackToHealThreshold = attackToHealThreshold;
            return this;
        }

        public Builder healOnSkullKill(float healOnSkullKill) {
            instance.healOnSkullKill = healOnSkullKill;
            return this;
        }

        public Builder charge(@Nullable WitherCharge charge) {
            instance.charge = charge;
            return this;
        }

        public Builder barrage(@Nullable WitherBarrage barrage) {
            instance.barrage = barrage;
            return this;
        }

        public WitherAttack build() {
            return instance;
        }
    }

    public static class WitherCharge {
        @SerializedName("on_hit")
        public OnHit onHit;
        @SerializedName("second_phase")
        public SecondPhase secondPhase;
        @SerializedName("target_unseen")
        public TargetUnseen targetUnseen;

        public static abstract class BaseCharge {
            @SerializedName("damage")
            public float damage;
            @SerializedName("time_to_charge")
            public int timeToCharge;
        }

        @JsonAdapter(OnHit.Serializer.class)
        public static class OnHit extends BaseCharge {
            @SerializedName("chance")
            public PoweredValue chance;

            public static class Serializer implements JsonSerializer<OnHit>, JsonDeserializer<OnHit> {
                @Override
                public OnHit deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject jObject = json.getAsJsonObject();
                    OnHit onHit = new OnHit();
                    onHit.chance = context.deserialize(jObject.get("chance"), PoweredValue.class);
                    onHit.damage = GsonHelper.getAsFloat(jObject, "damage");
                    onHit.timeToCharge = GsonHelper.getAsInt(jObject, "time_to_charge");
                    return onHit;
                }

                @Override
                public JsonElement serialize(OnHit src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.add("chance", context.serialize(src.chance));
                    jsonObject.addProperty("damage", src.damage);
                    jsonObject.addProperty("time_to_charge", src.timeToCharge);
                    return jsonObject;
                }
            }
        }

        @JsonAdapter(SecondPhase.Serializer.class)
        public static class SecondPhase extends BaseCharge {
            @SerializedName("times")
            public int times;
            @SerializedName("tick_reduction")
            public int tickReduction;
            @SerializedName("max_reduction")
            public int maxReduction;
            @SerializedName("barrage")
            public boolean barrage;
            @SerializedName("minion")
            public boolean minion;

            public static class Serializer implements JsonSerializer<SecondPhase>, JsonDeserializer<SecondPhase> {
                @Override
                public SecondPhase deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject jObject = json.getAsJsonObject();
                    SecondPhase secondPhase = new SecondPhase();
                    secondPhase.times = GsonHelper.getAsInt(jObject, "times");
                    secondPhase.tickReduction = GsonHelper.getAsInt(jObject, "tick_reduction", 0);
                    secondPhase.maxReduction = GsonHelper.getAsInt(jObject, "max_reduction", Integer.MAX_VALUE);
                    secondPhase.barrage = GsonHelper.getAsBoolean(jObject, "barrage", false);
                    secondPhase.minion = GsonHelper.getAsBoolean(jObject, "minion", false);
                    secondPhase.damage = GsonHelper.getAsFloat(jObject, "damage");
                    secondPhase.timeToCharge = GsonHelper.getAsInt(jObject, "time_to_charge");
                    return secondPhase;
                }

                @Override
                public JsonElement serialize(SecondPhase src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("times", src.times);
                    jsonObject.addProperty("tick_reduction", src.tickReduction);
                    jsonObject.addProperty("max_reduction", src.maxReduction);
                    jsonObject.addProperty("barrage", src.barrage);
                    jsonObject.addProperty("minion", src.minion);
                    jsonObject.addProperty("damage", src.damage);
                    jsonObject.addProperty("time_to_charge", src.timeToCharge);
                    return jsonObject;
                }
            }
        }

        @JsonAdapter(TargetUnseen.Serializer.class)
        public static class TargetUnseen extends BaseCharge {
            @SerializedName("seconds_unseen")
            public int secondsUnseen;
            @SerializedName("chance_per_second")
            public float chancePerSecond;
            @SerializedName("max_chance")
            public float maxChance;

            public static class Serializer implements JsonSerializer<TargetUnseen>, JsonDeserializer<TargetUnseen> {
                @Override
                public TargetUnseen deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                    JsonObject jObject = json.getAsJsonObject();
                    TargetUnseen targetUnseen = new TargetUnseen();
                    targetUnseen.secondsUnseen = GsonHelper.getAsInt(jObject, "seconds_unseen");
                    targetUnseen.chancePerSecond = GsonHelper.getAsFloat(jObject, "chance_per_second");
                    targetUnseen.maxChance = GsonHelper.getAsFloat(jObject, "max_chance", 1f);
                    targetUnseen.damage = GsonHelper.getAsFloat(jObject, "damage");
                    targetUnseen.timeToCharge = GsonHelper.getAsInt(jObject, "time_to_charge");
                    return targetUnseen;
                }

                @Override
                public JsonElement serialize(TargetUnseen src, Type typeOfSrc, JsonSerializationContext context) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("seconds_unseen", src.secondsUnseen);
                    jsonObject.addProperty("chance_per_second", src.chancePerSecond);
                    jsonObject.addProperty("max_chance", src.maxChance);
                    jsonObject.addProperty("damage", src.damage);
                    jsonObject.addProperty("time_to_charge", src.timeToCharge);
                    return jsonObject;
                }
            }
        }

        public static float getDamage(PBWither wither) {
            return wither.chargeType.getDamage(wither);
        }

        public static int getTimeToCharge(PBWither wither) {
            return wither.chargeType.getTimeToCharge(wither);
        }
    }

    public static class WitherBarrage {
        @SerializedName("chance_on_hit")
        public PoweredValue chanceOnHit = PoweredValue.of(0.05f);
        @SerializedName("min_duration")
        public int minDuration = 40;
        @SerializedName("max_duration")
        public int maxDuration = 60;
        @SerializedName("attack_speed")
        public int attackSpeed = 5;
        @SerializedName("attack_cooldown_on_end")
        public int attackCooldownOnEnd = 0;
        @SerializedName("inaccuracy")
        public PoweredValue inaccuracy = PoweredValue.ZERO;
    }
}
