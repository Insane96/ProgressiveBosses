package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(PoweredValue.Serializer.class)
public class PoweredValue {
    public float aboveHalfHealth;
    public float belowHalfHealth;

    public PoweredValue(float aboveHalfHealth, float belowHalfHealth) {
        this.aboveHalfHealth = aboveHalfHealth;
        this.belowHalfHealth = belowHalfHealth;
    }

    public PoweredValue(float value) {
        this.aboveHalfHealth = value;
        this.belowHalfHealth = value;
    }

    public float getValue(PBWither wither) {
        return wither.isPowered() && wither.getInvulnerableTicks() == 0 ? this.belowHalfHealth : this.aboveHalfHealth;
    }

    public int getIntValue(PBWither wither) {
        return (int) (wither.isPowered() ? this.belowHalfHealth : this.aboveHalfHealth);
    }

    public float getValue(boolean isPowered) {
        return isPowered ? this.belowHalfHealth : this.aboveHalfHealth;
    }

    public int getIntValue(boolean isPowered) {
        return (int) (isPowered ? this.belowHalfHealth : this.aboveHalfHealth);
    }

    public static class Serializer implements JsonSerializer<PoweredValue>, JsonDeserializer<PoweredValue> {
        @Override
        public PoweredValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive())
                return new PoweredValue(json.getAsFloat());
            return new PoweredValue(GsonHelper.getAsFloat(json.getAsJsonObject(), "above_half_health"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "below_half_health"));
        }

        @Override
        public JsonElement serialize(PoweredValue src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.aboveHalfHealth == src.belowHalfHealth)
                return new JsonPrimitive(src.aboveHalfHealth);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("above_half_health", src.aboveHalfHealth);
            jsonObject.addProperty("below_half_health", src.belowHalfHealth);
            return jsonObject;
        }
    }
}
