package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(PoweredValue.Serializer.class)
public class PoweredValue {
    public static final PoweredValue ZERO = new PoweredValue(0f);

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

    public static PoweredValue of(float value) {
        return new PoweredValue(value);
    }

    public static PoweredValue of(float aboveHalfHealth, float belowHalfHealth) {
        return new PoweredValue(aboveHalfHealth, belowHalfHealth);
    }

    public float getValue(PBWither wither) {
        return getValue(wither.isPowered() && wither.getInvulnerableTicks() == 0);
    }

    public int getIntValue(PBWither wither) {
        return (int) getValue(wither);
    }

    public float getValue(boolean isPowered) {
        return isPowered ? this.belowHalfHealth : this.aboveHalfHealth;
    }

    public int getIntValue(boolean isPowered) {
        return (int) (getValue(isPowered));
    }

    public boolean isZero() {
        return this == ZERO || (this.aboveHalfHealth == 0f && this.belowHalfHealth == 0f);
    }

    public static class Serializer implements JsonSerializer<PoweredValue>, JsonDeserializer<PoweredValue> {
        @Override
        public PoweredValue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json.isJsonPrimitive())
                return new PoweredValue(json.getAsFloat());
            return PoweredValue.of(GsonHelper.getAsFloat(json.getAsJsonObject(), "above_half_health"),
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
