package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Strings;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class PBItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ProgressiveBosses.MOD_ID);

    public static final RegistryObject<Item> NETHER_STAR_SHARD = ITEMS.register(Strings.Items.NETHER_STAR_SHARD, () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
    public static final RegistryObject<Item> ELDER_GUARDIAN_SPIKE = ITEMS.register(Strings.Items.ELDER_GUARDIAN_SPIKE, () -> new Item(new Item.Properties().tab(ItemGroup.TAB_MATERIALS)));
}
