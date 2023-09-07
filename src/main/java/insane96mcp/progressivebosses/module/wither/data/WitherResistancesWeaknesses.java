package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherResistancesWeaknesses.Serializer.class)
public class WitherResistancesWeaknesses {
    //TODO Change to armor
    public float meleeDamageReduction;
    public float shieldedMeleeDamageReduction;
    public float doubleMagicDamageEveryThisMissingHealth;

    public WitherResistancesWeaknesses(float meleeDamageReduction, float shieldedMeleeDamageReduction, float doubleMagicDamageEveryThisMissingHealth) {
        this.meleeDamageReduction = meleeDamageReduction;
        this.shieldedMeleeDamageReduction = shieldedMeleeDamageReduction;
        this.doubleMagicDamageEveryThisMissingHealth = doubleMagicDamageEveryThisMissingHealth;
    }

    public static class Serializer implements JsonSerializer<WitherResistancesWeaknesses>, JsonDeserializer<WitherResistancesWeaknesses> {
        @Override
        public WitherResistancesWeaknesses deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherResistancesWeaknesses(GsonHelper.getAsFloat(json.getAsJsonObject(), "melee_damage_reduction"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "shielded_melee_damage_reduction"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "double_magic_damage_every_this_missing_health"));
        }

        @Override
        public JsonElement serialize(WitherResistancesWeaknesses src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("melee_damage_reduction", src.meleeDamageReduction);
            jsonObject.addProperty("shielded_melee_damage_reduction", src.shieldedMeleeDamageReduction);
            jsonObject.addProperty("double_magic_damage_every_this_missing_health", src.doubleMagicDamageEveryThisMissingHealth);
            return jsonObject;
        }
    }
}
