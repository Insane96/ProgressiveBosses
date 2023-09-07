package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.module.wither.feature.DifficultyFeature;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(WitherStats.Serializer.class)
public class WitherStats {
    public int level;
    public WitherAttackStats attackStats;
    public WitherHealthStats healthStats;
    public WitherResistancesWeaknesses resistancesWeaknesses;
    public WitherMiscStats miscStats;

    public WitherStats(int level, WitherAttackStats attackStats, WitherHealthStats healthStats, WitherResistancesWeaknesses resistancesWeaknesses, WitherMiscStats miscStats) {
        this.level = level;
        this.attackStats = attackStats;
        this.healthStats = healthStats;
        this.resistancesWeaknesses = resistancesWeaknesses;
        this.miscStats = miscStats;
    }

    public void apply(PBWither wither) {
        wither.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.healthStats.health);
    }

    public void finalizeSpawn(PBWither wither) {
        wither.setHealth(wither.getMaxHealth());
    }

    public static WitherStats getDefaultStats() {
        return DifficultyFeature.DEFAULT_WITHER_STATS.get(0);
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<WitherStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<WitherStats>, JsonDeserializer<WitherStats> {
        @Override
        public WitherStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherStats(GsonHelper.getAsInt(json.getAsJsonObject(), "level"),
                    context.deserialize(json.getAsJsonObject().get("attack_stats"), WitherAttackStats.class),
                    context.deserialize(json.getAsJsonObject().get("health_stats"), WitherHealthStats.class),
                    context.deserialize(json.getAsJsonObject().get("resistances_weaknesses"), WitherResistancesWeaknesses.class),
                    context.deserialize(json.getAsJsonObject().get("misc_stats"), WitherMiscStats.class));
        }

        @Override
        public JsonElement serialize(WitherStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("level", src.level);
            jsonObject.add("attack_stats", context.serialize(src.attackStats, WitherAttackStats.class));
            jsonObject.add("health_stats", context.serialize(src.healthStats, WitherHealthStats.class));
            jsonObject.add("resistances_weaknesses", context.serialize(src.resistancesWeaknesses, WitherResistancesWeaknesses.class));
            jsonObject.add("misc_stats", context.serialize(src.miscStats, WitherMiscStats.class));
            return jsonObject;
        }
    }
}
