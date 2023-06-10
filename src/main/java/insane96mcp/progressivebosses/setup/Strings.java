package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;

import java.util.UUID;

public class Strings {
	public static class Tags {
		public static final String DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "difficulty";

		public static final String SPAWNED_WITHERS = ProgressiveBosses.RESOURCE_PREFIX + "spawned_withers";
		public static final String WITHER_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "wither_minion_cooldown";
		public static final String WITHER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "wither_minion";
		public static final String MINIONS = ProgressiveBosses.RESOURCE_PREFIX + "minions";
		public static final String CHARGE_ATTACK = ProgressiveBosses.RESOURCE_PREFIX + "charge_attack";
		public static final String UNSEEN_PLAYER_TICKS = ProgressiveBosses.RESOURCE_PREFIX + "unseen_player_ticks";
		public static final String BARRAGE_ATTACK = ProgressiveBosses.RESOURCE_PREFIX + "barrage_attack";

		public static final String KILLED_DRAGONS = ProgressiveBosses.RESOURCE_PREFIX + "killed_dragons";
		public static final String FIRST_DRAGON = ProgressiveBosses.RESOURCE_PREFIX + "first_dragon";
		public static final String EGGS_TO_DROP = ProgressiveBosses.RESOURCE_PREFIX + "eggs_to_drop";
		public static final String CRYSTAL_CAGES = ProgressiveBosses.RESOURCE_PREFIX + "crystal_cages";
		public static final String MORE_CRYSTALS = ProgressiveBosses.RESOURCE_PREFIX + "more_crystals";
		public static final String CRYSTAL_RESPAWN = ProgressiveBosses.RESOURCE_PREFIX + "crystal_respawn";
		public static final String DRAGON_MINION = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion";
		public static final String DRAGON_LARVA = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva";
		public static final String DRAGON_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion_cooldown";
		public static final String DRAGON_LARVA_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva_cooldown";
		public static final String BLINDNESS_BULLET = ProgressiveBosses.RESOURCE_PREFIX + "blindness_bullet";

		public static final String PREVIOUSLY_NEAR_ELDER_GUARDIAN = ProgressiveBosses.RESOURCE_PREFIX + "previously_near_elder_guardian";
		public static final String ADVENTURE_MESSAGE = ProgressiveBosses.RESOURCE_PREFIX + "adventure_message";
		public static final String ELDER_MINION_COOLDOWN = ProgressiveBosses.RESOURCE_PREFIX + "elder_minion_cooldown";
		public static final String ELDER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "elder_minion";
	}

	public static class AttributeModifiers {
		public static final UUID MOVEMENT_SPEED_BONUS_UUID = UUID.fromString("8588420e-ce50-4e4e-a3e4-974dfc8a98ec");
		public static final String MOVEMENT_SPEED_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "movement_speed_bonus";
		public static final UUID BONUS_HEALTH_UUID = UUID.fromString("a28d3ed0-45e7-41bc-9690-8ac4cd4e1ae5");
		public static final String BONUS_HEALTH = ProgressiveBosses.RESOURCE_PREFIX + "bonus_health";
		public static final UUID FOLLOW_RANGE_BONUS_UUID = UUID.fromString("58eb2705-8b21-41b6-8b8e-412fcdaeda97");
		public static final String FOLLOW_RANGE_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "follow_range_bonus";
		public static final UUID SWIM_SPEED_BONUS_UUID = UUID.fromString("d7007c0f-4533-4911-870b-63cce20328f0");
		public static final String SWIM_SPEED_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "swim_speed_bonus";
		public static final UUID ATTACK_DAMAGE_BONUS_UUID = UUID.fromString("6970dece-d8f3-4233-85c5-24de8852f32b");
		public static final String ATTACK_DAMAGE_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "attack_damage_bonus";
	}

	public static class Translatable {
		public static final String PLAYER_SET_BOSS_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_set_boss_difficulty";
		public static final String PLAYER_ADD_BOSS_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_add_boss_difficulty";
		public static final String PLAYER_GET_WITHER_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_get_wither_difficulty";
		public static final String PLAYER_GET_DRAGON_DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "command.player_get_dragon_difficulty";
		public static final String SUMMONED_ENTITY = ProgressiveBosses.RESOURCE_PREFIX + "command.summoned_entity";
		public static final String SUMMON_ENTITY_INVALID = ProgressiveBosses.RESOURCE_PREFIX + "command.summon_entity_invalid";


		public static final String DRAGON_MINION = ProgressiveBosses.RESOURCE_PREFIX + "dragon_minion";
		public static final String DRAGON_LARVA = ProgressiveBosses.RESOURCE_PREFIX + "dragon_larva";

		public static final String DRAGON_FIREBALL = ProgressiveBosses.RESOURCE_PREFIX + "dragon_fireball";

		public static final String FIRST_DRAGON_KILL = "dragon.first_killed";
		public static final String FIRST_WITHER_SUMMON = "wither.first_summon";
		public static final String APPROACHING_ELDER_GUARDIAN = "elder_guardian.approach";

		public static final String ELDER_MINION = ProgressiveBosses.RESOURCE_PREFIX + "elder_minion";
	}
	public static class Items {
		public static final String NETHER_STAR_SHARD = "nether_star_shard";
		public static final String ELDER_GUARDIAN_SPIKE = "elder_guardian_spike";
	}
}
