package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.corruptedendcrystal.CorruptedEndCrystalItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class PBItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.Items.createItems(ProgressiveBosses.MOD_ID);

    public static final DeferredItem<Item> NETHER_STAR_SHARD = REGISTRY.register("nether_star_shard", () -> new Item(new Item.Properties()));
    public static final DeferredItem<BlockItem> CORRUPTED_SOUL_SAND = REGISTRY.register("corrupted_soul_sand", () -> new BlockItem(PBBlocks.CORRUPTED_SOUL_SAND.get(), new Item.Properties()));

    public static final DeferredItem<Item> ELDER_GUARDIAN_SPIKE = REGISTRY.register("elder_guardian_spike", () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CORRUPTED_END_CRYSTAL = PBItems.REGISTRY.register("corrupted_end_crystal", () -> new CorruptedEndCrystalItem(new Item.Properties()));
}
