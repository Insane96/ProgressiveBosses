package insane96mcp.progressivebosses;

import insane96mcp.progressivebosses.commands.Command;
import insane96mcp.progressivebosses.proxy.ClientProxy;
import insane96mcp.progressivebosses.proxy.IProxy;
import insane96mcp.progressivebosses.proxy.ServerProxy;
import insane96mcp.progressivebosses.setup.ModConfig;
import insane96mcp.progressivebosses.setup.Reflection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;

@Mod("progressivebosses")
public class ProgressiveBosses {

    public static final String MOD_ID = "progressivebosses";
    public static final String RESOURCE_PREFIX = MOD_ID + ":";

    public static IProxy proxy = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());

    public static final Logger LOGGER = LogManager.getLogger();

    public ProgressiveBosses() {

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {

        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC);
        ModConfig.init(Paths.get("config", MOD_ID + ".toml"));
        Reflection.init();

        proxy.init();
    }

    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        Command.register(event.getCommandDispatcher());
    }
}
