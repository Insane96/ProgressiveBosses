package insane96mcp.progressivebosses;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.insanelib.util.IntegratedPack;
import insane96mcp.progressivebosses.commands.PBCommand;
import insane96mcp.progressivebosses.data.ComponentRegistry;
import insane96mcp.progressivebosses.module.PBModules;
import insane96mcp.progressivebosses.module.dragon.data.DragonDefinitionReloadListener;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import org.slf4j.Logger;

import java.util.function.BooleanSupplier;

@Mod(ProgressiveBosses.MOD_ID)
public class ProgressiveBosses {
    public static final String MOD_ID = "progressivebosses";
    @Deprecated
    public static final String RESOURCE_PREFIX = MOD_ID + ":";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;

    public ProgressiveBosses(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, PBModules::init, ProgressiveBosses.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);
        eventBus.addListener(ClientSetup::registerEntityRenderers);
        eventBus.addListener(ClientSetup::creativeTabsBuildContents);
        eventBus.addListener(NetworkHandler::register);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::registerAttributes);
        PBItems.REGISTRY.register(eventBus);
        PBEntities.REGISTRY.register(eventBus);
        PBBlocks.BLOCKS.register(eventBus);
        PBBlocks.BLOCK_ENTITY_TYPES.register(eventBus);
        PBLoot.LOOT_CONDITIONS.register(eventBus);
        PBLoot.LOOT_FUNCTION.register(eventBus);

        DragonCrystalRespawnPhase.init();
        PBDragonStrafePlayerPhase.init();
        PBDragonHoldingPatternPhase.init();
        DragonBlastAttackPhase.init();

        DispenserBlock.registerBehavior(Items.WITHER_SKELETON_SKULL, new WitherSkullDispenseBehavior());

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> PBCommand.register(event.getDispatcher()));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(DragonDefinitionReloadListener.INSTANCE);
        event.addListener(WitherStatsReloadListener.INSTANCE);
        event.addListener(ElderGuardianStatsReloadListener.INSTANCE);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ComponentRegistry.init();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        PBCommand.register(event.getDispatcher());
    }

    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(PBEntities.WITHER.get(), PBWither.prepareAttributes().build());
        event.put(PBEntities.WITHER_MINION.get(), WitherMinion.prepareAttributes().build());
    }

    public static void addClientPack(String path, String description, BooleanSupplier enabled) {
        IntegratedPack.addClientPack(MOD_ID, path, description, enabled);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
