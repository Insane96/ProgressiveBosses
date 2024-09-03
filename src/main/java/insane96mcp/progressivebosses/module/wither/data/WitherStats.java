package insane96mcp.progressivebosses.module.wither.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.insanelib.data.SerializableAttributeModifier;
import insane96mcp.insanelib.util.MCUtils;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@JsonAdapter(WitherStats.Serializer.class)
public class WitherStats {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/wither");

    public int level;
    public WitherAttack attack;
    public WitherHealth health;
    @Nullable
    public PoweredAttributeModifiers attributeModifiers;
    @Nullable
    public WitherMinionStats minion;
    public WitherDeath death;
    public WitherMiscStats misc;
    public int xpDropped;
    public ResourceLocation lootTable;

    public WitherStats(int level, WitherAttack attack, WitherHealth health, @Nullable PoweredAttributeModifiers attributeModifiers, @Nullable WitherMinionStats minion, WitherDeath death, WitherMiscStats misc, int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.attack = attack;
        this.health = health;
        this.attributeModifiers = attributeModifiers;
        this.minion = minion;
        this.death = death;
        this.misc = misc;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    public void apply(PBWither wither) {
        wither.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.health.health);
        if (this.attributeModifiers != null) {
            List<SerializableAttributeModifier> listToAdd = wither.isPowered() ? this.attributeModifiers.belowHalfHealth : this.attributeModifiers.aboveHalfHealth;
            for (SerializableAttributeModifier modifier : listToAdd) {
                MCUtils.applyModifier(wither, modifier.attribute().get(), modifier.getModifier(), true);
            }
        }
        if (this.minion != null)
            this.minion.setCooldown(wither, 2f);
        wither.lootTable = this.lootTable;
        wither.xpReward = this.xpDropped;
    }

    public void finalizeSpawn(PBWither wither) {
        wither.setHealth(wither.getMaxHealth());
    }

    public static final java.lang.reflect.Type LIST_TYPE = new TypeToken<ArrayList<WitherStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<WitherStats>, JsonDeserializer<WitherStats> {
        @Override
        public WitherStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String sLootTable = GsonHelper.getAsString(json.getAsJsonObject(), "loot_table", VANILLA_LOOT_TABLE.getPath());
            ResourceLocation lootTable = ResourceLocation.tryParse(sLootTable);
            PoweredAttributeModifiers resistances = json.getAsJsonObject().has("attribute_modifiers") ? context.deserialize(json.getAsJsonObject().get("attribute_modifiers"), PoweredAttributeModifiers.class) : null;
            WitherMinionStats witherMinionStats = json.getAsJsonObject().has("minion") ? context.deserialize(json.getAsJsonObject().get("minion"), WitherMinionStats.class) : null;
            return new WitherStats(GsonHelper.getAsInt(json.getAsJsonObject(), "level"),
                    context.deserialize(json.getAsJsonObject().get("attack"), WitherAttack.class),
                    context.deserialize(json.getAsJsonObject().get("health"), WitherHealth.class),
                    resistances,
                    witherMinionStats,
                    context.deserialize(json.getAsJsonObject().get("death"), WitherDeath.class),
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
            if (src.attributeModifiers != null)
                jsonObject.add("attribute_modifiers", context.serialize(src.attributeModifiers));
            if (src.minion != null)
                jsonObject.add("minion", context.serialize(src.minion));
            jsonObject.add("death", context.serialize(src.death));
            jsonObject.add("misc", context.serialize(src.misc));
            jsonObject.addProperty("xp_dropped", src.xpDropped);
            if (!src.lootTable.equals(VANILLA_LOOT_TABLE))
                jsonObject.addProperty("loot_table", src.lootTable.toString());
            return jsonObject;
        }
    }
}
