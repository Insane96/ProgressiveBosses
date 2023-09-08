package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherResistancesWeaknesses.Serializer.class)
public class WitherResistancesWeaknesses {
    public PoweredValue armor;
    public PoweredValue toughness;
    public float doubleMagicDamageEveryThisMissingHealth;

    public WitherResistancesWeaknesses(PoweredValue armor, PoweredValue toughness, float doubleMagicDamageEveryThisMissingHealth) {
        this.armor = armor;
        this.toughness = toughness;
        this.doubleMagicDamageEveryThisMissingHealth = doubleMagicDamageEveryThisMissingHealth;
    }

    public static class Serializer implements JsonSerializer<WitherResistancesWeaknesses>, JsonDeserializer<WitherResistancesWeaknesses> {
        @Override
        public WitherResistancesWeaknesses deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherResistancesWeaknesses(context.deserialize(json.getAsJsonObject().get("armor"), PoweredValue.class),
                    context.deserialize(json.getAsJsonObject().get("toughness"), PoweredValue.class),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "double_magic_damage_every_this_missing_health"));
        }

        @Override
        public JsonElement serialize(WitherResistancesWeaknesses src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("armor", context.serialize(src.armor));
            jsonObject.add("toughness", context.serialize(src.toughness));
            jsonObject.addProperty("double_magic_damage_every_this_missing_health", src.doubleMagicDamageEveryThisMissingHealth);
            return jsonObject;
        }
    }
}
