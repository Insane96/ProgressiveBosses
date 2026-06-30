package insane96mcp.progressivebosses.module.elderguardian.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(ElderGuardianStats.Serializer.class)
public class ElderGuardianStats {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/elder_guardian");

    public int level;
    public float bonusDamage;
    public int attackDuration;
    public float damageResistance;
    public int minionCooldown;
    public float health;
    public float absorption;
    public float regenOnAttack;
    public int xpDropped;
    public ResourceLocation lootTable;

    public ElderGuardianStats(int level, float bonusDamage, int attackDuration, float damageResistance, int minionCooldown, float health, float absorption, float regenOnAttack, int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.bonusDamage = bonusDamage;
        this.attackDuration = attackDuration;
        this.damageResistance = damageResistance;
        this.minionCooldown = minionCooldown;
        this.health = health;
        this.absorption = absorption;
        this.regenOnAttack = regenOnAttack;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<ElderGuardianStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<ElderGuardianStats>, JsonDeserializer<ElderGuardianStats> {
        @Override
        public ElderGuardianStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jObject = json.getAsJsonObject();
            String sLootTable = GsonHelper.getAsString(jObject, "loot_table", VANILLA_LOOT_TABLE.getPath());
            ResourceLocation lootTable = ResourceLocation.tryParse(sLootTable);
            return new ElderGuardianStats(GsonHelper.getAsInt(jObject, "level"),
                    GsonHelper.getAsFloat(jObject, "bonus_damage"),
                    GsonHelper.getAsInt(jObject, "attack_duration"),
                    GsonHelper.getAsFloat(jObject, "damage_resistance"),
                    GsonHelper.getAsInt(jObject, "minion_cooldown"),
                    GsonHelper.getAsFloat(jObject, "health"),
                    GsonHelper.getAsFloat(jObject, "absorption"),
                    GsonHelper.getAsFloat(jObject, "regen_on_attack"),
                    GsonHelper.getAsInt(jObject, "xp_dropped"),
                    lootTable);
        }

        @Override
        public JsonElement serialize(ElderGuardianStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("level", src.level);
            jObject.addProperty("bonus_damage", src.bonusDamage);
            jObject.addProperty("attack_duration", src.attackDuration);
            jObject.addProperty("damage_resistance", src.damageResistance);
            jObject.addProperty("minion_cooldown", src.minionCooldown);
            jObject.addProperty("health", src.health);
            jObject.addProperty("absorption", src.absorption);
            jObject.addProperty("regen_on_attack", src.regenOnAttack);
            jObject.addProperty("xp_dropped", src.xpDropped);
            if (!src.lootTable.equals(VANILLA_LOOT_TABLE))
                jObject.addProperty("loot_table", src.lootTable.toString());
            return jObject;
        }
    }
}
