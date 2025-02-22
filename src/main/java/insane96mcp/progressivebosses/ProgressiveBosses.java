package insane96mcp.progressivebosses;

import insane96mcp.progressivebosses.commands.PBCommand;
import insane96mcp.progressivebosses.module.dragon.data.DragonStatsReloadListener;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.module.dragon.phase.DragonBlastAttackPhase;
import insane96mcp.progressivebosses.module.dragon.phase.DragonCrystalRespawnPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonHoldingPatternPhase;
import insane96mcp.progressivebosses.module.dragon.phase.PBDragonStrafePlayerPhase;
import insane96mcp.progressivebosses.module.elderguardian.data.ElderGuardianStatsReloadListener;
import insane96mcp.progressivebosses.module.wither.data.WitherStatsReloadListener;
import insane96mcp.progressivebosses.module.wither.dispenser.WitherSkullDispenseBehavior;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.module.wither.entity.minion.WitherMinion;
import insane96mcp.progressivebosses.network.NetworkHandler;
import insane96mcp.progressivebosses.setup.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);
		MinecraftForge.EVENT_BUS.register(this);
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener(ClientSetup::registerEntityRenderers);
		modEventBus.addListener(ClientSetup::creativeTabsBuildContents);
		modEventBus.addListener(this::commonSetup);
		modEventBus.addListener(this::registerAttributes);
		PBItems.REGISTRY.register(modEventBus);
		PBEntities.REGISTRY.register(modEventBus);
		PBBlocks.BLOCKS.register(modEventBus);
		PBBlocks.BLOCK_ENTITY_TYPES.register(modEventBus);
		PBLoot.LOOT_CONDITIONS.register(modEventBus);
		PBLoot.LOOT_FUNCTION.register(modEventBus);

		DragonCrystalRespawnPhase.init();
		PBDragonStrafePlayerPhase.init();
		PBDragonHoldingPatternPhase.init();
		DragonBlastAttackPhase.init();

		DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onAddReloadListener(AddReloadListenerEvent event) {
		event.addListener(DragonStatsReloadListener.INSTANCE);
		event.addListener(WitherStatsReloadListener.INSTANCE);
		event.addListener(ElderGuardianStatsReloadListener.INSTANCE);
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		NetworkHandler.init();
	}

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		PBCommand.register(event.getDispatcher());
	}

	public void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(PBEntities.WITHER.get(), PBWither.prepareAttributes().build());
		event.put(PBEntities.WITHER_MINION.get(), WitherMinion.prepareAttributes().build());
		event.put(PBEntities.LARVA.get(), Larva.prepareAttributes().build());
	}
}
