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
		public static final UUID FOLLOW_RANGE_BONUS_UUID = UUID.fromString("58eb2705-8b21-41b6-8b8e-412fcdaeda97");
		public static final String FOLLOW_RANGE_BONUS = ProgressiveBosses.RESOURCE_PREFIX + "follow_range_bonus";
	}
}
