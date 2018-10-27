package net.insane96mcp.progressivebosses.lib;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Ignore;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;

@Config(modid = ProgressiveBosses.MOD_ID, category = "", name = "ProgressiveBosses")
public class Properties {
	
	public static final ConfigOptions config = new ConfigOptions();
	
	public static class ConfigOptions {
		public Wither wither = new Wither();
		
		public static class Wither {
			public General general = new General();
			
			public static class General {
				@Name("Spawn Radius Players Check")
				@Comment("How much blocks from wither will be scanned for players to check")
				public int spawnRadiusPlayerCheck = 96;
				@Name("Sum Spawned Wither")
				@Comment("If true and there are more players around the wither, the wither will have his stats based on the sum of both players spawned withers. If false, the wither stats will be based on the average of the spawned wither count of the players around")
				public boolean sumSpawnedWither = false;
				@Name("Max Difficulty")
				@Comment("The Maximum difficulty (times spawned) reachable by Wither. By default is set to 72 because the wither reaches the maximum amount of health (handled by minecraft) after 72 withers spawned")
				public int maxDifficulty = 72;
			}

			
			public Minions minions = new Minions();
			
			public static class Minions {
				@Name("Spawn After")
				@Comment("After how many withers spawned by players, the wither will start spawning wither minions during the fight. Set to -1 to disable this")
				@RangeInt(min = -1, max = Integer.MAX_VALUE)
				public int spawnAfter = 2;
				@Name("Spawn Every")
				@Comment("As the wither starts spawning wither minions, every how much withers spawned the wither will spawn one more minion. Cannot be lower than 1")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnEvery = 4;
				@Name("Spawn Max Count")
				@Comment("Maximum number of wither minions that a Wither can spawn")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMaxCount = 8;
				@Name("Max Minions")
				@Comment("Maximum amount of Wither minions that can be around the Wither in a 24 block radius. After this number is reached the wither will stop spawning minions. Set to 0 to disable this check")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int maxMinions = 16;
				@Name("Spawn Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMinCooldown = 150;
				@Name("Spawn Max Cooldown")
				@Comment("After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMaxCooldown = 300;
				@Name("Min Armor")
				@Comment("Minimum armor value that wither skeletons should spawn with")
				@RangeInt(min = 0, max = 24)
				public int minArmor = 0;
				@Name("Max Armor")
				@Comment("Maximum armor value that wither skeletons should spawn with")
				@RangeInt(min = 0, max = 20)
				public int maxArmor = 10;
			}
		
		
			public Health health = new Health();
			
			public static class Health {
				@Name("Bonus per Spawned")
				@Comment("How much more health the withers will have more for each wither that has been already spawned")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerSpawned = 10f;
				@Name("Maximum Regeneration")
				@Comment("Maximum regeneration for regen_per_spawned. Set to 0 to disable health regeneration. It's not recommended to go over 1.0f without mods that adds stronger items, potions, etc.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float maximumRegeneration = 1.0f;
				@Name("Regeneration per Spawned")
				@Comment("How many half hearts will regen the wither per wither spawned, this doesn't alter the normal health regeneration of the wither (1 hp per second) (E.g. With 6 withers spawned, the wither will heal 0.6 half-hearts more per second).")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float regenPerSpawned = 0.05f;
			}
		
		
			public Armor armor = new Armor();
			
			public static class Armor {
				@Name("Bonus per Spawned")
				@Comment("How much armor points will have withers for each time a wither is spawned")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerSpawned = 0.25f;
				@Name("Maximum")
				@Comment("Maximum armor points that withers can spawn with. It's not recommended to go over 20")
				@RangeDouble(min = 0, max = 24)
				public float maximum = 10f;
			}
			
			
			public Rewards rewards = new Rewards();
			
			public static class Rewards {
				@Name("Bonus % Experience")
				@Comment("How much more percentage experience will wither drop per wither spawned. The percentage is additive (e.g. 10% experience boost, 7 withers spawned = 70% more experience)")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusExperience = 10.0f;
				@Name("Shards per Spawned")
				@Comment("How much chance per wither spawned to get a Nether Star Shard from killing the wither")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float shardPerSpawned = 2f;
				@Name("Shards Max Chance")
				@Comment("Maximum chance to get a Nether Star shard")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float shardMaxChance = 50.0f;
				@Name("Shards Divider")
				@Comment("Divider of killed withers for how many times the game tries to drop one more shard. Given this value x you get ((killed_wither / x) + 1) times to get one or more shard.\nE.g. By default, at 10 withers killed you have 20% chance to drop a shard, another 20% chance to get another one, etc. up to 6 times.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int shardDivider = 2;
				@Name("Shards Max Count")
				@Comment("Maximum amount of shard that you can get from wither")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int shardMaxCount = 8;
			}
		}
		
		
		public Dragon dragon = new Dragon();
		
		public static class Dragon {
			
			public General general = new General();
			
			public static class General {
				@Name("Sum Killed Dragons")
				@Comment("If true and there are more players around the dragon that has spawned, the dragon will have his stats based on the sum of both players killed dragons. If false, the dragon stats will be based on the average of the killed dragons count of the players in End's main island")
				public boolean sumKilledDragons = false;
				@Name("Max Difficulty")
				@Comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 82 because the Ender Dragon reaches the maximum amount of health (handled by minecraft) after 82 Ender dragons killed")
				public int maxDifficulty = 82;
			}
			
			
			public Health health = new Health();
			
			public static class Health {
				@Name("Bonus per Killed")
				@Comment("How much more health will have the ender dragon for each ender dragon that has been killed")
				@RangeDouble(min = 0, max = 1024)
				public float bonusPerKilled = 10f;
				@Name("Maximum Regeneration")
				@Comment("Maximum bonus regeneration for 'Regen per Killed'. Set to 0 to disable bonus health regeneration. It's not recommended to go over 1.0f without mods that adds stronger things to kill the dragon faster")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float maximumRegeneration = 1.0f;
				@Name("Regen per Killed")
				@Comment("How many half hearts will regen the dragon per dragons killed per second (even without crystals) (E.g. By default and with 6 dragons killed, the dragon will heal 0.3 half-hearts per second without crystals).")
				@RangeDouble(min = 0, max = 1024)
				public float regenPerKilled = 0.05f;
			}
			
			
			public Armor armor = new Armor();
			
			public static class Armor {
				@Name("Bonus per Killed")
				@Comment("How much armor points will have ender dragons for each time a dragon is killed")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerKilled = 0.2f;
				@Name("Maximum")
				@Comment("Maximum armor points that enderdragons can spawn with. It's not recommended to go over 10 as the Ender Dragon already has some damage reduction")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float maximum = 5f;
			}
			
			
			public Endermites endermites = new Endermites();
			
			public static class Endermites {
				@Name("Spawn Every")
				@Comment("Every this number of dragons killed, the dragon will spawn one more ultrafast endermites (Dragon's Larvae) at the center island. (by default 1 endermite at 4 killed dragons, 2 endermite at 8 killed dragons, etc.). Setting this to 0 will disable larvae spawn")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int spawnEvery = 4;
				@Name("Spawn Max Amount")
				@Comment("Maximum number of Endermites that the dragon can spawn (e.g. by default values, after the 24th dragon, he will always spawn 6 endermites")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMaxCount = 6;
				@Name("Spawn Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the ender dragon will spawn endermites")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMinCooldown = 600;
				@Name("Spawn Max Cooldown")
				@Comment("After how many maximum ticks (20 ticks = 1 second) the ender dragon will spawn endermites")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMaxCooldown = 1200;
				@Name("Spawn Cooldown Reduction")
				@Comment("For each killed dragon the spawn endermites cooldown min and max will be reduced by this value (E.g. with 10 killed dragons and this set to 5, the spawn endermites cooldown min will be 550 and max 1150)")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnCooldownReduction = 5;
			}
		
		
			public Shulkers shulkers = new Shulkers();
			
			public static class Shulkers {
				@Name("Spawn After")
				@Comment("After how many dragons killed, the dragon will start spawning shulker (Dragon's Minion). Setting this to 0 will disable shulkers spawn")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int spawnAfter = 5;
				@Name("Spawn Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMinCooldown = 900;
				@Name("Spawn Max Cooldown")
				@Comment("After how many maximum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnMaxCooldown = 1200;
				@Name("Spawn Cooldown Reduction")
				@Comment("For each killed dragon the spawn shulkers cooldown min and max will be reduced by this value (E.g. with 10 killed dragons and this set to 10, the spawn shulkers cooldown min will be 800 and max 1100)")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnCooldownReduction = 10;
			}
		
		
			public Rewards rewards = new Rewards();
			
			public static class Rewards {
				@Name("Bonus Experience")
				@Comment("How much more percentage experience will ender dragon drop per dragon killed. The percentage is additive (e.g. 5% experience boost, 7 dragons killed = 35% more experience)")
				@Ignore
				public float bonusExperience = 35.0f;
				@Name("First Dragon per Player")
				@Comment("Should the first Dragon killed per Player always drop the egg? If true means that every player will get the ender dragon egg as reward.")
				public boolean firstDragonPerPlayer = true;
				
			}
		}
	}
}
