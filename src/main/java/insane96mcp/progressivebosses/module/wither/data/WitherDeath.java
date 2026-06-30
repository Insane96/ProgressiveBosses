package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherDeath.Serializer.class)
public class WitherDeath {
    public float explosionPower;
    public boolean explosionCausesFire;


    public WitherDeath(float explosionPower, boolean explosionCausesFire) {
        this.explosionPower = explosionPower;
        this.explosionCausesFire = explosionCausesFire;
    }

    public static class Serializer implements JsonSerializer<WitherDeath>, JsonDeserializer<WitherDeath> {
        @Override
        public WitherDeath deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherDeath(GsonHelper.getAsFloat(json.getAsJsonObject(), "explosion_power"),
                    GsonHelper.getAsBoolean(json.getAsJsonObject(), "explosion_causes_fire"));
        }

        @Override
        public JsonElement serialize(WitherDeath src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("explosion_power", src.explosionPower);
            jsonObject.addProperty("explosion_causes_fire", src.explosionCausesFire);
            return jsonObject;
        }
    }
}
