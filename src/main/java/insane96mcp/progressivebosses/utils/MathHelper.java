package insane96mcp.progressivebosses.utils;

import net.minecraft.util.RandomSource;

// Cloned from InsaneLib to support 1.19
// TODO: Remove when Insane96 gets around to patching this.
public class MathHelper {
    public static double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static float round(float value, int places) {
        double scale = Math.pow(10, places);
        return (float) (Math.round(value * scale) / scale);
    }

    /**
     * Given a value, will return the integer part plus a chance given by the decimal part to have a +1 on the return value
     * Example 1.2 would have 20% chance to return 2 and 80% chance to return 1
     */
    public static int getAmountWithDecimalChance(RandomSource rand, float f) {
        return getAmountWithDecimalChance(rand, (double) f);
    }

    /**
     * Given a value, will return the integer part plus a chance given by the decimal part to have a +1 on the return value
     * Example 1.2 would have 20% chance to return 2 and 80% chance to return 1
     */
    public static int getAmountWithDecimalChance(RandomSource rand, double f) {
        double mod = f - (int)f;
        if (mod == 0f)
            return (int) f;
        f -= mod;
        if (rand.nextDouble() < mod)
            f++;
        return (int) f;
    }
}
