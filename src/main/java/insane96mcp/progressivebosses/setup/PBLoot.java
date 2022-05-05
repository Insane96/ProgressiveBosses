package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.loot.DifficultyCondition;
import insane96mcp.progressivebosses.loot.RandomChanceWithDifficultyCondition;
import insane96mcp.progressivebosses.loot.SetCountPerDifficulty;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PBLoot {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registry.LOOT_CONDITION_TYPE.key(), ProgressiveBosses.MOD_ID);

    public static final RegistryObject<LootItemConditionType> DIFFICULTY = LOOT_CONDITIONS.register("difficulty", () -> new LootItemConditionType(new DifficultyCondition.Serializer()));
    public static final RegistryObject<LootItemConditionType> RANDOM_CHANCE_WITH_DIFFICULTY = LOOT_CONDITIONS.register("random_chance_with_difficulty", () -> new LootItemConditionType(new RandomChanceWithDifficultyCondition.Serializer()));

    public static final DeferredRegister<LootItemFunctionType> LOOT_FUNCTION = DeferredRegister.create(Registry.LOOT_FUNCTION_TYPE.key(), ProgressiveBosses.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> SET_COUNT_PER_DIFFICULTY = LOOT_FUNCTION.register("set_count_per_difficulty", () -> new LootItemFunctionType(new SetCountPerDifficulty.Serializer()));
}
