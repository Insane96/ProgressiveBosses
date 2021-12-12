package insane96mcp.progressivebosses;

import insane96mcp.progressivebosses.base.Strings;
import insane96mcp.progressivebosses.capability.DifficultyCapability;
import insane96mcp.progressivebosses.commands.PBCommand;
import insane96mcp.progressivebosses.module.dragon.phase.CrystalRespawnPhase;
import insane96mcp.progressivebosses.setup.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientSetup::init);
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		PBItems.ITEMS.register(modEventBus);
		PBEntities.ENTITIES.register(modEventBus);
		Reflection.init();

		CrystalRespawnPhase.init();
	}

	@SubscribeEvent
	public void attachCapabilitiesEntity(final AttachCapabilitiesEvent<Entity> event)
	{
		if(event.getObject() instanceof PlayerEntity) {
			DifficultyCapability difficultyCapability = new DifficultyCapability();
			event.addCapability(new ResourceLocation(Strings.Tags.DIFFICULTY), difficultyCapability);
			event.addListener(difficultyCapability::invalidate);
		}
	}

	private void setup(final FMLCommonSetupEvent event) {
		DifficultyCapability.register();
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		PBCommand.register(event.getDispatcher());
	}
}
