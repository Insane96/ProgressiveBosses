package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.lang.reflect.Type;

@JsonAdapter(DragonValue.Serializer.class)
public class DragonValue {
    public float base;
    public float angered;

    public DragonValue(float value) {
        this.base = value;
        this.angered = value;
    }

    public DragonValue(float base, float angered) {
        this.base = base;
        this.angered = angered;
    }

    public float getValue(EnderDragon dragon) {
        if (this.base == this.angered)
            return this.base;
        return DragonAnger.isAngered(dragon) ? this.angered : this.base;
    }

    public int getIntValue(EnderDragon dragon) {
        return (int) getValue(dragon);
    }

    public static class Serializer implements JsonSerializer<DragonValue>, JsonDeserializer<DragonValue> {
        @Override
        public DragonValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive())
                return new DragonValue(json.getAsFloat(), json.getAsFloat());
            JsonObject jsonObject = json.getAsJsonObject();
            float base = GsonHelper.getAsFloat(jsonObject, "base");
            float withNoCrystalsLeft = GsonHelper.getAsFloat(jsonObject, "angered");
            return new DragonValue(base, withNoCrystalsLeft);
        }

        @Override
        public JsonElement serialize(DragonValue src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.base == src.angered)
                return new JsonPrimitive(src.base);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("base", src.base);
            jsonObject.addProperty("angered", src.angered);
            return jsonObject;
        }
    }
}
