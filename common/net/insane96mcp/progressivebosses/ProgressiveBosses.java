package net.insane96mcp.progressivebosses;

import java.util.Random;

import net.insane96mcp.progressivebosses.commands.Counter;
import net.insane96mcp.progressivebosses.proxies.CommonProxy;
import net.minecraft.world.gen.structure.MapGenEndCity.Start;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = ProgressiveBosses.MOD_ID, name = ProgressiveBosses.MOD_NAME, version = ProgressiveBosses.VERSION, acceptableRemoteVersions = "*", acceptedMinecraftVersions = ProgressiveBosses.MINECRAFT_VERSIONS)
public class ProgressiveBosses {
	
	public static final String MOD_ID = "progressivebosses";
	public static final String MOD_NAME = "Progressive Bosses";
	public static final String VERSION = "1.2.0";
	public static final String RESOURCE_PREFIX = MOD_ID.toLowerCase() + ":";
	public static final String MINECRAFT_VERSIONS = "[1.12,1.12.2]";
	
	@Instance(MOD_ID)
	public static ProgressiveBosses instance;
	
	@SidedProxy(clientSide = "net.insane96mcp.progressivebosses.proxies.ClientProxy", serverSide = "net.insane96mcp.progressivebosses.proxies.ServerProxy")
	public static CommonProxy proxy;
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event) {
		proxy.PreInit(event);
	}
	
	@EventHandler
	public void Init(FMLInitializationEvent event) {
		proxy.Init(event);
	}
	
	@EventHandler
	public void PostInit(FMLPostInitializationEvent event) {
		proxy.PostInit(event);
	}
	
	@EventHandler 
	public void Start(FMLServerStartingEvent event){
		event.registerServerCommand(new Counter());
	}
}
