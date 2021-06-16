package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.Modules;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
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
    public static void onModConfigEvent(final net.minecraftforge.fml.config.ModConfig.ModConfigEvent event) {
        Modules.loadConfig();
    }

    public static class Dragon {
        public static String name = "Dragon";

        public final Crystal crystal;

        public Dragon(final ForgeConfigSpec.Builder builder) {
            builder.push(name);
            crystal = new Crystal(builder);
            builder.pop();
        }

        public static class Crystal {
            public static String name = "Crystal";

            public ConfigValue<Integer> moreCagesAtDifficulty;
            public ConfigValue<Integer> moreCrystalsAtDifficulty;

            public Crystal(final ForgeConfigSpec.Builder builder){
                builder.push(name);

                moreCagesAtDifficulty = builder
                        .comment("At this difficulty cages will start to appear around other crystals too, starting from the lowest ones. -1 will disable this feature.")
                        .defineInRange("More Cages at Difficulty", 1, -1, Integer.MAX_VALUE);
                moreCrystalsAtDifficulty = builder
                        .comment("At this difficulty more crystals will start to appear inside obsidian towers, starting from the lowest ones. -1 will disable this feature.")
                        .defineInRange("More Crystals at Difficulty", 8, -1, Integer.MAX_VALUE);
                builder.pop();
            }
        }
    }
}
