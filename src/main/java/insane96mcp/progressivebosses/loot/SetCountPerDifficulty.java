package insane96mcp.progressivebosses.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.Set;

public class SetCountPerDifficulty extends LootItemConditionalFunction {
    final NumberProvider count;
    final float perDifficultyChance;
    final int difficultyModifier;

    SetCountPerDifficulty(LootItemCondition[] lootItemConditions, NumberProvider count, float perDifficultyChance, int difficultyModifier) {
        super(lootItemConditions);
        this.count = count;
        this.perDifficultyChance = perDifficultyChance;
        this.difficultyModifier = difficultyModifier;
    }

    public LootItemFunctionType getType() {
        return PBLoot.SET_COUNT_PER_DIFFICULTY.get();
    }

    public Set<LootContextParam<?>> getReferencedContextParams() {
        ImmutableSet.Builder<LootContextParam<?>> builder = ImmutableSet.builder();
        builder.add(LootContextParams.THIS_ENTITY);
        return builder.build();
    }

    //Rolls are equal to the current difficulty + difficulty_modifier
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof Mob mob))
            return itemStack;

        if (!mob.getPersistentData().contains(Strings.Tags.DIFFICULTY))
            return itemStack;

        float difficulty = mob.getPersistentData().getFloat(Strings.Tags.DIFFICULTY);
        int tries = (int) difficulty + this.difficultyModifier;
        for (int i = 0; i < tries; i++) {
            float r = lootContext.getRandom().nextFloat();
            if (r < this.perDifficultyChance)
                itemStack.grow(this.count.getInt(lootContext));
        }
        //Remove one as the loot table starts with 1 item
        itemStack.shrink(1);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerDifficulty(NumberProvider count, float perDifficultyChance, int difficultyModifier) {
        return simpleBuilder((lootItemConditions) -> new SetCountPerDifficulty(lootItemConditions, count, perDifficultyChance, difficultyModifier));
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerDifficulty(NumberProvider count, float perDifficultyChance) {
        return simpleBuilder((lootItemConditions) -> new SetCountPerDifficulty(lootItemConditions, count, perDifficultyChance, 0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetCountPerDifficulty> {
        public void serialize(JsonObject jsonObject, SetCountPerDifficulty setCountPerDifficulty, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setCountPerDifficulty, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize(setCountPerDifficulty.count));
            jsonObject.add("per_difficulty_chance", jsonSerializationContext.serialize(setCountPerDifficulty.perDifficultyChance));
            jsonObject.add("difficulty_modifier", jsonSerializationContext.serialize(setCountPerDifficulty.difficultyModifier));
        }

        public SetCountPerDifficulty deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            NumberProvider numberprovider = GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, NumberProvider.class);
            return new SetCountPerDifficulty(lootItemConditions, numberprovider, Mth.clamp(GsonHelper.getAsFloat(jsonObject, "per_difficulty_chance", 1f), 0f, 1f), GsonHelper.getAsInt(jsonObject, "difficulty_modifier", 0));
        }
    }
}