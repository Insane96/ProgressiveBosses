package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.items.NetherStarShardItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ProgressiveBosses.MOD_ID)
public class RegistryEventHandler {

    //1.12 Register Items and Blocks
	/*@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		for (Block block : ModBlocks.BLOCKS)
			event.getRegistry().register(block);
	}*/

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new NetherStarShardItem());

		/*for (Block block : ModBlocks.BLOCKS)
			event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));*/
    }
}
