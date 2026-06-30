package insane96mcp.progressivebosses.mixin.accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {
    @Accessor
    void setLootTable(ResourceKey<LootTable> lootTable);

    @Accessor
    void setXpReward(int xpReward);
}
