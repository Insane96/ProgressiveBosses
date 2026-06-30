package insane96mcp.progressivebosses.utils;

import net.minecraft.util.Mth;

public class Utils {
    /**
     * Returns a value (outputMin~outputMax) based off a min and max value. when value >= max the chance is outputMin. when value <= min the chance is outputMax. In-between the threshold, chance scales accordingly
     */
    public static float getChanceAtValue(float value, float max, float min, float outputMin, float outputMax) {
        float clampedValue = Mth.clamp((max - min - (value - min)) / (max - min), 0f, 1f);
        return outputMin + clampedValue * (outputMax - outputMin);
    }
}
