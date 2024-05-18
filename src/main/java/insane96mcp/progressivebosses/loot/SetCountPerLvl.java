package insane96mcp.progressivebosses.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.setup.Strings;
import insane96mcp.progressivebosses.utils.LvlHelper;
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

public class SetCountPerLvl extends LootItemConditionalFunction {
    final NumberProvider count;
    final float perLvlChance;
    final int lvlModifier;

    SetCountPerLvl(LootItemCondition[] lootItemConditions, NumberProvider count, float perLvlChance, int lvlModifier) {
        super(lootItemConditions);
        this.count = count;
        this.perLvlChance = perLvlChance;
        this.lvlModifier = lvlModifier;
    }

    public LootItemFunctionType getType() {
        return PBLoot.SET_COUNT_PER_LVL.get();
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

        int lvl = LvlHelper.getLvl(mob);
        int tries = lvl + this.lvlModifier;
        for (int i = 0; i < tries; i++) {
            float r = lootContext.getRandom().nextFloat();
            if (r < this.perLvlChance)
                itemStack.grow(this.count.getInt(lootContext));
        }
        //Remove one as the loot table starts with 1 item
        itemStack.shrink(1);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance, int lvlModifier) {
        return simpleBuilder((lootItemConditions) -> new SetCountPerLvl(lootItemConditions, count, perLvlChance, lvlModifier));
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance) {
        return simpleBuilder((lootItemConditions) -> new SetCountPerLvl(lootItemConditions, count, perLvlChance, 0));
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<SetCountPerLvl> {
        public void serialize(JsonObject jsonObject, SetCountPerLvl setCountPerLvl, JsonSerializationContext jsonSerializationContext) {
            super.serialize(jsonObject, setCountPerLvl, jsonSerializationContext);
            jsonObject.add("count", jsonSerializationContext.serialize(setCountPerLvl.count));
            jsonObject.add("per_lvl_chance", jsonSerializationContext.serialize(setCountPerLvl.perLvlChance));
            jsonObject.add("lvl_modifier", jsonSerializationContext.serialize(setCountPerLvl.lvlModifier));
        }

        public SetCountPerLvl deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
            NumberProvider numberprovider = GsonHelper.getAsObject(jsonObject, "count", jsonDeserializationContext, NumberProvider.class);
            return new SetCountPerLvl(lootItemConditions, numberprovider, Mth.clamp(GsonHelper.getAsFloat(jsonObject, "per_lvl_chance", 1f), 0f, 1f), GsonHelper.getAsInt(jsonObject, "lvl_modifier", 0));
        }
    }
}