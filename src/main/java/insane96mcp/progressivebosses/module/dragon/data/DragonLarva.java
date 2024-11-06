package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(DragonLarva.Serializer.class)
public class DragonLarva {
    public float health;
    public int spawned;
    public int minCooldown;
    public int maxCooldown;

    public DragonLarva(float health, int spawned, int minCooldown, int maxCooldown) {
        this.health = health;
        this.spawned = spawned;
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    public static class Serializer implements JsonSerializer<DragonLarva>, JsonDeserializer<DragonLarva> {
        @Override
        public DragonLarva deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonLarva(GsonHelper.getAsFloat(json.getAsJsonObject(), "health"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "spawned"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "min_cooldown"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "max_cooldown"));
        }

        @Override
        public JsonElement serialize(DragonLarva src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("health", src.health);
            jsonObject.addProperty("spawned", src.spawned);
            jsonObject.addProperty("min_cooldown", src.minCooldown);
            jsonObject.addProperty("max_cooldown", src.maxCooldown);
            return jsonObject;
        }
    }
}
