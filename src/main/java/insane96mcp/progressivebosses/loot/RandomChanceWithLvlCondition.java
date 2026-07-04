package insane96mcp.progressivebosses.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class RandomChanceWithLvlCondition implements LootItemCondition {

    public static final MapCodec<RandomChanceWithLvlCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("chance").forGetter(c -> c.chance),
            Codec.INT.optionalFieldOf("lvl_modifier", 0).forGetter(c -> c.lvlModifier)
        ).apply(instance, RandomChanceWithLvlCondition::new)
    );

    final float chance;
    final int lvlModifier;

    RandomChanceWithLvlCondition(float chance, int lvlModifier) {
        this.chance = chance;
        this.lvlModifier = lvlModifier;
    }

    @Override
    public LootItemConditionType getType() {
        return PBLoot.RANDOM_CHANCE_WITH_LVL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof Mob mob))
            return false;

        if (!LvlHelper.hasLvl(mob))
            return false;

        float lvl = LvlHelper.getLvl(mob);
        return lootContext.getRandom().nextFloat() < this.chance * (lvl + this.lvlModifier);
    }

    public static Builder randomChargeWithLvl(float chance, int lvlModifier) {
        return () -> new RandomChanceWithLvlCondition(chance, lvlModifier);
    }

    public static Builder randomChanceWithLvl(float chance) {
        return () -> new RandomChanceWithLvlCondition(chance, 0);
    }
}
