package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.insanelib.data.SerializableAttributeModifier;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@JsonAdapter(PoweredAttributeModifiers.Serializer.class)
public class PoweredAttributeModifiers {
    public List<SerializableAttributeModifier> aboveHalfHealth;
    public List<SerializableAttributeModifier> belowHalfHealth;

    public PoweredAttributeModifiers(List<SerializableAttributeModifier> aboveHalfHealth, List<SerializableAttributeModifier> belowHalfHealth) {
        this.aboveHalfHealth = aboveHalfHealth;
        this.belowHalfHealth = belowHalfHealth;
    }

    public List<SerializableAttributeModifier> getValue(PBWither wither) {
        return getValue(wither.isPowered() && wither.getInvulnerableTicks() == 0);
    }

    public List<SerializableAttributeModifier> getValue(boolean isPowered) {
        return isPowered ? this.belowHalfHealth : this.aboveHalfHealth;
    }

    public static class Serializer implements JsonSerializer<PoweredAttributeModifiers>, JsonDeserializer<PoweredAttributeModifiers> {
        @Override
        public PoweredAttributeModifiers deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            List<SerializableAttributeModifier> aboveHalfHealth = new ArrayList<>();
            List<SerializableAttributeModifier> belowHalfHealth = new ArrayList<>();

            if (jObject.has("above_half_health"))
                aboveHalfHealth.addAll(context.deserialize(jObject.get("above_half_health"), SerializableAttributeModifier.LIST_TYPE));
            if (jObject.has("below_half_health"))
                belowHalfHealth.addAll(context.deserialize(jObject.get("below_half_health"), SerializableAttributeModifier.LIST_TYPE));

            return new PoweredAttributeModifiers(aboveHalfHealth, belowHalfHealth);
        }

        @Override
        public JsonElement serialize(PoweredAttributeModifiers src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("above_half_health", context.serialize(src.aboveHalfHealth));
            jsonObject.add("below_half_health", context.serialize(src.belowHalfHealth));
            return jsonObject;
        }
    }
}
