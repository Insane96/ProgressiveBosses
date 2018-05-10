package net.insane96mcp.progressivebosses.lib;

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
			
			public static void Init() {
				spawnRadiusPlayerCheck = Config.LoadIntProperty(SUBCATEGORY, "spawn_radius_player_check", "How much blocks from wither will be scanned for players to check", 96);
				sumSpawnedWither = Config.LoadBoolProperty(SUBCATEGORY, "sum_spawned_wither", "If true and there are more players around the wither, the wither will have his stats based on the sum of both players spawned withers. If false, the wither stats will be based on the average of the spawned wither count of the players around", false);
			}
		}
		
		public static class Rewards{
			public static String SUBCATEGORY = CATEGORY + ".rewards";
			public static String SUBDESCRIPTION = "Set here every parameter for wither's rewards";
			
			public static float bonusExperience;
			public static float shardPerSpawned;
			public static float shardMaxChance;
			public static int shardDivider;
			public static int shardMaxCount;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusExperience = Config.LoadFloatProperty(SUBCATEGORY, "bonus_experience", "How much more percentage experience will wither drop per wither spawned. The percentage is additive (e.g. 10% experience boost, 7 withers spawned = 70% more experience)", 10.0f);
			
				shardPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "shard_per_spawned", "How much chance per wither spawned to get a Nether Star Shard from killing the wither", 2f);
				shardMaxChance = Config.LoadFloatProperty(SUBCATEGORY, "shard_max_chance", "Maximum chance to get a Wither Skull", 50.0f);
				shardDivider = Config.LoadIntProperty(SUBCATEGORY, "shard_ratio", "Divider of killed withers for how many times the game tries to drop one more shard. Given this value x you get ((killed_wither / x) + 1) times to get one more shard.\nE.g. By default, at 10 withers killed you have 20% chance to drop a shard, another 20% chance to get another one, etc. up to 6 times.", 2);
				shardMaxCount = Config.LoadIntProperty(SUBCATEGORY, "shard_max_count", "Maximum amount of shard that you can get from wither", 8);
			}
		}
		
		public static class Skeletons{
			public static String SUBCATEGORY = CATEGORY + ".skeletons";
			public static String SUBDESCRIPTION = "Set here every parameter for wither skeletons spawned by the Wither";
			
			public static int spawnAt;
			public static int spawnEvery;
			public static int spawnMaxCount;
			public static int spawnMinCooldown;
			public static int spawnMaxCooldown;
			public static float spawnWithSword;
			public static int minArmor;
			public static int maxArmor;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many withers spawned by players, the wither will start spawning wither skeletons during the fight. Can be 0", 2);
				spawnEvery = Config.LoadIntProperty(SUBCATEGORY, "spawn_every", "As the wither starts spawning wither skeletons, every how much withers spawned the wither will spawn one more wither. Cannot be lower than 1", 4);
				spawnMaxCount = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_count", "Maximum number of wither skeletons that a Wither can spawn", 8);
				spawnMinCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 150);
				spawnMaxCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 300);
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
			public static float regenPerSpawned;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "bonus_per_spawned", "How much health the withers will have more for each wither that has been already spawned", 10f);
				maximumRegeneration = Config.LoadFloatProperty(SUBCATEGORY, "max_regeneration", "Maximum regeneration for regen_per_spawned. Set to 0 to disable health regeneration. It's not recommended to go over 1.0f without mods that adds stronger items, potions, etc.", 1.0f);
				regenPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "regen_per_spawned", "How many half hearts will regen the wither per wither spawned, this doesn't alter the normal health regeneration of the wither (1 hp per second) (E.g. With 6 withers spawned, the wither will heal 0.6 half-hearts more per second).", 0.05f);
			}
		}
		
		public static class Armor {
			public static String SUBCATEGORY = CATEGORY + ".armor";
			public static String SUBDESCRIPTION = "Set here every parameter for wither's armor";

			public static float bonusPerSpawned;
			public static float maximum;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerSpawned = Config.LoadFloatProperty(SUBCATEGORY, "bonus_per_killed", "How much armor points will have withers for each time a wither is spawned", 0.25f);
				maximum = Config.LoadFloatProperty(SUBCATEGORY, "maximum", "Maximum armor points that withers can spawn with. It's not recommended to go over 20", 10f);
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
			Rewards.Init();
		}
		
		public static class General {
			public static String SUBCATEGORY = CATEGORY + ".general";
			
			public static boolean sumKilledDragons;
			
			public static void Init() {
				sumKilledDragons = Config.LoadBoolProperty(SUBCATEGORY, "sum_spawned_dragon", "If true and there are more players around the dragon that has spawned, the dragon will have his stats based on the sum of both players killed dragons. If false, the dragon stats will be based on the average of the killed dragons count of the players around", false);
			}
			
		}
		
		public static class Rewards {
			public static String SUBCATEGORY = CATEGORY + ".rewards";
			public static String SUBDESCRIPTION = "Set here every parameter for dragon's rewards";
			
			public static float bonusExperience;
			public static float eggDropPerKilled;
			public static float eggDropMaximum;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusExperience = Config.LoadFloatProperty(SUBCATEGORY, "bonus_experience", "How much more percentage experience will ender dragon drop per dragon killed. The percentage is additive (e.g. 5% experience boost, 7 dragons killed = 35% more experience) (Not working as now, this config will do nothing)", 35.0f);
				eggDropPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "egg_drop_per_killed", "Chance increase for each dragon killed for dragon to drop a Dragon Egg", 2.5f);
				eggDropMaximum = Config.LoadFloatProperty(SUBCATEGORY, "egg_drop_max", "Max chance for ender dragon to drop a dragon egg", 50.0f);
				
			}
		}
		
		public static class Endermites {
			public static String SUBCATEGORY = CATEGORY + ".endermites";
			public static String SUBDESCRIPTION = "Set here every parameter for Dragon's health";

			public static int spawnAt;
			public static int spawnMaxCount;
			public static int spawnMinCooldown;
			public static int spawnMaxCooldown;
			public static int spawnCooldownReduction;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many dragons killed, the dragon will start spawning ultrafast endermites (Dragon's Larvae) at the center island. The ender dragon will spawn x endermites based on this value (by default 1 endermite at 4 killed dragons, 2 endermite at 8 killed dragons, etc.)", 4);
				spawnMaxCount = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_count", "Maximum number of Endermites that the dragon can spawn", 6);
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
			public static float regenPerKilled;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "bonus_health_per_killed", "How much health will have the ender dragon for each ender dragon that has been killed", 10f);
				maximumRegeneration = Config.LoadFloatProperty(SUBCATEGORY, "max_regeneration", "Maximum regeneration for regen_per_spawned. Set to 0 to disable health regeneration. It's not recommended to go over 1.0f without mods that adds stronger items, potions, etc.", 1.0f);
				regenPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "regen_per_killed", "How many half hearts will regen the dragon per dragons killed per second (E.g. With 6 dragons killed, the dragon will heal 0.6 half-hearts per second).", 0.05f);
			}
		}
		
		public static class Armor {
			public static String SUBCATEGORY = CATEGORY + ".armor";
			public static String SUBDESCRIPTION = "Set here every parameter for dragon's armor";

			public static float bonusPerKilled;
			public static float maximum;
			
			public static void Init() {
				Config.SetCategoryComment(SUBCATEGORY, SUBDESCRIPTION);
				bonusPerKilled = Config.LoadFloatProperty(SUBCATEGORY, "bonus_armor_per_killed", "How much armor points will have ender dragons for each time a dragon is killed", 0.2f);
				maximum = Config.LoadFloatProperty(SUBCATEGORY, "maximum_armor", "Maximum armor points that enderdragons can spawn with. It's not recommended to go over 10 as the Ender Dragon already has some damage reduction", 5f);
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
				
				spawnAt = Config.LoadIntProperty(SUBCATEGORY, "spawn_at", "After how many dragons killed, the dragon will start spawning shulker (Dragon's Minion).", 5);
				spawnMinCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers", 900);
				spawnMaxCooldown = Config.LoadIntProperty(SUBCATEGORY, "spawn_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the enderdragon will spawn shulkers", 1200);
				spawnCooldownReduction = Config.LoadIntProperty(SUBCATEGORY, "spawn_cooldown_reduction", "For each killed dragon the spawn shulkers cooldown min and max will be reduced by this value (E.g. with 10 killed dragons and this set to 5, the spawn shulkers cooldown min will be 300 and max 400)", 10);
			}
		}
	}
}
