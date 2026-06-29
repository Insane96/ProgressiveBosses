package insane96mcp.progressivebosses.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.setup.PBLoot;
import insane96mcp.progressivebosses.utils.LvlHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

import java.util.List;

public class SetCountPerLvl extends LootItemConditionalFunction {

    public static final MapCodec<SetCountPerLvl> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(
                    inst.group(
                            NumberProviders.CODEC.fieldOf("count").forGetter(f -> f.count),
                            Codec.FLOAT.fieldOf("per_lvl_chance").forGetter(f -> f.perLvlChance),
                            Codec.INT.optionalFieldOf("lvl_modifier", 0).forGetter(f -> f.lvlModifier)
                    )
            ).apply(inst, SetCountPerLvl::new)
    );

    final NumberProvider count;
    final float perLvlChance;
    final int lvlModifier;

    SetCountPerLvl(List<LootItemCondition> conditions, NumberProvider count, float perLvlChance, int lvlModifier) {
        super(conditions);
        this.count = count;
        this.perLvlChance = Mth.clamp(perLvlChance, 0f, 1f);
        this.lvlModifier = lvlModifier;
    }

    @Override
    public LootItemFunctionType<SetCountPerLvl> getType() {
        return PBLoot.SET_COUNT_PER_LVL.get();
    }

    // Rolls are equal to the current lvl + lvl_modifier
    @Override
    public ItemStack run(ItemStack itemStack, LootContext lootContext) {
        Entity entity = lootContext.getParam(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Mob mob))
            return itemStack;
        if (!ModNBTData.contains(mob, LvlHelper.LEVEL_KEY))
            return itemStack;
        int lvl = LvlHelper.getLvl(mob);
        int tries = lvl + this.lvlModifier;
        for (int i = 0; i < tries; i++) {
            if (lootContext.getRandom().nextFloat() < this.perLvlChance)
                itemStack.grow(this.count.getInt(lootContext));
        }
        // Remove one as the loot table starts with 1 item
        itemStack.shrink(1);
        return itemStack;
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance, int lvlModifier) {
        return simpleBuilder(conditions -> new SetCountPerLvl(conditions, count, perLvlChance, lvlModifier));
    }

    public static LootItemConditionalFunction.Builder<?> setCountPerLvl(NumberProvider count, float perLvlChance) {
        return simpleBuilder(conditions -> new SetCountPerLvl(conditions, count, perLvlChance, 0));
    }
}
