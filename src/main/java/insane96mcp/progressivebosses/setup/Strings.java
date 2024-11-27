package insane96mcp.progressivebosses.setup;

import insane96mcp.progressivebosses.ProgressiveBosses;

import java.util.UUID;

public class Strings {
	public static class Tags {
		//TODO
		public static final String DIFFICULTY = ProgressiveBosses.RESOURCE_PREFIX + "difficulty";
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
}
