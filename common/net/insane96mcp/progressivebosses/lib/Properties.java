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
		public static String DESCRIPTION = "Set here every parameter for withers, like how much more health should have, etc.";
		
		public static int spawnRadiusPlayerCheck;
		public static float bonusHealthPerSpawned;
		public static boolean sumSpawnedWither;
		public static int spawnWitherSkeletonsAt;
		public static int spawnWitherSkeletonsMinCooldown;
		public static int spawnWitherSkeletonsMaxCooldown;
		public static float spawnWitherSkeletonsSword;
		public static int normalWitherCount;
		
		public static void Init() {
			spawnRadiusPlayerCheck = Config.LoadIntProperty(CATEGORY, "spawn_radius_player_check", "How much blocks from wither will be scanned for players to check", 96);
			bonusHealthPerSpawned = Config.LoadFloatProperty(CATEGORY, "bonus_health_per_spawned", "How much health the withers will have more for each wither that has been already spawned", 33.3f);
			sumSpawnedWither = Config.LoadBoolProperty(CATEGORY, "sum_spawned_wither", "If true and there are more players around the wither, the wither will have his stats based on the sum of both players spawned withers. If false, the wither stats will be based on the average of the spawned wither count of the players around", false);
			spawnWitherSkeletonsAt = Config.LoadIntProperty(CATEGORY, "spawn_wither_skeleton_at", "After how many withers spawned by players, the wither will start spawning wither skeletons during the fight. The wither will spawn x wither skeletons based on this value (1 wither skeleton at 4 spawned withers, 2 wither skeletons at 8 spawned withers, etc.)", 4);
			spawnWitherSkeletonsMinCooldown = Config.LoadIntProperty(CATEGORY, "spawn_wither_skeleton_min_cooldown", "After how many minimum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 150);
			spawnWitherSkeletonsMaxCooldown = Config.LoadIntProperty(CATEGORY, "spawn_wither_skeleton_max_cooldown", "After how many maximum ticks (20 ticks = 1 second) the wither will try to spawn wither skeletons", 250);
			spawnWitherSkeletonsSword = Config.LoadFloatProperty(CATEGORY, "spawn_wither_skeletons_sword", "Base Chance for wither skeletons  to spawn with swords. The chance is increased by 1 for each spawned wither. Set this to a really low value (e.g. -1000000, don't go below -2 billions) to disable", 20.0f);
			normalWitherCount = Config.LoadIntProperty(CATEGORY, "normal_wither_count", "After how many withers spawned the wither will have the same health as vanilla? (e.g The spawned count for the player is 0, this is 2; the wither will have 1/3rd of the stats (1/-(-this + spawned_count - 1))). By default, the first withers spawned is easier", 1);
		}
	}
	
	public static class Dragon{
		public static String CATEGORY = "dragon";
		public static String DESCRIPTION = "Set here every parameter for ender dragon, like how much more health should have, etc.";
		
		public static float bonusHealthPerKilled;
		public static boolean sumKilledDragons;
		
		public static void Init() {
			bonusHealthPerKilled = Config.LoadFloatProperty(CATEGORY, "bonus_health_per_killed", "How much health will have the ender dragon for each ender dragon that has been killed", 33.3f);
			sumKilledDragons = Config.LoadBoolProperty(CATEGORY, "sum_spawned_dragon", "If true and there are more players around the dragon that has spawned, the dragon will have his stats based on the sum of both players killed dragons. If false, the dragon stats will be based on the average of the killed dragons count of the players around", false);
		}
	}
}
