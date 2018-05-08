package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.insane96mcp.progressivebosses.item.ModItems;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class RegistryEventHandler {

	//1.12 Register Items and Blocks
	/*@SubscribeEvent
	public static void RegisterBlocks(RegistryEvent.Register<Block> event) {
		for (Block block : ModBlocks.BLOCKS)
			event.getRegistry().register(block);
	}*/
	
	@SubscribeEvent
	public static void RegisterItems(RegistryEvent.Register<Item> event) {
		for (Item item : ModItems.ITEMS)
			event.getRegistry().register(item);

		/*for (Block block : ModBlocks.BLOCKS)
			event.getRegistry().register(new ItemBlock(block).setRegistryName(block.getRegistryName()));*/
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void RegisterModels(ModelRegistryEvent event) {
		for (Item item : ModItems.ITEMS) {				
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
		}
		/*for (Block block : ModBlocks.BLOCKS) {
			Item item = Item.getItemFromBlock(block);
			ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
		}*/
	}
}
