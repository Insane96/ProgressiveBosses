package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(DragonCrystal.Serializer.class)
public class DragonCrystal {
    public int cages;
    public int bonusCrystals;
    public int crystalsRespawned;

    public DragonCrystal(int cages, int bonusCrystals, int crystalsRespawned) {
        this.cages = cages;
        this.bonusCrystals = bonusCrystals;
        this.crystalsRespawned = crystalsRespawned;
    }

    public static class Serializer implements JsonSerializer<DragonCrystal>, JsonDeserializer<DragonCrystal> {
        @Override
        public DragonCrystal deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new DragonCrystal(GsonHelper.getAsInt(json.getAsJsonObject(), "cages"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "bonus_crystals"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "crystals_respawned"));
        }

        @Override
        public JsonElement serialize(DragonCrystal src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cages", src.cages);
            jsonObject.addProperty("bonus_crystals", src.bonusCrystals);
            jsonObject.addProperty("crystals_respawned", src.crystalsRespawned);
            return jsonObject;
        }
    }
}
