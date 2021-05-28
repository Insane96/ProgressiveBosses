package insane96mcp.progressivebosses.base;

import insane96mcp.progressivebosses.ProgressiveBosses;

import java.util.UUID;

public class Strings {
	public static class Tags {
		public static final String DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "difficulty";

		public static final String SPAWNED_WITHERS = ProgressiveBosses.RESOURCE_PREFIX + "spawned_withers";
		public static final String WITHER_BONUS_HEALTH = ProgressiveBosses.RESOURCE_PREFIX + "wither_bonus_health";
		public static final String WITHER_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "wither_minion_cooldown";
		public static final String WITHER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "wither_minion";
		public static final String MINIONS = ProgressiveBosses.RESOURCE_PREFIX + "minions";
		public static final String CHARGE_ATTACK = ProgressiveBosses.RESOURCE_PREFIX + "charge_attack";

		public static final String KILLED_DRAGONS = ProgressiveBosses.RESOURCE_PREFIX + "killed_dragons";
		public static final String FIRST_DRAGON = ProgressiveBosses.RESOURCE_PREFIX + "first_dragon";
		public static final String EGGS_TO_DROP = ProgressiveBosses.RESOURCE_PREFIX + "eggs_to_drop";
		public static final String DRAGON_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion_cooldown";
		public static final String DRAGON_LARVA_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva_cooldown";
	}

	public static class AttributeModifiers {
		public static final UUID MOVEMENT_SPEED_BONUS_UUID = UUID.fromString("8588420e-ce50-4e4e-a3e4-974dfc8a98ec");
		public static final String MOVEMENT_SPEED_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "movement_speed_bonus";
		public static final UUID FOLLOW_RANGE_BONUS_UUID = UUID.fromString("58eb2705-8b21-41b6-8b8e-412fcdaeda97");
		public static final String FOLLOW_RANGE_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "follow_range_bonus";
		public static final UUID SWIM_SPEED_BONUS_UUID = UUID.fromString("d7007c0f-4533-4911-870b-63cce20328f0");
		public static final String SWIM_SPEED_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "swim_speed_bonus";
	}

	public static class Translatable {
		public static final String PLAYER_SET_BOSS_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_set_boss_difficulty";
		public static final String PLAYER_ADD_BOSS_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_add_boss_difficulty";
		public static final String PLAYER_GET_WITHER_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_get_wither_difficulty";
		public static final String PLAYER_GET_DRAGON_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_get_dragon_difficulty";

		public static final String WITHER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "wither.minion";
	}
	public static class Items {
		public static final String NETHER_STAR_SHARD = "nether_star_shard";
	}
}
