package insane96mcp.progressivebosses.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class DifficultyCondition implements LootItemCondition {

    final LootContext.EntityTarget entityTarget;
    final String boss;
    final IntRange difficulty;

    DifficultyCondition(LootContext.EntityTarget entityTarget, String boss, IntRange intRange) {
        this.entityTarget = entityTarget;
        this.boss = boss;
        this.difficulty = intRange;
    }

    @Override
    public LootItemConditionType getType() {
        return PBLoot.DIFFICULTY.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());

        if ("wither".equals(this.boss) && !(entity instanceof WitherBoss))
            return false;

        if ("dragon".equals(this.boss) && !(entity instanceof EnderDragon))
            return false;

        Mob mob = (Mob) entity;
        if (!mob.getPersistentData().contains(Strings.Tags.DIFFICULTY))
            return false;

        float difficulty = mob.getPersistentData().getFloat(Strings.Tags.DIFFICULTY);
        return this.difficulty.test(lootContext, (int) difficulty);
    }

    public static LootItemCondition.Builder withDifficulty(LootContext.EntityTarget entityTarget, String boss, IntRange difficulty) {
        return () -> new DifficultyCondition(entityTarget, boss, difficulty);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<DifficultyCondition> {
        @Override
        public void serialize(JsonObject jsonObject, DifficultyCondition difficultyCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("entity", jsonSerializationContext.serialize(difficultyCondition.entityTarget));
            jsonObject.add("boss", jsonSerializationContext.serialize(difficultyCondition.boss));
            jsonObject.add("difficulty", jsonSerializationContext.serialize(difficultyCondition.difficulty));
        }

        @Override
        public DifficultyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            IntRange intRange = GsonHelper.getAsObject(jsonObject, "difficulty", jsonDeserializationContext, IntRange.class);
            String boss = GsonHelper.getAsString(jsonObject, "boss");
            if (!"wither".equals(boss) && !"dragon".equals(boss))
                throw new JsonSyntaxException("boss string is required to be either \"wither\" or \"dragon\"");
            return new DifficultyCondition(GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class), GsonHelper.getAsString(jsonObject, "boss"), intRange);
        }
    }
}
