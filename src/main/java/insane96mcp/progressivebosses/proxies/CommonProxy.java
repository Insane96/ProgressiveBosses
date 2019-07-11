package net.insane96mcp.progressivebosses.proxies;

import net.insane96mcp.progressivebosses.item.ModItems;
import net.insane96mcp.progressivebosses.lib.LootTables;
import net.insane96mcp.progressivebosses.lib.Reflection;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	public void PreInit(FMLPreInitializationEvent event) {
		LootTables.Init();
		Reflection.Init();
		ModItems.Init();
	}
	
	public void Init(FMLInitializationEvent event) {

	}
	
	public void PostInit(FMLPostInitializationEvent event) {
		
	}
}
