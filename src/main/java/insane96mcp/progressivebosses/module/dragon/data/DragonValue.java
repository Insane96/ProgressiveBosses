package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.lang.reflect.Type;

@JsonAdapter(DragonValue.Serializer.class)
public class DragonValue {
    public static final int BASE_CRYSTALS = 10;

    public float base;
    public float withNoCrystalsLeft;

    public DragonValue(float base, float withNoCrystalsLeft) {
        this.base = base;
        this.withNoCrystalsLeft = withNoCrystalsLeft;
    }

    public float getValue(EnderDragon dragon) {
        if (this.base == this.withNoCrystalsLeft)
            return this.base;
        if (dragon.getDragonFight() == null)
            return this.withNoCrystalsLeft;
        int crystalsLeft = dragon.getDragonFight().getCrystalsAlive();
        if (crystalsLeft <= 0)
            return this.withNoCrystalsLeft;
        else if (crystalsLeft >= BASE_CRYSTALS)
            return this.base;
        return this.base - (this.withNoCrystalsLeft - this.base) * ((float) crystalsLeft / BASE_CRYSTALS) + this.base;
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
            float withNoCrystalsLeft = GsonHelper.getAsFloat(jsonObject, "with_no_crystals_left");
            return new DragonValue(base, withNoCrystalsLeft);
        }

        @Override
        public JsonElement serialize(DragonValue src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.base == src.withNoCrystalsLeft)
                return new JsonPrimitive(src.base);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("base", src.base);
            jsonObject.addProperty("with_no_crystals_left", src.withNoCrystalsLeft);
            return jsonObject;
        }
    }
}
