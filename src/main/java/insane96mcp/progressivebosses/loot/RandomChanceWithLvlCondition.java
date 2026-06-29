package insane96mcp.progressivebosses.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record RandomChanceWithLvlCondition(float chance, int lvlModifier) implements LootItemCondition {

    public static final MapCodec<RandomChanceWithLvlCondition> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.FLOAT.fieldOf("chance").forGetter(RandomChanceWithLvlCondition::chance),
            Codec.INT.optionalFieldOf("lvl_modifier", 0).forGetter(RandomChanceWithLvlCondition::lvlModifier)
    ).apply(inst, RandomChanceWithLvlCondition::new));

    @Override
    public LootItemConditionType getType() {
        return PBLoot.RANDOM_CHANCE_WITH_LVL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Mob mob))
            return false;
        if (!ModNBTData.contains(mob, LvlHelper.LEVEL_KEY))
            return false;
        int lvl = LvlHelper.getLvl(mob);
        return lootContext.getRandom().nextFloat() < this.chance * (lvl + this.lvlModifier);
    }

    public static LootItemCondition.Builder randomChanceWithLvl(float chance, int lvlModifier) {
        return () -> new RandomChanceWithLvlCondition(chance, lvlModifier);
    }

    public static LootItemCondition.Builder randomChanceWithLvl(float chance) {
        return () -> new RandomChanceWithLvlCondition(chance, 0);
    }
}
