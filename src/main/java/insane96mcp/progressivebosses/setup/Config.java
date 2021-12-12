package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    public static final ForgeConfigSpec.Builder builder;

    static {
        builder = new ForgeConfigSpec.Builder();
        final Pair<CommonConfig, ForgeConfigSpec> specPair = builder.configure(CommonConfig::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    public static class CommonConfig {
        public CommonConfig(final ForgeConfigSpec.Builder builder) {
            Modules.init();
        }
    }

    @SubscribeEvent
    public static void onModConfigEvent(final ModConfigEvent event) {
        Modules.loadConfig();
    }
}
