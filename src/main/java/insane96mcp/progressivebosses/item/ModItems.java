package net.insane96mcp.progressivebosses.item;

import java.util.ArrayList;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.insane96mcp.progressivebosses.lib.Strings.Names;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

public class ModItems {
	
	public static Item starShard;
	
	public static ArrayList<Item> ITEMS = new ArrayList<Item>();
	
	public static void Init() {
		starShard = new Item();
		starShard.setCreativeTab(CreativeTabs.MATERIALS);
		starShard.setRegistryName(new ResourceLocation(ProgressiveBosses.MOD_ID, Names.NETHER_STAR_SHARD));
		starShard.setTranslationKey(ProgressiveBosses.RESOURCE_PREFIX + Names.NETHER_STAR_SHARD);
		ITEMS.add(starShard);
	}
}
