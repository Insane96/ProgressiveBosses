package insane96mcp.progressivebosses.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.utils.LvlHelper;
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
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;
import java.util.Set;

public class SetCountPerLvl extends LootItemConditionalFunction {

    public static final MapCodec<SetCountPerLvl> CODEC = RecordCodecBuilder.mapCodec(instance ->
        commonFields(instance).and(
            instance.group(
                NumberProviders.CODEC.fieldOf("count").forGetter(f -> f.count),
                Codec.FLOAT.fieldOf("per_lvl_chance").forGetter(f -> f.perLvlChance),
                Codec.INT.optionalFieldOf("lvl_modifier", 0).forGetter(f -> f.lvlModifier)
            )
        ).apply(instance, SetCountPerLvl::new)
    );

    final NumberProvider count;
    final float perLvlChance;
    final int lvlModifier;

    SetCountPerLvl(List<LootItemCondition> conditions, NumberProvider count, float perLvlChance, int lvlModifier) {
        super(conditions);
        this.count = count;
        this.perLvlChance = perLvlChance;
        this.lvlModifier = lvlModifier;
    }

    @Override
    public LootItemFunctionType<SetCountPerLvl> getType() {
        return PBLoot.SET_COUNT_PER_LVL.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.THIS_ENTITY);
    }

    // Rolls are equal to the current difficulty + difficulty_modifier
    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof Mob mob))
            return itemStack;

        if (!LvlHelper.hasLvl(mob))
            return itemStack;

        int lvl = LvlHelper.getLvl(mob);
        int tries = lvl + this.lvlModifier;
        for (int i = 0; i < tries; i++) {
            float r = lootContext.getRandom().nextFloat();
            if (r < this.perLvlChance)
                itemStack.grow(this.count.getInt(lootContext));
        }
        // Remove one as the loot table starts with 1 item
        itemStack.shrink(1);
        return itemStack;
    }

    public static Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance, int lvlModifier) {
        return simpleBuilder(conditions -> new SetCountPerLvl(conditions, count, perLvlChance, lvlModifier));
    }

    public static Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance) {
        return simpleBuilder(conditions -> new SetCountPerLvl(conditions, count, perLvlChance, 0));
    }
}
