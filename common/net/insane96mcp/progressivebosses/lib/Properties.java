package net.insane96mcp.progressivebosses.lib;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.Config.Name;
import net.minecraftforge.common.config.Config.RangeDouble;
import net.minecraftforge.common.config.Config.RangeInt;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ProgressiveBosses.MOD_ID, category = "", name = "ProgressiveBosses")
public class Properties {
	
	public static final ConfigOptions config = new ConfigOptions();
	
	public static class ConfigOptions {
		@Name("Wither")
		@Comment("Difficulty is the amount of Withers Spawned")
		public Wither wither = new Wither();
		
		public static class Wither {
			public General general = new General();
			
			public static class General {
				@Name("Spawn Radius Players Check")
				@Comment("How much blocks from wither will be scanned for players to check for difficulty.")
				public int spawnRadiusPlayerCheck = 96;
				@Name("Sum Spawned Wither Difficulty")
				@Comment("If true and there are more players around the Wither, the Wither will have his stats based on the sum of both players difficulty. If false, the Wither stats will be based on the average of the difficulty of the players around.")
				public boolean sumSpawnedWitherDifficulty = false;
				@Name("Max Difficulty")
				@Comment("The Maximum difficulty (times spawned) reachable by Wither. By default is set to 72 because the Wither reaches the maximum amount of health (1024, handled by Minecraft. Some mods can increase this) after 72 withers spawned.")
				public int maxDifficulty = 72;
			}

			
			public WitherMinions minions = new WitherMinions();
			
			public static class WitherMinions {
				@Name("Difficulty to Spawn Minions")
				@Comment("Minimum Difficulty required for the Wither will start spawning Wither Minions during the fight.")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int difficultyToSpawn = 2;
				@Name("Difficulty to Spawn One More Minion")
				@Comment("As the Wither starts spawning Minions, every how much difficulty the Wither will spawn one more Minion.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int difficultyToSpawnOneMore = 4;
				@Name("Max Minions Spawned")
				@Comment("Maximum number of Wither Minions that a Wither can spawn. Set to 0 to disable Wither Minions.")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int maxSpawned = 8;
				@Name("Max Minions Around")
				@Comment("Maximum amount of Wither Minions that can be around the Wither in a 24 block radius. After this number is reached the Wither will stop spawning minions. Set to 0 to disable this check.")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int maxAround = 16;
				@Name("Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the Wither will try to spawn Minions")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int minCooldown = 150;
				@Name("Max Cooldown")
				@Comment("After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn Minions")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int maxCooldown = 300;
				@Name("Min Armor")
				@Comment("Minimum armor value that Wither Minions should spawn with")
				@RangeInt(min = 0, max = 24)
				public int minArmor = 0;
				@Name("Max Armor")
				@Comment("Maximum armor value that Wither Minions should spawn with")
				@RangeInt(min = 0, max = 20)
				public int maxArmor = 10;
			}
		
		
			public Health health = new Health();
			
			public static class Health {
				@Name("Health Bonus per Difficulty")
				@Comment("Increase Wither's Health by this value per difficulty.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerDifficulty = 10f;
				@Name("Maximum Bonus Regeneration")
				@Comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the natural regeneration of the Wither (1 Health per Second). It's not recommended to go over 1.0f without mods that adds stronger things to kill the Wither.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float maximumBonusRegen = 1.0f;
				@Name("Bonus Regeneration per Difficulty")
				@Comment("How many half hearts will the Wither regen per difficulty. This doesn't affect the natural regeneration of the Wither (1 Health per Second). (E.g. With 6 Withers spawned, the Wither will heal 1.6 health per second).")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusRegenPerSpawned = 0.05f;
			}
		
		
			public Armor armor = new Armor();
			
			public static class Armor {
				@Name("Bonus Armor per Difficulty")
				@Comment("How much armor points will have Withers per Difficulty.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerDifficulty = 0.25f;
				@Name("Maximum Armor")
				@Comment("Maximum armor that Withers can spawn with. It's not recommended to go over 20.")
				@RangeDouble(min = 0, max = 30)
				public float maximum = 10f;
			}
			
			
			public Rewards rewards = new Rewards();
			
			public static class Rewards {
				@Name("Bonus Experience per Difficulty")
				@Comment("How much more experience (percentage) will Wither drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 withers spawned = 70% more experience)")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusExperience = 10.0f;
				@Name("Shards per Difficulty")
				@Comment("How much chance per Difficulty to get a Nether Star Shard from killing the wither")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float shardPerDifficulty = 2f;
				@Name("Shards Max Chance")
				@Comment("Maximum chance to get a Nether Star shard.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float shardMaxChance = 50.0f;
				@Name("Shards Divider")
				@Comment("Difficulty Divider for how many times the game tries to drop one more shard. Given this value x you get ((difficulty / x) + 1) times to get one or more shard.\nE.g. By default, at 10 withers killed you have 6 times 20% chance to drop a shard.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int shardDivider = 2;
				@Name("Shards Max Count")
				@Comment("Maximum amount of shards that you can get from a Wither")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int shardMaxCount = 8;
			}
		}
		

		@Name("Ender Dragon")
		@Comment("Difficulty is the amount of Ender Dragons Killed")
		public Dragon dragon = new Dragon();
		
		public static class Dragon {
			
			public General general = new General();
			
			public static class General {
				@Name("Sum Killed Dragons Difficulty")
				@Comment("If true and there are more players around the Dragon, she will have his stats based on the sum of both players' difficulty. If false, the Dragon stats will be based on the average of the difficulty of the players around.")
				public boolean sumKilledDragonsDifficulty = false;
				@Name("Max Difficulty")
				@Comment("The Maximum difficulty (times killed) reachable by Ender Dragon. By default is set to 82 because the Ender Dragon reaches the maximum amount of health (1024, handled by Minecraft. Some mods can increase this) after 82 Dragons killed.")
				public int maxDifficulty = 82;
			}
			
			
			public Health health = new Health();
			
			public static class Health {
				@Name("Health Bonus per Difficulty")
				@Comment("Increase Ender Dragon's Health by this value per difficulty.")
				@RangeDouble(min = 0, max = 1024)
				public float bonusPerDifficulty = 10f;
				@Name("Maximum Bonus Regeneration")
				@Comment("Maximum bonus regeneration per second given by \"Bonus Regeneration per Difficulty\". Set to 0 to disable bonus health regeneration. This doesn't affect the regeneration of the Ender Dragon from Crystals. It's not recommended to go over 1.0f without mods that adds stronger things to kill the Ender Dragon.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float maximumBonusRegen = 0.5f;
				@Name("Bonus Regeneration per Difficulty")
				@Comment("How many half hearts will the Ender Dragon regen per difficulty. This doesn't affect the regeneration of the Ender Dragon from Crystals. (E.g. With 6 Dragons killed, the Dragon will heal 0.6 health per second without Crystals).")
				@RangeDouble(min = 0, max = 1024)
				public float bonusRegenPerSpawned = 0.025f;
			}
			
			
			public Armor armor = new Armor();
			
			public static class Armor {
				@Name("Bonus Armor per Difficulty")
				@Comment("How much armor points will have Ender Dragons per Difficulty.")
				@RangeDouble(min = 0, max = Float.MAX_VALUE)
				public float bonusPerDifficulty = 0.25f;
				@Name("Maximum Armor")
				@Comment("Maximum armor points that Ender Dragons can spawn with. It's not recommended to go over 10 as the Ender Dragon already has some damage reduction")
				@RangeDouble(min = 0, max = 30)
				public float maximum = 2.5f;
			}

			
			public Larvae larvae = new Larvae();
			
			public static class Larvae {
				@Name("Difficulty to Spawn One More Larva")
				@Comment("As the Ender Dragon Difficulty reaches this value she will be spawning a Larva during the fight. Plus every time Difficulty is a multiplier of this value the Ender Dragon will spawn one more Larva.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int difficultyToSpawnOneMore = 1;
				@Name("Max Larvae Spawned")
				@Comment("Maximum number of Larvae that an Ender Dragon can spawn. Set to 0 to disable Dragon's Larvae.")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int maxSpawned = 6;
				@Name("Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int minCooldown = 1050;
				@Name("Max Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn Larvae.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int maxCooldown = 1200;
				@Name("Cooldown Reduction per Difficulty")
				@Comment("For each difficulty the Larvae spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 5, the Larvae cooldown min will be 550 and max 1150)")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int cooldownReduction = 10;
			}
			
		
			public DragonMinions minions = new DragonMinions();
			
			public static class DragonMinions {
				@Name("Difficulty to Spawn Minions")
				@Comment("Minimum Difficulty required for the Ender Dragon to start spawning Dragon's Minions during the fight. Set to -1 to disable Dragon's Minions spawning.")
				@RangeInt(min = 0, max = Integer.MAX_VALUE)
				public int difficultyToSpawn = 2;
				@Name("Min Cooldown")
				@Comment("After how many minimum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int minCooldown = 1200;
				@Name("Max Cooldown")
				@Comment("After how many maximum ticks (20 ticks = 1 second) the Ender Dragon will try to spawn a Minion.")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int maxCooldown = 1800;
				@Name("Cooldown Reduction per Difficulty")
				@Comment("For each difficulty the Minion spawn cooldown min and max values will be reduced by this value (E.g. with 10 difficulty and this set to 10, the Minion cooldown min will be 500 and max 1100)")
				@RangeInt(min = 1, max = Integer.MAX_VALUE)
				public int spawnCooldownReduction = 10;
			}
			
		
			public Rewards rewards = new Rewards();
			
			public static class Rewards {
				@Name("Bonus Experience")
				@Comment("How much more experience (percentage) will Ender Dragon drop per Difficulty. The percentage is additive (e.g. with this set to 10%, 7 Ender Dragons killed = 70% more experience).")
				public float bonusExperience = 10;
				@Name("First Dragon per Player")
				@Comment("Should the first Dragon killed per Player always drop the egg and the first Dragon experience (12k instead of 500)? If true means that every player will get the Dragon Egg as they kill their first Dragon.")
				public boolean firstDragonPerPlayer = true;
				
			}
		}
	}

	@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
	private static class EventHandler{
		@SubscribeEvent
	    public static void onConfigChangedEvent(OnConfigChangedEvent event)
	    {
	        if (event.getModID().equals(ProgressiveBosses.MOD_ID))
	        {
	            ConfigManager.sync(ProgressiveBosses.MOD_ID, Type.INSTANCE);
	        }
	    }
	}
}
