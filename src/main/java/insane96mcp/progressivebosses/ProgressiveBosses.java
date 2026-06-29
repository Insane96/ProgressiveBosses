package insane96mcp.progressivebosses;

import com.mojang.logging.LogUtils;
import insane96mcp.insanelib.setup.ILModConfig;
import insane96mcp.progressivebosses.commands.PBCommand;
import insane96mcp.progressivebosses.module.PBModules;
import insane96mcp.progressivebosses.setup.PBLoot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.slf4j.Logger;

@Mod(ProgressiveBosses.MOD_ID)
public class ProgressiveBosses {
    public static final String MOD_ID = "progressivebosses";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ILModConfig CONFIG;

    public ProgressiveBosses(IEventBus eventBus, ModContainer modContainer) {
        CONFIG = new ILModConfig(MOD_ID, ModConfig.Type.COMMON, eventBus, PBModules::init, ProgressiveBosses.class.getClassLoader());
        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG.spec);

        PBLoot.LOOT_CONDITIONS.register(eventBus);
        PBLoot.LOOT_FUNCTIONS.register(eventBus);

        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> PBCommand.register(event.getDispatcher()));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String lang(String path) {
        return MOD_ID + "." + path;
    }
}
