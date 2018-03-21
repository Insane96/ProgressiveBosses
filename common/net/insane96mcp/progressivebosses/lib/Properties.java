package net.insane96mcp.progressivebosses.lib;

import net.minecraft.item.crafting.ShulkerBoxRecipes;

public class Properties {
	
	public static void Init() {
		General.Init();
		Wither.Init();
		Dragon.Init();
	}
	
	public static class General{
		
		public static void Init() {

		}
	}

	public static class Wither{
		public static String CATEGORY = "wither";
		public static String DESCRIPTION = "Set here every parameter for withers";
		
		public static void Init() {
			Config.SetCategoryComment(CATEGORY, DESCRIPTION);
			General.Init();
			Skeletons.Init();
			Health.Init();
			Armor.Init();
			Rewards.Init();
		}
		
		public static class General{
			public static String SUBCATEGORY = CATEGORY + ".general";

			public static int spawnRadiusPlayerCheck;
			public static boolean sumSpawnedWither;
			public static int normalWitherCount;
			
			public static void Init() {
				spawnRadiusPlayerCheck = Config.LoadIntProperty(SUBCATEGORY, "spawn_radius_player_check", "How much blocks from wither will be scanned for players to check", 96);
				sumSpawnedWither = Config.LoadBoolProperty(SUBCATEGORY, "sum_spawned_wither", "If true and there are more players around the wither, the wither will have his stats based on the sum of both players spawned withers. If false, the wither stats will be based on the average of the spawned wither count of the players around", false);
				normalWitherCount = Config.LoadIntProperty(SUBCATEGORY, "normal_wither_count", "After how many withers spawned the wither will have the same health as vanilla? (e.g The spawned count for the player is 0, this is 2; the wither will have less stats. By default, the first wither spawned is easier", 1);
			}
		}
		
		public static class Rewards{
			public static String SUBCATEGORY = CATEGORY + ".rewards";
			public static String SUBDESCRIPTION = "Set here every parameter for wither skeletons more rewards";
			
			public static float bonusExperience;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusExperience = Config.LoadFloatProperty(SUBCATEGORY, "bonus_experience", "How much more percentage experience will wither drop per wither spawned. The percentage is additive (e.g. 10% experience boost, 7 withers killed = 70% more experience)", 10.0f);
			}
		}
		
		public static class Skeletons{
			public static String SUBCATEGORY = CATEGORY + ".skeletons";
			public static String SUBDESCRIPTION = "Set here every parameter for wither skeletons spawned by the Wither";
			
			public static int spawnAt;
			public static int spawnMinCooldown;
			public static int spawnMaxCooldown;
			public static float spawnWithSword;
			public static int minArmor;
			public static int maxArmor;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many withers spawned by players, the wither will start spawning wither skeletons during the fight. The wither will spawn x wither skeletons based on this value (by default 1 wither skeleton at 4 spawned withers, 2 wither skeletons at 8 spawned withers, etc.)", 4);
				spawnMinCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 150);
				spawnMaxCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 250);
				spawnWithSword = Config.LoadFloatProperty(SUBCATEGORY, "spawn_with_sword", "Base Chance for wither skeletons  to spawn with swords. The chance is increased by 1 for each spawned wither. Set this to a really low value (e.g. -1000000, don't go below -2 billions) to disable", 20.0f);
				minArmor = Config.LoadIntProperty(SUBCATEGORY, "min_armor", "Minimum armor value that wither skeletons should spawn with", 0);
				maxArmor = Config.LoadIntProperty(SUBCATEGORY, "max_armor", "Maximum armor value that wither skeletons should spawn with. The maximum armor is actually the difficulty, up to this value", 20);
			}
		}
		
		public static class Health {
			public static String SUBCATEGORY = CATEGORY + ".health";
			public static String SUBDESCRIPTION = "Set here every parameter for wither's health";

			public static float bonusPerSpawned;
			public static float maximumRegeneration;
			public static float regenerationRate;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "bonus_per_spawned", "How much health the withers will have more for each wither that has been already spawned", 25f);
				maximumRegeneration = Config.LoadFloatProperty(SUBCATEGORY, "max_regeneration", "For every wither spawned, the wither will regenerate health (without crystals). He regenerates (killed_dragons / 10) per second. Up to a maximum of this value. Set to 0 to disable health regeneration. It's not recommended to go over 1.0f without mods that adds stronger items, potions, etc.", 1.0f);
				regenerationRate = Config.LoadFloatProperty(SUBCATEGORY, "regeneration_rate", "Increases (or decreases) the amount of health that the wither heals for each wither killed. Setting this to 1.0 will make wither heal at normal regeneration rate (spawned_wither / 10)", 1.0f);
			}
		}
		
		public static class Armor {
			public static String SUBCATEGORY = CATEGORY + ".armor";
			public static String SUBDESCRIPTION = "Set here every parameter for wither's armor";

			public static float bonusPerSpawned;
			public static float maximum;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "bonus_per_killed", "How much armor points will have withers for each time a wither is spawned", 1.0f);
				maximum = Config.LoadFloatProperty(SUBCATEGORY, "maximum", "Maximum armor points that withers can spawn with. It's not recommended to go over 30", 10f);
			}
		}
	}
	
	public static class Dragon{
		public static String CATEGORY = "dragon";
		public static String DESCRIPTION = "Set here every parameter for ender dragon, like how much more health should have, etc.";
		
		public static void Init() {
			Config.SetCategoryComment(CATEGORY, DESCRIPTION);
			General.Init();
			Health.Init();
			Armor.Init();
			Endermites.Init();
			Shulkers.Init();
		}
		
		public static class General {
			public static String SUBCATEGORY = CATEGORY + ".general";
			
			public static boolean sumKilledDragons;
			
			public static void Init() {
				sumKilledDragons = Config.LoadBoolProperty(SUBCATEGORY, "sum_spawned_dragon", "If true and there are more players around the dragon that has spawned, the dragon will have his stats based on the sum of both players killed dragons. If false, the dragon stats will be based on the average of the killed dragons count of the players around", false);
			}
			
		}
		
		public static class Endermites {
			public static String SUBCATEGORY = CATEGORY + ".endermites";
			public static String SUBDESCRIPTION = "Set here every parameter for Dragon's health";

			public static int spawnAt;
			public static int spawnMinCooldown;
			public static int spawnMaxCooldown;
			public static int spawnCooldownReduction;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many dragons killed, the dragon will start spawning ultrafast endermites (Dragon's Larvae) at the center island. The ender dragon will spawn x endermites based on this value (by default 1 endermite at 3 killed dragons, 2 endermite at 6 killed dragons, etc.)", 3);
				spawnMinCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the enderdragon will spawn endermites", 600);
				spawnMaxCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the enderdragon will spawn endermites", 1200);
				spawnCooldownReduction = Config.LoadIntProperty(SUBCATEGORY, "spawn_cooldown_reduction", "For each killed dragon the spawn endermites cooldown min and max will be reduced by this value (E.g. with 10 killed dragons and this set to 5, the spawn endermites cooldown min will be 300 and max 400)", 5);
			}
		}
		
		public static class Health {
			public static String SUBCATEGORY = CATEGORY + ".health";
			public static String SUBDESCRIPTION = "Set here every parameter for Dragon's health";

			public static float bonusPerKilled;
			public static float maximumRegeneration;
			public static float regenerationRate;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "bonus_health_per_killed", "How much health will have the ender dragon for each ender dragon that has been killed", 25f);
				maximumRegeneration = Config.LoadFloatProperty(SUBCATEGORY, "max_health_regeneration", "For every dragon killed, the dragon will regenerate health (without crystals). He regenerates (killed_dragons / 10) per second. Up to a maximum of this value. Set to 0 to disable health regeneration. It's not recommended to go over 1.0f without mods that adds stronger items, potions, etc.", 1.0f);
				regenerationRate = Config.LoadFloatProperty(SUBCATEGORY, "health_regeneration_rate", "Increases (or decreases) the amount of health that the dragon heals for each dragon killed. Setting this to 1.0 will make dragon heal at normal regeneration rate (killed_dragons / 10)", 1.0f);
			}
		}
		
		public static class Armor {
			public static String SUBCATEGORY = CATEGORY + ".armor";
			public static String SUBDESCRIPTION = "Set here every parameter for dragon's armor";

			public static float bonusPerKilled;
			public static float maximum;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "bonus_armor_per_killed", "How much armor points will have ender dragons for each time a dragon is killed", 0.5f);
				maximum = Config.LoadFloatProperty(SUBCATEGORY, "maximum_armor", "Maximum armor points that enderdragons can spawn with. It's not recommended to go over 30", 10f);
			}
		}
	
		public static class Shulkers {
			public static String SUBCATEGORY = CATEGORY + ".shulkers";
			public static String SUBDESCRIPTION = "Set here every parameter for dragon's shulkers";

			public static int spawnAt;
			public static int spawnMinCooldown;
			public static int spawnMaxCooldown;
			public static int spawnCooldownReduction;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many dragons killed, the dragon will start spawning shulkers (Dragon's Minion).", 5);
				spawnMinCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers", 600);
				spawnMaxCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers", 1200);
				spawnCooldownReduction = Config.LoadIntProperty(SUBCATEGORY, "spawn_cooldown_reduction", "For each killed dragon the spawn shulkers cooldown min and max will be reduced by this value (E.g. with 10 killed dragons and this set to 5, the spawn shulkers cooldown min will be 300 and max 400)", 10);
			}
		}
	}
}
