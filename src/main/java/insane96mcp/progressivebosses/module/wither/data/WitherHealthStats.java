package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherHealthStats.Serializer.class)
public class WitherHealthStats {
    public float health;
    public float regeneration;
    public float regenWhenHit;

    public int regenWhenHitDuration;

    public WitherHealthStats(float health, float regeneration, float regenWhenHit, int regenWhenHitDuration) {
        this.health = health;
        this.regeneration = regeneration;
        this.regenWhenHit = regenWhenHit;
        this.regenWhenHitDuration = regenWhenHitDuration;
    }

    public static class Serializer implements JsonSerializer<WitherHealthStats>, JsonDeserializer<WitherHealthStats> {
        @Override
        public WitherHealthStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherHealthStats(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regen_when_hit"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "regen_when_hit_duration"));
        }

        @Override
        public JsonElement serialize(WitherHealthStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("regeneration", src.regeneration);
            jsonObject.addProperty("regen_when_hit", src.regenWhenHit);
            jsonObject.addProperty("regen_when_hit_duration", src.regenWhenHitDuration);
            return jsonObject;
        }
    }
}
