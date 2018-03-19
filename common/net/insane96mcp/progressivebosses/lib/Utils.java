package net.insane96mcp.progressivebosses.lib;

import java.util.Random;

public class Utils {
	public static class Math {	
		public static float getFloat(Random random, float min, float max) {
			return min >= max ? min : random.nextFloat() * (max - min + 1) + min;
		}
	}
}
