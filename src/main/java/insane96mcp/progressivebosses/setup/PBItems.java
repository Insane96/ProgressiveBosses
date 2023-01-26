package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PBItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ProgressiveBosses.MOD_ID);

    public static final RegistryObject<Item> NETHER_STAR_SHARD = ITEMS.register(Strings.Items.NETHER_STAR_SHARD, () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ELDER_GUARDIAN_SPIKE = ITEMS.register(Strings.Items.ELDER_GUARDIAN_SPIKE, () -> new Item(new Item.Properties()));
}
