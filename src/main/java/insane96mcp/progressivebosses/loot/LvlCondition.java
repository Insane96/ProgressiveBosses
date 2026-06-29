package insane96mcp.progressivebosses.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LvlCondition(LootContext.EntityTarget entityTarget, IntRange lvl) implements LootItemCondition {

    public static final MapCodec<LvlCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            LootContext.EntityTarget.CODEC.fieldOf("entity").forGetter(LvlCondition::entityTarget),
            IntRange.CODEC.fieldOf("lvl").forGetter(LvlCondition::lvl)
    ).apply(inst, LvlCondition::new));

    @Override
    public LootItemConditionType getType() {
        return PBLoot.LVL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.entityTarget.getParam());
        if (!(entity instanceof LivingEntity living))
            return false;
        if (!ModNBTData.contains(living, LvlHelper.LEVEL_KEY))
            return false;
        return this.lvl.test(lootContext, LvlHelper.getLvl(living));
    }

    public static LootItemCondition.Builder withLvl(LootContext.EntityTarget entityTarget, IntRange lvl) {
        return () -> new LvlCondition(entityTarget, lvl);
    }
}
