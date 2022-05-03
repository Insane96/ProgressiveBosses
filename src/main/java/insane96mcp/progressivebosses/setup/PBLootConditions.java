package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.loot.DifficultyCondition;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class PBLootConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registry.LOOT_CONDITION_TYPE.key(), ProgressiveBosses.MOD_ID);

    public static final RegistryObject<LootItemConditionType> DIFFICULTY = LOOT_CONDITIONS.register("difficulty", () -> new LootItemConditionType(new DifficultyCondition.Serializer()));
}
