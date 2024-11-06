package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(DragonMinion.Serializer.class)
public class DragonMinion {
    public float health;
    public int spawned;
    public int minCooldown;
    public int maxCooldown;
    public float blindingChance;
    public int blindingDuration;

    public DragonMinion(float health, int spawned, int minCooldown, int maxCooldown, float blindingChance, int blindingDuration) {
        this.health = health;
        this.spawned = spawned;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
        this.blindingChance = blindingChance;
        this.blindingDuration = blindingDuration;
    }

    public static class Serializer implements JsonSerializer<DragonMinion>, JsonDeserializer<DragonMinion> {
        @Override
        public DragonMinion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonMinion(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "spawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "min_cooldown"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "max_cooldown"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "blinding_chance"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "blinding_duration"));
        }

        @Override
        public JsonElement serialize(DragonMinion src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("spawned", src.spawned);
            jsonObject.addProperty("min_cooldown", src.minCooldown);
            jsonObject.addProperty("max_cooldown", src.maxCooldown);
            jsonObject.addProperty("blinding_chance", src.blindingChance);
            jsonObject.addProperty("blinding_duration", src.blindingDuration);
            return jsonObject;
        }
    }
}
