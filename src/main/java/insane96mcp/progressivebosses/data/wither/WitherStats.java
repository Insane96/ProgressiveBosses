package insane96mcp.progressivebosses.data.wither;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;

@JsonAdapter(WitherStats.Serializer.class)
public class WitherStats {
    public int level;
    public WitherAttackStats attackStats;
    public WitherHealthStats healthStats;

    public WitherStats(int level, WitherAttackStats attackStats, WitherHealthStats healthStats) {
        this.level = level;
        this.attackStats = attackStats;
        this.healthStats = healthStats;
    }

    public CompoundTag toNbt(CompoundTag tag) {
        tag.putInt("level", this.level);
        tag.put("attack_stats", this.attackStats.toNbt(new CompoundTag()));
        tag.put("health_stats", this.healthStats.toNbt(new CompoundTag()));
        return tag;
    }

    public static WitherStats fromNbt(CompoundTag tag) {
        return new WitherStats(tag.getInt("level"), WitherAttackStats.fromNbt(tag.getCompound("attack_stats")), WitherHealthStats.fromNbt(tag.getCompound("health_stats")));
    }

    public static class Serializer implements JsonSerializer<WitherStats>, JsonDeserializer<WitherStats> {
        @Override
        public WitherStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new WitherStats(GsonHelper.getAsInt(json.getAsJsonObject(), "level"), context.deserialize(json.getAsJsonObject().get("attack_stats"), WitherAttackStats.class), context.deserialize(json.getAsJsonObject().get("health_stats"), WitherHealthStats.class));
        }

        @Override
        public JsonElement serialize(WitherStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("level", src.level);
            jsonObject.add("attack_stats", context.serialize(src.attackStats, WitherAttackStats.class));
            jsonObject.add("health_stats", context.serialize(src.healthStats, WitherHealthStats.class));
            return jsonObject;
        }
    }
}
