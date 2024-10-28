package insane96mcp.progressivebosses.module.dragon.data;

import com.google.gson.*;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.reflect.TypeToken;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.lang.reflect.Type;
import java.util.ArrayList;

@JsonAdapter(DragonStats.Serializer.class)
public class DragonStats {
    private static final ResourceLocation VANILLA_LOOT_TABLE = new ResourceLocation("entities/ender_dragon");

    public int level;
    public DragonHealth health;
    /*public WitherAttack attack;
    @Nullable
    public PoweredAttributeModifiers attributeModifiers;
    @Nullable
    public WitherMinionStats minion;
    public WitherDeath death;
    public WitherMiscStats misc;*/
    public int xpDropped;
    public ResourceLocation lootTable;

    public DragonStats(int level, DragonHealth health, /*WitherAttack attack, @Nullable PoweredAttributeModifiers attributeModifiers, @Nullable WitherMinionStats minion, WitherDeath death, WitherMiscStats misc,*/ int xpDropped, ResourceLocation lootTable) {
        this.level = level;
        this.health = health;
        //this.attack = attack;
        //this.attributeModifiers = attributeModifiers;
        //this.minion = minion;
        //this.death = death;
        //this.misc = misc;
        this.xpDropped = xpDropped;
        this.lootTable = lootTable;
    }

    public static void apply(EnderDragon dragon, DragonStats stats) {
        dragon.getAttribute(Attributes.MAX_HEALTH).setBaseValue(stats.health.health);
        dragon.setHealth(stats.health.health);
        //if (this.attributeModifiers != null) {
        //    List<SerializableAttributeModifier> listToAdd = wither.isPowered() ? this.attributeModifiers.belowHalfHealth : this.attributeModifiers.aboveHalfHealth;
        //    for (SerializableAttributeModifier modifier : listToAdd) {
        //        MCUtils.applyModifier(wither, modifier.attribute().get(), modifier.getModifier(), true);
        //    }
        //}
        //if (this.minion != null)
        //    this.minion.setCooldown(wither, 2f);
        dragon.lootTable = stats.lootTable;
    }

    public void finalizeSpawn(PBWither wither) {
        wither.setHealth(wither.getMaxHealth());
    }

    public static final Type LIST_TYPE = new TypeToken<ArrayList<DragonStats>>(){}.getType();

    public static class Serializer implements JsonSerializer<DragonStats>, JsonDeserializer<DragonStats> {
        @Override
        public DragonStats deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String sLootTable = GsonHelper.getAsString(json.getAsJsonObject(), "loot_table", VANILLA_LOOT_TABLE.getPath());
            ResourceLocation lootTable = ResourceLocation.tryParse(sLootTable);
            //PoweredAttributeModifiers resistances = json.getAsJsonObject().has("attribute_modifiers") ? context.deserialize(json.getAsJsonObject().get("attribute_modifiers"), PoweredAttributeModifiers.class) : null;
            //WitherMinionStats witherMinionStats = json.getAsJsonObject().has("minion") ? context.deserialize(json.getAsJsonObject().get("minion"), WitherMinionStats.class) : null;
            return new DragonStats(GsonHelper.getAsInt(json.getAsJsonObject(), "level"),
                    context.deserialize(json.getAsJsonObject().get("health"), DragonHealth.class),
                    //context.deserialize(json.getAsJsonObject().get("attack"), WitherAttack.class),
                    //resistances,
                    //witherMinionStats,
                    //context.deserialize(json.getAsJsonObject().get("death"), WitherDeath.class),
                    //context.deserialize(json.getAsJsonObject().get("misc"), WitherMiscStats.class),
                    GsonHelper.getAsInt(json.getAsJsonObject(), "xp_dropped"),
                    lootTable);
        }

        @Override
        public JsonElement serialize(DragonStats src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("level", src.level);
            jsonObject.add("health", context.serialize(src.health));
            //jsonObject.add("attack", context.serialize(src.attack));
            //if (src.attributeModifiers != null)
            //    jsonObject.add("attribute_modifiers", context.serialize(src.attributeModifiers));
            //if (src.minion != null)
            //    jsonObject.add("minion", context.serialize(src.minion));
            //jsonObject.add("death", context.serialize(src.death));
            //jsonObject.add("misc", context.serialize(src.misc));
            jsonObject.addProperty("xp_dropped", src.xpDropped);
            if (!src.lootTable.equals(VANILLA_LOOT_TABLE))
                jsonObject.addProperty("loot_table", src.lootTable.toString());
            return jsonObject;
        }
    }
}
