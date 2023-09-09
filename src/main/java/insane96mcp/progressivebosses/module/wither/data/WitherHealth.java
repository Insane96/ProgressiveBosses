package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherHealth.Serializer.class)
public class WitherHealth {
    public float health;
    public float regeneration;
    public float regenWhenHit;
    public int regenWhenHitDuration;

    public WitherHealth(float health, float regeneration, float regenWhenHit, int regenWhenHitDuration) {
        this.health = health;
        this.regeneration = regeneration;
        this.regenWhenHit = regenWhenHit;
        this.regenWhenHitDuration = regenWhenHitDuration;
    }

    public static class Serializer implements JsonSerializer<WitherHealth>, JsonDeserializer<WitherHealth> {
        @Override
        public WitherHealth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherHealth(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regen_when_hit"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "regen_when_hit_duration"));
        }

        @Override
        public JsonElement serialize(WitherHealth src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("regeneration", src.regeneration);
            jsonObject.addProperty("regen_when_hit", src.regenWhenHit);
            jsonObject.addProperty("regen_when_hit_duration", src.regenWhenHitDuration);
            return jsonObject;
        }
    }
}
