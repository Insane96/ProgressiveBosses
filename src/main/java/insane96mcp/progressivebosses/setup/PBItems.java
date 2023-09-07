package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, ProgressiveBosses.MOD_ID);

    public static final RegistryObject<Item> NETHER_STAR_SHARD = REGISTRY.register(Strings.Items.NETHER_STAR_SHARD, () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELDER_GUARDIAN_SPIKE = REGISTRY.register(Strings.Items.ELDER_GUARDIAN_SPIKE, () -> new Item(new Item.Properties()));
    public static final RegistryObject<BlockItem> CORRUPTED_SOUL_SAND = REGISTRY.register("corrupted_soul_sand", () -> new BlockItem(PBBlocks.CORRUPTED_SOUL_SAND.get(), new Item.Properties()));
}
