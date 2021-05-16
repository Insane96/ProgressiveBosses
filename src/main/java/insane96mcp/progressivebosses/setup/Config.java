package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.base.Modules;
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

    public static class Wither {
        public static String name = "Wither";

        public final Minions minions;
        public final Health health;
        public final Armor armor;
        public final Rewards rewards;

        public Wither(final ForgeConfigSpec.Builder builder) {
            builder.push(name);
            minions = new Minions(builder);
            health = new Health(builder);
            armor = new Armor(builder);
            rewards = new Rewards(builder);
            builder.pop();
        }

        public static class Minions {
            public static String name = "Minions";

            public ConfigValue<Integer> difficultyToSpawn;
            public ConfigValue<Integer> difficultyToSpawnOneMore;
            public ConfigValue<Integer> maxSpawned;
            public ConfigValue<Integer> maxAround;
            public ConfigValue<Integer> minCooldown;
            public ConfigValue<Integer> maxCooldown;
            public ConfigValue<Integer> minHealth;
            public ConfigValue<Integer> maxHealth;

            public Minions(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                difficultyToSpawn = builder
                        .comment("Minimum Difficulty required for the Wither will start spawning Wither Minions during the fight")
                        .defineInRange("Difficulty to Spawn Minions", 1, 0, Integer.MAX_VALUE);
                difficultyToSpawnOneMore = builder
                        .comment("As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion")
                        .defineInRange("Difficulty to Spawn One More Minion", 2, 1, Integer.MAX_VALUE);
                maxSpawned = builder
                        .comment("Maximum number of Wither Minions that a Wither can spawn. Set to 0 to disable Wither Minions")
                        .defineInRange("Max Minions Spawned", 8, 0, Integer.MAX_VALUE);
                maxAround = builder
                        .comment("Maximum amount of Wither Minions that can be around the Wither in a 24 block radius. After this number is reached the Wither will stop spawning minions. Set to 0 to disable this check")
                        .defineInRange("Max Minions Around", 16, 1, Integer.MAX_VALUE);
                minCooldown = builder
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Wither will try to spawn Minions. This is reduced by the current percentage health of the Wither, down to a minimum of 25% this value")
                        .defineInRange("Min Cooldown", 200, 1, Integer.MAX_VALUE);
                maxCooldown = builder
                        .comment("After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn Minions")
                        .defineInRange("Max Cooldown", 400, 1, Integer.MAX_VALUE);
                minHealth = builder
                        .comment("Minimum Health with which Wither Minions can spawn with")
                        .defineInRange("Min Health", 10, 0, 1024);
                maxHealth = builder
                        .comment("Minimum Health with which Wither Minions can spawn with")
                        .defineInRange("Max Health", 20, 0, 1024);
                builder.pop();
            }
        }

        public static class Health {
            public static String name = "Health";

            public ConfigValue<Double> bonusPerDifficulty;
            public ConfigValue<Double> maximumBonusRegen;
            public ConfigValue<Double> bonusRegenPerSpawned;

            public Health(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                bonusPerDifficulty = builder
                        .comment("Increase Wither's Health by this value per difficulty")
                        .defineInRange("Health Bonus per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                maximumBonusRegen = builder
                        .comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second). It's not recommended to go over 1.0f without mods that adds stronger things to kill the Wither")
                        .defineInRange("Maximum Bonus Regeneration", 1.0, 0.0, Double.MAX_VALUE);
                bonusRegenPerSpawned = builder
                        .comment("How many half hearts will the Wither regen more per difficulty. This doesn't affect the natural regeneration of the Wither (1 Health per Second). (E.g. By default, with 6 Withers spawned, the Wither will heal 1.3 health per second).")
                        .defineInRange("Bonus Regeneration per Difficulty", 0.05, 0.0, Double.MAX_VALUE);
                builder.pop();
            }
        }

        //TODO Rework and add Toughness too
        public static class Armor {
            public static String name = "Armor";

            public ConfigValue<Double> bonusPerDifficulty;
            public ConfigValue<Double> maximum;

            public Armor(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                bonusPerDifficulty = builder
                        .comment("How much armor points will have Withers per Difficulty")
                        .defineInRange("Bonus Armor per Difficulty", 0.25, 0.0, 30.0);
                maximum = builder
                        .comment("Maximum armor that Withers can spawn with")
                        .defineInRange("Maximum Armor", 10.0, 0.0, 30.0);
                builder.pop();
            }
        }

        public static class Rewards {
            public static String name = "Rewards";

            public ConfigValue<Double> bonusExperience;
            public ConfigValue<Double> shardPerDifficulty;
            public ConfigValue<Double> shardMaxChance;
            public ConfigValue<Integer> shardDivider;
            public ConfigValue<Integer> shardMaxCount;

            public Rewards(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                bonusExperience = builder
                        .comment("How much more experience (percentage) will Wither drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 withers spawned = 70% more experience)")
                        .defineInRange("Bonus Experience per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                shardPerDifficulty = builder
                        .comment("How much chance per Difficulty to get a Nether Star Shard from killing the Wither")
                        .defineInRange("Shards per Difficulty", 2.0, 0.0, 100.0);
                shardMaxChance = builder
                        .comment("Maximum chance to get a Nether Star shard.")
                        .defineInRange("Shards Max Chance", 50.0, 0.0, 100.0);
                shardDivider = builder
                        .comment("Difficulty Divider for how many times the game tries to drop one more shard. Given this value x you get ((difficulty / x) + 1) times to get one or more shard.\nE.g. By default, at 10 withers killed you have 6 times 20% chance to drop a shard.")
                        .defineInRange("Shards Divider", 2, 1, Integer.MAX_VALUE);
                shardMaxCount = builder
                        .comment("Maximum amount of shards that you can get from a Wither.")
                        .defineInRange("Shards Max Count", 8, 0, Integer.MAX_VALUE);
                builder.pop();
            }
        }
    }

    public static class Dragon {
        public static String name = "Dragon";

        public final General general;
        public final Health health;
        public final Larvae larvae;
        public final Minions minions;
        public final Rewards rewards;
        public final Attack attack;
        public final Crystal crystal;

        public Dragon(final ForgeConfigSpec.Builder builder) {
            builder.push(name);
            general = new General(builder);
            health = new Health(builder);
            larvae = new Larvae(builder);
            minions = new Minions(builder);
            rewards = new Rewards(builder);
            attack = new Attack(builder);
            crystal = new Crystal(builder);
            builder.pop();
        }

        public static class General {
            public static String name = "General";

            public ConfigValue<Boolean> sumKilledDragonsDifficulty;
            public ConfigValue<Integer> maxDifficulty;

            public General(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                sumKilledDragonsDifficulty = builder
                        .comment("If true and there are more players around the Dragon, she will have his stats based on the sum of both players' difficulty. If false, the Dragon stats will be based on the average of the difficulty of the players around.")
                        .define("Sum Killed Dragons Difficulty", false);
                maxDifficulty = builder
                        .comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 82 because the Ender Dragon reaches the maximum amount of health (1024, handled by Minecraft. Some mods can increase this) after 82 Dragons killed.")
                        .defineInRange("Max Difficulty", 82, 1, Integer.MAX_VALUE);
                builder.pop();
            }
        }

        public static class Health {
            public static String name = "Health";

            public ConfigValue<Double> bonusPerDifficulty;
            public ConfigValue<Double> maximumBonusRegen;
            public ConfigValue<Double> bonusRegenPerSpawned;

            public Health(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                bonusPerDifficulty = builder
                        .comment("Increase Ender Dragon's Health by this value per difficulty")
                        .defineInRange("Health Bonus per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                maximumBonusRegen = builder
                        .comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the regeneration of the Ender Dragon from Crystals. It's not recommended to go over 1.0f without mods that adds stronger things to kill the Ender Dragon")
                        .defineInRange("Maximum Bonus Regeneration", 0.5, 0.0, Double.MAX_VALUE);
                bonusRegenPerSpawned = builder
                        .comment("How many half hearts will the Ender Dragon regen per difficulty. This doesn't affect the regeneration of the Ender Dragon from Crystals. (E.g. With 6 Dragons killed, the Dragon will heal 0.6 health per second without Crystals).")
                        .defineInRange("Bonus Regeneration per Difficulty", 0.025, 0.0, Double.MAX_VALUE);
                builder.pop();
            }
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

        public static class Minions {
            public static String name = "Minion";

            public ConfigValue<Integer> difficultyToSpawn;
            public ConfigValue<Integer> minCooldown;
            public ConfigValue<Integer> maxCooldown;
            public ConfigValue<Integer> cooldownReduction;

            public Minions(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                difficultyToSpawn = builder
                        .comment("Minimum Difficulty required for the Ender Dragon to start spawning Dragon's Minions during the fight. Set to -1 to disable Dragon's Minions spawning")
                        .defineInRange("Difficulty to Spawn Minions", 1, 0, Integer.MAX_VALUE);
                minCooldown = builder
                        .comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion")
                        .defineInRange("Min Cooldown", 1200, 1, Integer.MAX_VALUE);
                maxCooldown = builder
                        .comment("After how many maximum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion")
                        .defineInRange("Max Cooldown", 1800, 1, Integer.MAX_VALUE);
                cooldownReduction = builder
                        .comment("For each difficulty the Minion spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 10, the Minion cooldown min will be 1100 and max 1700)")
                        .defineInRange("Cooldown Reduction Per Difficulty", 10, 0, Integer.MAX_VALUE);
                builder.pop();
            }
        }

        public static class Rewards {
            public static String name = "Rewards";

            public ConfigValue<Double> bonusExperience;
            public ConfigValue<Boolean> firstDragonPerPlayer;

            public Rewards(final ForgeConfigSpec.Builder builder) {
                builder.push(name);
                bonusExperience = builder
                        .comment("How much more experience (percentage) will Ender Dragon drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 Ender Dragons killed = 70% more experience)")
                        .defineInRange("Bonus Experience per Difficulty", 10.0, 0.0, Double.MAX_VALUE);
                firstDragonPerPlayer = builder
                        .comment("Should the first Dragon killed per Player always drop the egg and the first Dragon experience (12k instead of 500)? If true means that every player will get the Dragon Egg as they kill their first Dragon (yes even when 2 players kill the dragon)")
                        .define("First Dragon per Player", true);
                builder.pop();
            }
        }

        public static class Attack {
            public static String name = "Attack";

            public ConfigValue<Double> bonusAttackDamage;
            public ConfigValue<Double> bonusAcidPoolDamage;
            public ConfigValue<Double> chargePlayerMaxChance;
            public ConfigValue<Double> fireballMaxChance;
            public ConfigValue<Double> maxChanceAtDifficulty;

            public Attack(final ForgeConfigSpec.Builder builder){
                builder.push(name);

                bonusAttackDamage = builder
                        .comment("How much more percentage damage does the Ender Dragon deal per difficulty? Setting to 0 will disable this feature.")
                        .defineInRange("Bonus Attack Damage", 10.0, 0.0, Double.MAX_VALUE);
                bonusAcidPoolDamage = builder
                        .comment("How much more percentage damage does the Ender Dragon's Acid Pool per difficulty? Setting to 0 will disable this feature.\nRemember that in vanilla the Acid Pool is magic damage and only Protection enchantment can reduce it.")
                        .defineInRange("Bonus Acid Pool Damage", 10.0, 0.0, Double.MAX_VALUE);
                chargePlayerMaxChance = builder
                        .comment("Normally the Ender Dragon attacks only when leaving the center platform. With this active she has a chance each tick (1/20th of second) when roaming around to attack the player.\\nThis defines the chance to attack the player each tick when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher. The actual formula is\\n(this_value% / 'Max Chance at Difficulty') * difficulty * (1 / remaining_crystals).\\nSetting to 0 will disable this feature.")
                        .defineInRange("Charge Player Max Chance", 1, 0.0, Double.MAX_VALUE);
                fireballMaxChance = builder
                        .comment("Normally the Ender Dragon spits fireballs when a Crystal is destroyed and rarely during the fight. With this active she has a chance each tick (1/20th of second) when roaming around to spit a fireball.\\nThis defines the chance to attack the player each tick when all the crystals were destoyed and the difficulty is 'Max Chance at Difficulty' or higher. The actual formula is\\n(this_value% / 'Max Chance at Difficulty') * difficulty * (1 / remaining_crystals).\\nSetting to 0 will disable this feature.")
                        .defineInRange("Fireball Max Chance", 1.5, 0.0, Double.MAX_VALUE);
                maxChanceAtDifficulty = builder
                        .comment("Defines at which difficulty the Dragon has max chance to attack or spit fireballs when all crystals are destroyed (see 'Fireball Max Chance' and 'Charge Player Max Chance')")
                        .defineInRange("Max Chance at Difficulty", 16, 0.0, Double.MAX_VALUE);
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
