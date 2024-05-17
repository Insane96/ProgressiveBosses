package insane96mcp.progressivebosses.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import insane96mcp.progressivebosses.module.wither.data.PoweredValue;

import java.lang.reflect.Type;

@JsonAdapter(Resistances.Serializer.class)
public class Resistances {
    public PoweredValue armor;
    public PoweredValue toughness;

    public Resistances(PoweredValue armor, PoweredValue toughness) {
        this.armor = armor;
        this.toughness = toughness;
    }

    public static class Serializer implements JsonSerializer<Resistances>, JsonDeserializer<Resistances> {
        @Override
        public Resistances deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Resistances(context.deserialize(json.getAsJsonObject().get("armor"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("toughness"), PoweredValue.class));
        }

        @Override
        public JsonElement serialize(Resistances src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("armor", context.serialize(src.armor));
            jsonObject.add("toughness", context.serialize(src.toughness));
            return jsonObject;
        }
    }
}
