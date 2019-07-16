package insane96mcp.progressivebosses.setup;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.nio.file.Path;

public class ModConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

    public static void init(Path file) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(file)
                .sync()
                .autosave()
                .writingMode(WritingMode.REPLACE)
                .build();

        configData.load();
        SPEC.setConfig(configData);
    }

    public static class Wither {
        public static String name = "Wither";

        public static class General {
            public static String name = "General";

            public static ConfigValue<Integer> spawnRadiusPlayerCheck;
            public static ConfigValue<Boolean> sumSpawnedWitherDifficulty;
            public static ConfigValue<Integer> maxDifficulty;

            public static void init() {
                BUILDER.push(name);
                spawnRadiusPlayerCheck = BUILDER
                        .comment("How much blocks from wither will be scanned for players to check for difficulty")
                        .defineInRange("Spawn Radius Player Check", 96, 16, Integer.MAX_VALUE);
                sumSpawnedWitherDifficulty = BUILDER
                        .comment("If true and there are more players around the Wither, the Wither will have his stats based on the sum of both players difficulty. If false, the Wither stats will be based on the average of the difficulty of the players around")
                        .define("Sum Spawned Wither Difficulty", false);
                maxDifficulty = BUILDER
                        .comment("The Maximum difficulty (times spawned) reachable by Wither. By default is set to 72 because the Wither reaches the maximum amount of health (1024, handled by Minecraft. Some mods can increase this) after 72 withers spawned.")
                        .defineInRange("Max Difficulty", 72, 1, Integer.MAX_VALUE);
                BUILDER.pop();

            }
        }

        public static class Minions {
            public static String name = "Minions";

            public static ConfigValue<Integer> difficultyToSpawn;
            public static ConfigValue<Integer> difficultyToSpawnOneMore;
            public static ConfigValue<Integer> maxSpawned;
            public static ConfigValue<Integer> maxAround;
            public static ConfigValue<Integer> minCooldown;
            public static ConfigValue<Integer> maxCooldown;
            public static ConfigValue<Integer> baseHealth;
            public static ConfigValue<Integer> healthPerDifficulty;

            public static void init() {
                BUILDER.push(name);
                difficultyToSpawn = BUILDER
                        .comment("Minimum Difficulty required for the Wither will start spawning Wither Minions during the fight")
                        .defineInRange("Difficulty to Spawn Minions", 1, 0, Integer.MAX_VALUE);
                difficultyToSpawnOneMore = BUILDER
                        .comment("As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion")
                        .defineInRange("Difficulty to Spawn One More Minion", 2, 1, Integer.MAX_VALUE);
                maxSpawned = BUILDER
                        .comment("Maximum number of Wither Minions that a Wither can spawn. Set to 0 to disable Wither Minions")
                        .defineInRange("Max Minions Spawned", 8, 0, Integer.MAX_VALUE);
                maxAround = BUILDER
                        .comment("Maximum amount of Wither Minions that can be around the Wither in a 24 block radius. After this number is reached the Wither will stop spawning minions. Set to 0 to disable this check")
                        .defineInRange("Max Minions Around", 16, 1, Integer.MAX_VALUE);
                minCooldown = BUILDER
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Wither will try to spawn Minions. This is reduced by the current percentage health of the Wither, down to a minimum of 25% this value")
                        .defineInRange("Min Cooldown", 200, 1, Integer.MAX_VALUE);
                maxCooldown = BUILDER
                        .comment("After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn Minions")
                        .defineInRange("Max Cooldown", 400, 1, Integer.MAX_VALUE);
                baseHealth = BUILDER
                        .comment("Health with which Minions spawn at Difficulty = 0")
                        .defineInRange("Base Health", 20, 0, 1024);
                healthPerDifficulty = BUILDER
                        .comment("Health Points Minions gain per difficulty")
                        .defineInRange("Health Per Difficulty", 1, 0, 1024);
                BUILDER.pop();
            }
        }

        public static class Health {
            public static String name = "Health";

            public static ConfigValue<Double> bonusPerDifficulty;
            public static ConfigValue<Double> maximumBonusRegen;
            public static ConfigValue<Double> bonusRegenPerSpawned;

            public static void init() {
                BUILDER.push(name);
                bonusPerDifficulty = BUILDER
                        .comment("Increase Wither's Health by this value per difficulty")
                        .defineInRange("Health Bonus per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                maximumBonusRegen = BUILDER
                        .comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second). It's not recommended to go over 1.0f without mods that adds stronger things to kill the Wither")
                        .defineInRange("Maximum Bonus Regeneration", 1.0, 0.0, Double.MAX_VALUE);
                bonusRegenPerSpawned = BUILDER
                        .comment("How many half hearts will the Wither regen more per difficulty. This doesn't affect the natural regeneration of the Wither (1 Health per Second). (E.g. By default, with 6 Withers spawned, the Wither will heal 1.3 health per second).")
                        .defineInRange("Bonus Regeneration per Difficulty", 0.05, 0.0, Double.MAX_VALUE);
                BUILDER.pop();
            }
        }

        public static class Armor {
            public static String name = "Armor";

            public static ConfigValue<Double> bonusPerDifficulty;
            public static ConfigValue<Double> maximum;

            public static void init() {
                BUILDER.push(name);
                bonusPerDifficulty = BUILDER
                        .comment("How much armor points will have Withers per Difficulty")
                        .defineInRange("Bonus Armor per Difficulty", 0.25, 0.0, 30.0);
                maximum = BUILDER
                        .comment("Maximum armor that Withers can spawn with")
                        .defineInRange("Maximum Armor", 10.0, 0.0, 30.0);
                BUILDER.pop();
            }
        }

        public static class Rewards {
            public static String name = "Rewards";

            public static ConfigValue<Double> bonusExperience;
            public static ConfigValue<Double> shardPerDifficulty;
            public static ConfigValue<Double> shardMaxChance;
            public static ConfigValue<Integer> shardDivider;
            public static ConfigValue<Integer> shardMaxCount;

            public static void init() {
                BUILDER.push(name);
                bonusExperience = BUILDER
                        .comment("How much more experience (percentage) will Wither drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 withers spawned = 70% more experience)")
                        .defineInRange("Bonus Experience per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                shardPerDifficulty = BUILDER
                        .comment("How much chance per Difficulty to get a Nether Star Shard from killing the Wither")
                        .defineInRange("Shards per Difficulty", 2.0, 0.0, 100.0);
                shardMaxChance = BUILDER
                        .comment("Maximum chance to get a Nether Star shard.")
                        .defineInRange("Shards Max Chance", 50.0, 0.0, 100.0);
                shardDivider = BUILDER
                        .comment("Difficulty Divider for how many times the game tries to drop one more shard. Given this value x you get ((difficulty / x) + 1) times to get one or more shard.\nE.g. By default, at 10 withers killed you have 6 times 20% chance to drop a shard.")
                        .defineInRange("Shards Divider", 2, 1, Integer.MAX_VALUE);
                shardMaxCount = BUILDER
                        .comment("Maximum amount of shards that you can get from a Wither.")
                        .defineInRange("Shards Max Count", 8, 0, Integer.MAX_VALUE);
                BUILDER.pop();
            }
        }

        public static void init() {
            BUILDER.push(name);
            General.init();
            Minions.init();
            Health.init();
            Armor.init();
            Rewards.init();
            BUILDER.pop();
        }
    }

    public static class Dragon {
        public static String name = "Dragon";

        public static class General {
            public static String name = "General";

            public static ConfigValue<Boolean> sumKilledDragonsDifficulty;
            public static ConfigValue<Integer> maxDifficulty;

            public static void init() {
                BUILDER.push(name);
                sumKilledDragonsDifficulty = BUILDER
                        .comment("If true and there are more players around the Dragon, she will have his stats based on the sum of both players' difficulty. If false, the Dragon stats will be based on the average of the difficulty of the players around.")
                        .define("Sum Killed Dragons Difficulty", false);
                maxDifficulty = BUILDER
                        .comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 82 because the Ender Dragon reaches the maximum amount of health (1024, handled by Minecraft. Some mods can increase this) after 82 Dragons killed.")
                        .defineInRange("Max Difficulty", 82, 1, Integer.MAX_VALUE);
                BUILDER.pop();

            }
        }

        public static class Health {
            public static String name = "Health";

            public static ConfigValue<Double> bonusPerDifficulty;
            public static ConfigValue<Double> maximumBonusRegen;
            public static ConfigValue<Double> bonusRegenPerSpawned;

            public static void init() {
                BUILDER.push(name);
                bonusPerDifficulty = BUILDER
                        .comment("Increase Ender Dragon's Health by this value per difficulty")
                        .defineInRange("Health Bonus per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                maximumBonusRegen = BUILDER
                        .comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the regeneration of the Ender Dragon from Crystals. It's not recommended to go over 1.0f without mods that adds stronger things to kill the Ender Dragon")
                        .defineInRange("Maximum Bonus Regeneration", 0.5, 0.0, Double.MAX_VALUE);
                bonusRegenPerSpawned = BUILDER
                        .comment("How many half hearts will the Ender Dragon regen per difficulty. This doesn't affect the regeneration of the Ender Dragon from Crystals. (E.g. With 6 Dragons killed, the Dragon will heal 0.6 health per second without Crystals).")
                        .defineInRange("Bonus Regeneration per Difficulty", 0.025, 0.0, Double.MAX_VALUE);
                BUILDER.pop();
            }
        }

        public static class Larvae {
            public static String name = "Larvae";

            public static ConfigValue<Integer> difficultyToSpawnOneMore;
            public static ConfigValue<Integer> maxSpawned;
            public static ConfigValue<Integer> minCooldown;
            public static ConfigValue<Integer> maxCooldown;
            public static ConfigValue<Integer> cooldownReduction;

            public static void init() {
                BUILDER.push(name);
                difficultyToSpawnOneMore = BUILDER
                        .comment("As the Ender Dragon Difficulty reaches this value she will be spawning a Larva during the fight. Plus every time Difficulty is a multiplier of this value the Ender Dragon will spawn one more Larva")
                        .defineInRange("Difficulty to Spawn One More Larva", 1, 1, Integer.MAX_VALUE);
                maxSpawned = BUILDER
                        .comment("Maximum number of Larvae that an Ender Dragon can spawn. Set to 0 to disable Dragon's Larvae")
                        .defineInRange("Max Larvae Spawned", 6, 0, Integer.MAX_VALUE);
                minCooldown = BUILDER
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae")
                        .defineInRange("Min Cooldown", 1050, 1, Integer.MAX_VALUE);
                maxCooldown = BUILDER
                        .comment("After how many maximum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae")
                        .defineInRange("Max Cooldown", 1200, 1, Integer.MAX_VALUE);
                cooldownReduction = BUILDER
                        .comment("For each difficulty the Larvae spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 5, the Larvae cooldown min will be 550 and max 1150)")
                        .defineInRange("Cooldown Reduction Per Difficulty", 10, 0, Integer.MAX_VALUE);
                BUILDER.pop();
            }
        }

        public static class Minion {
            public static String name = "Minion";

            public static ConfigValue<Integer> difficultyToSpawn;
            public static ConfigValue<Integer> minCooldown;
            public static ConfigValue<Integer> maxCooldown;
            public static ConfigValue<Integer> cooldownReduction;

            public static void init() {
                BUILDER.push(name);
                difficultyToSpawn = BUILDER
                        .comment("Minimum Difficulty required for the Ender Dragon to start spawning Dragon's Minions during the fight. Set to -1 to disable Dragon's Minions spawning")
                        .defineInRange("Difficulty to Spawn Minions", 1, 0, Integer.MAX_VALUE);
                minCooldown = BUILDER
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion")
                        .defineInRange("Min Cooldown", 1200, 1, Integer.MAX_VALUE);
                maxCooldown = BUILDER
                        .comment("After how many maximum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion")
                        .defineInRange("Max Cooldown", 1800, 1, Integer.MAX_VALUE);
                cooldownReduction = BUILDER
                        .comment("For each difficulty the Minion spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 10, the Minion cooldown min will be 1100 and max 1700)")
                        .defineInRange("Cooldown Reduction Per Difficulty", 10, 0, Integer.MAX_VALUE);
                BUILDER.pop();
            }
        }

        public static class Rewards {
            public static String name = "Rewards";

            public static ConfigValue<Double> bonusExperience;
            public static ConfigValue<Boolean> firstDragonPerPlayer;

            public static void init() {
                BUILDER.push(name);
                bonusExperience = BUILDER
                        .comment("How much more experience (percentage) will Ender Dragon drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 Ender Dragons killed = 70% more experience)")
                        .defineInRange("Bonus Experience per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                firstDragonPerPlayer = BUILDER
                        .comment("Should the first Dragon killed per Player always drop the egg and the first Dragon experience (12k instead of 500)? If true means that every player will get the Dragon Egg as they kill their first Dragon (yes even when 2 players kill the dragon)")
                        .define("First Dragon per Player", true);
                BUILDER.pop();
            }
        }

        public static void init() {
            BUILDER.push(name);
            General.init();
            Health.init();
            Larvae.init();
            Minion.init();
            Rewards.init();
            BUILDER.pop();
        }
    }

    static {
        Wither.init();
        Dragon.init();

        SPEC = BUILDER.build();
    }
}
