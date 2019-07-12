package insane96mcp.progressivebosses.items;

import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class NetherStarShardItem extends Item {
    public NetherStarShardItem() {
        super(new Item.Properties().group(ItemGroup.MATERIALS).maxStackSize(8));
        setRegistryName("nether_star_shard");
    }
}
