package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.loot.LvlCondition;
import insane96mcp.progressivebosses.loot.RandomChanceWithLvlCondition;
import insane96mcp.progressivebosses.loot.SetCountPerLvl;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBLoot {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ProgressiveBosses.MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> LVL = LOOT_CONDITIONS.register("lvl", () -> new LootItemConditionType(LvlCondition.CODEC));
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> RANDOM_CHANCE_WITH_LVL = LOOT_CONDITIONS.register("random_chance_with_lvl", () -> new LootItemConditionType(RandomChanceWithLvlCondition.CODEC));

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTION = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, ProgressiveBosses.MOD_ID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetCountPerLvl>> SET_COUNT_PER_LVL = LOOT_FUNCTION.register("set_count_per_lvl", () -> new LootItemFunctionType<>(SetCountPerLvl.CODEC));
}
