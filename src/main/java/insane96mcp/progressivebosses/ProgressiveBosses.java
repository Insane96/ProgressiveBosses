package insane96mcp.progressivebosses;

import insane96mcp.progressivebosses.capability.DifficultyProvider;
import insane96mcp.progressivebosses.commands.PBCommand;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.module.wither.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.network.PacketManager;
import insane96mcp.progressivebosses.setup.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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

	public static final String CONFIG_FOLDER = "config/" + MOD_ID;

	public static final Logger LOGGER = LogManager.getLogger();

	public ProgressiveBosses() {
		ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(this);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::registerEntityRenderers);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::creativeTabsBuildContents);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		PBItems.ITEMS.register(modEventBus);
		PBEntities.ENTITIES.register(modEventBus);
		PBLoot.LOOT_CONDITIONS.register(modEventBus);
		PBLoot.LOOT_FUNCTION.register(modEventBus);
		Reflection.init();

		CrystalRespawnPhase.init();
	}

	@SubscribeEvent
	public void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event)
	{
		if (event.getObject() instanceof Player)
			event.addCapability(DifficultyProvider.IDENTIFIER, new DifficultyProvider());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onAddReloadListener(AddReloadListenerEvent event) {
		event.addListener(WitherStatsReloadListener.INSTANCE);
	}
	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		PBCommand.register(event.getDispatcher());
	}

	private void setup(final FMLCommonSetupEvent event) {
		PacketManager.init();
	}
}
