package net.insane96mcp.progressivebosses.lib;

import java.util.Random;

public class Utils {
	public static class Math {	
		public static float getFloat(Random random, float min, float max) {
			return min >= max ? min : random.nextFloat() * (max - min + 1) + min;
		}
	}
	
	public static class CustomReward {
		String id;
		float difficulty;
		float chance;
		RewardMode mode;
		
		public CustomReward(String id, float difficulty, float chance, RewardMode mode) {
			this.id = id;
			this.difficulty = difficulty;
			this.chance = chance;
			this.mode = mode;
		}
		
		enum RewardMode {
			ONCE,
			REPEAT,
			EXPONENTIAL
		}
	}
}
