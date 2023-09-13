package insane96mcp.progressivebosses.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class RandomChanceWithLvlCondition implements LootItemCondition {
    final float chance;
    final int lvlModifier;

    RandomChanceWithLvlCondition(float chance, int lvlModifier) {
        this.chance = chance;
        this.lvlModifier = lvlModifier;
    }

    public LootItemConditionType getType() {
        return PBLoot.RANDOM_CHANCE_WITH_LVL.get();
    }

    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof Mob mob))
            return false;

        if (!mob.getPersistentData().contains(Strings.Tags.DIFFICULTY))
            return false;

        float lvl = LvlHelper.getLvl(mob);
        return lootContext.getRandom().nextFloat() < this.chance * (lvl + this.lvlModifier);
    }

    public static LootItemCondition.Builder randomChargeWithLvl(float chance, int lvlModifier) {
        return () -> new RandomChanceWithLvlCondition(chance, lvlModifier);
    }

    public static LootItemCondition.Builder randomChanceWithLvl(float chance) {
        return () -> new RandomChanceWithLvlCondition(chance, 0);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<RandomChanceWithLvlCondition> {
        public void serialize(JsonObject jsonObject, RandomChanceWithLvlCondition randomChanceWithLvlCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", randomChanceWithLvlCondition.chance);
            jsonObject.addProperty("lvl_modifier", randomChanceWithLvlCondition.lvlModifier);
        }

        public RandomChanceWithLvlCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new RandomChanceWithLvlCondition(GsonHelper.getAsFloat(jsonObject, "chance"), GsonHelper.getAsInt(jsonObject, "lvl_modifier", 0));
        }
    }
}
