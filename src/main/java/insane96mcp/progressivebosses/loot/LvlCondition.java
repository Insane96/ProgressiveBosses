package insane96mcp.progressivebosses.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import insane96mcp.progressivebosses.module.dragon.DragonFeature;
import insane96mcp.progressivebosses.module.elderguardian.ElderGuardianFeature;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.setup.PBLoot;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;
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
        int lvl = -1;
        if (entity instanceof PBWither wither)
            lvl = wither.getLvl();
        else if (entity instanceof EnderDragon dragon)
            lvl = DragonFeature.getDragonLvl(dragon);
        else if (entity instanceof ElderGuardian elderGuardian)
            lvl = ElderGuardianFeature.getGuardianLvl(elderGuardian);
        if (lvl == -1)
            return false;

        return this.lvl.test(lootContext, lvl);
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
