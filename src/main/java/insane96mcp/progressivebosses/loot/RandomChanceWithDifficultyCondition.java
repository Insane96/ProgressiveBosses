package insane96mcp.progressivebosses.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class RandomChanceWithDifficultyCondition implements LootItemCondition {
    final float chance;
    final int difficultyModifier;

    RandomChanceWithDifficultyCondition(float chance, int difficultyModifier) {
        this.chance = chance;
        this.difficultyModifier = difficultyModifier;
    }

    public LootItemConditionType getType() {
        return PBLoot.RANDOM_CHANCE_WITH_DIFFICULTY.get();
    }

    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof Mob mob))
            return false;

        if (!mob.getPersistentData().contains(Strings.Tags.DIFFICULTY))
            return false;

        float difficulty = mob.getPersistentData().getFloat(Strings.Tags.DIFFICULTY);
        return lootContext.getRandom().nextFloat() < this.chance * (difficulty + this.difficultyModifier);
    }

    public static LootItemCondition.Builder randomChanceWithDifficulty(float chance, int difficultyModifier) {
        return () -> new RandomChanceWithDifficultyCondition(chance, difficultyModifier);
    }

    public static LootItemCondition.Builder randomChanceWithDifficulty(float chance) {
        return () -> new RandomChanceWithDifficultyCondition(chance, 0);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<RandomChanceWithDifficultyCondition> {
        public void serialize(JsonObject jsonObject, RandomChanceWithDifficultyCondition randomChanceWithDifficultyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("chance", randomChanceWithDifficultyCondition.chance);
            jsonObject.addProperty("difficulty_modifier", randomChanceWithDifficultyCondition.difficultyModifier);
        }

        public RandomChanceWithDifficultyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new RandomChanceWithDifficultyCondition(GsonHelper.getAsFloat(jsonObject, "chance"), GsonHelper.getAsInt(jsonObject, "difficulty_modifier", 0));
        }
    }
}
