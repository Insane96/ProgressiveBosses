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
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class LvlCondition implements LootItemCondition {

    final LootContext.EntityTarget entityTarget;
    final IntRange lvl;

    LvlCondition(LootContext.EntityTarget entityTarget, IntRange lvl) {
        this.entityTarget = entityTarget;
        this.lvl = lvl;
    }

    @Override
    public LootItemConditionType getType() {
        return PBLoot.LVL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        if (!(entity instanceof Mob mob))
            return false;

        if (!mob.getPersistentData().contains(Strings.Tags.DIFFICULTY))
            return false;

        float lvl = LvlHelper.getLvl(mob);
        return this.lvl.test(lootContext, (int) lvl);
    }

    public static LootItemCondition.Builder withLvl(LootContext.EntityTarget entityTarget, IntRange lvl) {
        return () -> new LvlCondition(entityTarget, lvl);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LvlCondition> {
        @Override
        public void serialize(JsonObject jsonObject, LvlCondition lvlCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("entity", jsonSerializationContext.serialize(lvlCondition.entityTarget));
            jsonObject.add("lvl", jsonSerializationContext.serialize(lvlCondition.lvl));
        }

        @Override
        public LvlCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            IntRange intRange = GsonHelper.getAsObject(jsonObject, "lvl", jsonDeserializationContext, IntRange.class);
            return new LvlCondition(GsonHelper.getAsObject(jsonObject, "entity", jsonDeserializationContext, LootContext.EntityTarget.class), intRange);
        }
    }
}
