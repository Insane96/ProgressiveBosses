package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(DragonHealth.Serializer.class)
public class DragonHealth {
    public float health;
    public float regeneration;
    public float crystalRegeneration;
    public float regenWhenHitRatio;
    public int regenWhenHitDuration;

    public DragonHealth(float health, float regeneration, float crystalRegeneration, float regenWhenHitRatio, int regenWhenHitDuration) {
        this.health = health;
        this.regeneration = regeneration;
        this.crystalRegeneration = crystalRegeneration;
        this.regenWhenHitRatio = regenWhenHitRatio;
        this.regenWhenHitDuration = regenWhenHitDuration;
    }

    public static class Serializer implements JsonSerializer<DragonHealth>, JsonDeserializer<DragonHealth> {
        @Override
        public DragonHealth deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonHealth(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "crystal_regeneration"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "regen_when_hit_ratio"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "regen_when_hit_duration"));
        }

        @Override
        public JsonElement serialize(DragonHealth src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("regeneration", src.regeneration);
            jsonObject.addProperty("crystal_regeneration", src.crystalRegeneration);
            jsonObject.addProperty("regen_when_hit_ratio", src.regenWhenHitRatio);
            jsonObject.addProperty("regen_when_hit_duration", src.regenWhenHitDuration);
            return jsonObject;
        }
    }
}
