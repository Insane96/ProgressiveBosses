package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherMiscStats.Serializer.class)
public class WitherMiscStats {
    public float explosionPower;
    public boolean explosionCausesFire;
    public boolean ignoreWitherProofBlocks;
    public boolean netherOnly;

    public WitherMiscStats(float explosionPower, boolean explosionCausesFire, boolean ignoreWitherProofBlocks, boolean netherOnly) {
        this.explosionPower = explosionPower;
        this.explosionCausesFire = explosionCausesFire;
        this.ignoreWitherProofBlocks = ignoreWitherProofBlocks;
        this.netherOnly = netherOnly;
    }

    public static class Serializer implements JsonSerializer<WitherMiscStats>, JsonDeserializer<WitherMiscStats> {
        @Override
        public WitherMiscStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherMiscStats(GsonHelper.getAsFloat(json.getAsJsonObject(), "explosion_power"),
                    GsonHelper.getAsBoolean(json.getAsJsonObject(), "explosion_causes_fire"),
                    GsonHelper.getAsBoolean(json.getAsJsonObject(), "ignore_wither_proof_blocks"),
                    GsonHelper.getAsBoolean(json.getAsJsonObject(), "nether_only"));
        }

        @Override
        public JsonElement serialize(WitherMiscStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("explosion_power", src.explosionPower);
            jsonObject.addProperty("explosion_causes_fire", src.explosionCausesFire);
            jsonObject.addProperty("ignore_wither_proof_blocks", src.ignoreWitherProofBlocks);
            jsonObject.addProperty("nether_only", src.netherOnly);
            return jsonObject;
        }
    }
}
