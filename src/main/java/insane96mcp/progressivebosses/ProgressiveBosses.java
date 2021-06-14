package insane96mcp.progressivebosses;

import insane96mcp.progressivebosses.commands.DifficultyCommand;
import insane96mcp.progressivebosses.setup.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("progressivebosses")
public class ProgressiveBosses {

	public static final String MOD_ID = "progressivebosses";
	public static final String RESOURCE_PREFIX = MOD_ID + ":";

	public static final Logger LOGGER = LogManager.getLogger();

	public ProgressiveBosses() {
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.ITEMS.register(modEventBus);
		ModEntities.ENTITIES.register(modEventBus);
		Reflection.init();
	}

	private void setup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		DifficultyCommand.register(event.getDispatcher());
	}
}
