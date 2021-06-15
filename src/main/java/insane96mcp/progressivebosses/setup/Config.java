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

        public final Larvae larvae;
        public final Crystal crystal;

        public Dragon(final ForgeConfigSpec.Builder builder) {
            builder.push(name);
            larvae = new Larvae(builder);
            crystal = new Crystal(builder);
            builder.pop();
        }

        public static class Larvae {
            public static String name = "Larvae";

            public ConfigValue<Integer> difficultyToSpawnOneMore;
            public ConfigValue<Integer> maxSpawned;
            public ConfigValue<Integer> minCooldown;
            public ConfigValue<Integer> maxCooldown;
            public ConfigValue<Integer> cooldownReduction;

            public Larvae(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                difficultyToSpawnOneMore = builder
                        .comment("As the Ender Dragon Difficulty reaches this value she will be spawning a Larva during the fight. Plus every time Difficulty is a multiplier of this value the Ender Dragon will spawn one more Larva")
                        .defineInRange("Difficulty to Spawn One More Larva", 1, 1, Integer.MAX_VALUE);
                maxSpawned = builder
                        .comment("Maximum number of Larvae that an Ender Dragon can spawn. Set to 0 to disable Dragon's Larvae")
                        .defineInRange("Max Larvae Spawned", 6, 0, Integer.MAX_VALUE);
                minCooldown = builder
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae")
                        .defineInRange("Min Cooldown", 1050, 1, Integer.MAX_VALUE);
                maxCooldown = builder
                        .comment("After how many maximum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae")
                        .defineInRange("Max Cooldown", 1200, 1, Integer.MAX_VALUE);
                cooldownReduction = builder
                        .comment("For each difficulty the Larvae spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 5, the Larvae cooldown min will be 550 and max 1150)")
                        .defineInRange("Cooldown Reduction Per Difficulty", 10, 0, Integer.MAX_VALUE);
                builder.pop();
            }
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
