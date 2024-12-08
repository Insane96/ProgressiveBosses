package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(DragonStats.Serializer.class)
public class DragonStats {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/ender_dragon");

    public byte level;
    public float maxSittingDamageReceived;
    public int roarTime;
    public int sittingScanningIdleTime;
    public int sittingFlamingTime;
    public DragonHealth health;
    public DragonVulnerabilities vulnerabilities;
    public DragonCrystal crystal;
    @Nullable
    public DragonLarva larva;
    @Nullable
    public DragonMinion minion;
    public DragonAttack attack;
    public int xpDropped;
    public ResourceLocation lootTable;

    public DragonStats(byte level, float maxSittingDamageReceived, int roarTime, int sittingScanningIdleTime, int sittingFlamingTime, DragonHealth health, DragonVulnerabilities vulnerabilities, DragonCrystal crystal, @Nullable DragonLarva larva, @Nullable DragonMinion minion, DragonAttack attack, int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.maxSittingDamageReceived = maxSittingDamageReceived;
        this.roarTime = roarTime;
        this.sittingScanningIdleTime = sittingScanningIdleTime;
        this.sittingFlamingTime = sittingFlamingTime;
        this.health = health;
        this.vulnerabilities = vulnerabilities;
        this.crystal = crystal;
        this.larva = larva;
        this.minion = minion;
        this.attack = attack;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    public static void apply(EnderDragon dragon, DragonStats stats) {
        dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.health.health);
        dragon.setHealth(stats.health.health);
        dragon.lootTable = stats.lootTable;
        DragonCrystal.moreCrystals(dragon, stats);
        DragonLarva.setupLarvaCooldown(dragon, stats);
        DragonMinion.setupMinionCooldown(dragon, stats);
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<DragonStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<DragonStats>, JsonDeserializer<DragonStats> {
        @Override
        public DragonStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String sLootTable = GsonHelper.getAsString(json.getAsJsonObject(), "loot_table", VANILLA_LOOT_TABLE.toString());
            ResourceLocation lootTable = ResourceLocation.tryParse(sLootTable);
            return new DragonStats(GsonHelper.getAsByte(json.getAsJsonObject(), "level"),
                    GsonHelper.getAsFloat(json.getAsJsonObject(), "max_sitting_damage_received"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "roar_time"),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "sitting_scanning_idle_time", 0),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "sitting_flaming_time", 0),
                    context.deserialize(json.getAsJsonObject().get("health"), DragonHealth.class),
                    context.deserialize(json.getAsJsonObject().get("vulnerabilities"), DragonVulnerabilities.class),
                    context.deserialize(json.getAsJsonObject().get("crystal"), DragonCrystal.class),
                    json.getAsJsonObject().has("larva") ? context.deserialize(json.getAsJsonObject().get("larva"), DragonLarva.class) : null,
                    json.getAsJsonObject().has("minion") ? context.deserialize(json.getAsJsonObject().get("minion"), DragonMinion.class) : null,
                    context.deserialize(json.getAsJsonObject().get("attack"), DragonAttack.class),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "xp_dropped"),
                    lootTable);
        }

        @Override
        public JsonElement serialize(DragonStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("level", src.level);
            jsonObject.addProperty("max_sitting_damage_received", src.maxSittingDamageReceived);
            jsonObject.addProperty("roar_time", src.roarTime);
            jsonObject.addProperty("sitting_scanning_idle_time", src.sittingScanningIdleTime);
            jsonObject.addProperty("sitting_flaming_time", src.sittingFlamingTime);
            jsonObject.add("health", context.serialize(src.health));
            jsonObject.add("vulnerabilities", context.serialize(src.vulnerabilities));
            jsonObject.add("crystal", context.serialize(src.crystal));
            if (src.larva != null)
                jsonObject.add("larva", context.serialize(src.larva));
            if (src.minion != null)
                jsonObject.add("minion", context.serialize(src.minion));
            jsonObject.add("attack", context.serialize(src.attack));
            jsonObject.addProperty("xp_dropped", src.xpDropped);
            if (!src.lootTable.equals(VANILLA_LOOT_TABLE))
                jsonObject.addProperty("loot_table", src.lootTable.toString());
            return jsonObject;
        }
    }
}
