package net.insane96mcp.progressivebosses.proxies;

import net.insane96mcp.progressivebosses.events.EntityJoinWorld;
import net.insane96mcp.progressivebosses.events.LivingDeath;
import net.insane96mcp.progressivebosses.events.LivingDrops;
import net.insane96mcp.progressivebosses.events.LivingUpdate;
import net.insane96mcp.progressivebosses.events.PlayerClone;
import net.insane96mcp.progressivebosses.events.RegistryEventHandler;
import net.insane96mcp.progressivebosses.item.ModItems;
import net.insane96mcp.progressivebosses.lib.Config;
import net.insane96mcp.progressivebosses.lib.LootTables;
import net.insane96mcp.progressivebosses.lib.Properties;
import net.insane96mcp.progressivebosses.lib.Reflection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {
	public void PreInit(FMLPreInitializationEvent event) {
		Config.config = new Configuration(event.getSuggestedConfigurationFile());
		Config.SyncConfig();
		Properties.Init();
		LootTables.Init();
		Reflection.Init();
		ModItems.Init();
	}
	
	public void Init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(EntityJoinWorld.class);
		MinecraftForge.EVENT_BUS.register(LivingUpdate.class);
		MinecraftForge.EVENT_BUS.register(LivingDeath.class);
		MinecraftForge.EVENT_BUS.register(LivingDrops.class);
		MinecraftForge.EVENT_BUS.register(PlayerClone.class);
	}
	
	public void PostInit(FMLPostInitializationEvent event) {
		Config.SaveConfig();
	}
}
