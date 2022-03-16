package insane96mcp.progressivebosses.utils;

import java.util.Random;

public class Utils {
	/**
	 * Given a value, will return the integer part plus a chance given by the decimal part to have a +1 on the return value
	 * Example 1.2 would have 20% chance to return 2 and 80% chance to return 1
	 */
	public static int getAmountWithDecimalChance(Random rand, float f) {
		return getAmountWithDecimalChance(rand, (double) f);
	}

	/**
	 * Given a value, will return the integer part plus a chance given by the decimal part to have a +1 on the return value
	 * Example 1.2 would have 20% chance to return 2 and 80% chance to return 1
	 */
	public static int getAmountWithDecimalChance(Random rand, double f) {
		double mod = f - (int)f;
		if (mod == 0f)
			return (int) f;
		f -= mod;
		if (rand.nextDouble() < mod)
			f++;
		return (int) f;
	}
}
