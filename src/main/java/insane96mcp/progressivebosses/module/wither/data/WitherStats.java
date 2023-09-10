package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.progressivebosses.module.wither.WitherFeature;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(WitherStats.Serializer.class)
public class WitherStats {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/wither");

    public int level;
    public WitherAttack attack;
    public WitherHealth health;
    @Nullable
    public WitherResistancesWeaknesses resistancesWeaknesses;
    @Nullable
    public WitherMinionStats minion;
    public WitherMiscStats misc;
    public int xpDropped;
    public ResourceLocation lootTable;

    public WitherStats(int level, WitherAttack attack, WitherHealth health, @Nullable WitherResistancesWeaknesses resistancesWeaknesses, @Nullable WitherMinionStats minion, WitherMiscStats misc, int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.attack = attack;
        this.health = health;
        this.resistancesWeaknesses = resistancesWeaknesses;
        this.minion = minion;
        this.misc = misc;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    public void apply(PBWither wither) {
        wither.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.health.health);
        if (this.resistancesWeaknesses != null) {
            wither.getAttribute(Attributes.ARMOR).setBaseValue(this.resistancesWeaknesses.armor.getValue(wither));
            wither.getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(this.resistancesWeaknesses.toughness.getValue(wither));
        }
        if (this.minion != null) {
            this.minion.setCooldown(wither, 2f);
        }
        wither.lootTable = this.lootTable;
        wither.xpReward = this.xpDropped;
    }

    public void finalizeSpawn(PBWither wither) {
        wither.setHealth(wither.getMaxHealth());
    }

    public static WitherStats getDefaultStats() {
        return WitherFeature.DEFAULT_WITHER_STATS.get(0);
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<WitherStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<WitherStats>, JsonDeserializer<WitherStats> {
        @Override
        public WitherStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String sLootTable = GsonHelper.getAsString(json.getAsJsonObject(), "loot_table", VANILLA_LOOT_TABLE.getPath());
            ResourceLocation lootTable = ResourceLocation.tryParse(sLootTable);
            WitherResistancesWeaknesses witherResistancesWeaknesses = json.getAsJsonObject().has("resistances_weaknesses") ? context.deserialize(json.getAsJsonObject().get("resistances_weaknesses"), WitherResistancesWeaknesses.class) : null;
            WitherMinionStats witherMinionStats = json.getAsJsonObject().has("minion") ? context.deserialize(json.getAsJsonObject().get("minion"), WitherMinionStats.class) : null;
            return new WitherStats(GsonHelper.getAsInt(json.getAsJsonObject(), "level"),
                    context.deserialize(json.getAsJsonObject().get("attack"), WitherAttack.class),
                    context.deserialize(json.getAsJsonObject().get("health"), WitherHealth.class),
                    witherResistancesWeaknesses,
                    witherMinionStats,
                    context.deserialize(json.getAsJsonObject().get("misc"), WitherMiscStats.class),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "xp_dropped"),
                    lootTable);
        }

        @Override
        public JsonElement serialize(WitherStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("level", src.level);
            jsonObject.add("attack", context.serialize(src.attack));
            jsonObject.add("health", context.serialize(src.health));
            if (src.resistancesWeaknesses != null)
                jsonObject.add("resistances_weaknesses", context.serialize(src.resistancesWeaknesses));
            if (src.minion != null)
                jsonObject.add("minion", context.serialize(src.minion));
            jsonObject.add("misc", context.serialize(src.misc));
            jsonObject.addProperty("xp_dropped", src.xpDropped);
            if (!src.lootTable.equals(VANILLA_LOOT_TABLE))
                jsonObject.addProperty("loot_table", src.lootTable.toString());
            return jsonObject;
        }
    }
}
