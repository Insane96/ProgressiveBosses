package insane96mcp.progressivebosses.utils;

import net.minecraft.util.Mth;

public class Utils {
    /**
     * Maps {@code value} from the input range [{@code min}, {@code max}] to the output range
     * [{@code outputMin}, {@code outputMax}], with the mapping inverted:
     * <ul>
     *   <li>when {@code value >= max} → returns {@code outputMin}</li>
     *   <li>when {@code value <= min} → returns {@code outputMax}</li>
     *   <li>in-between → linearly interpolated</li>
     * </ul>
     *
     * @param value     the input value to evaluate
     * @param max       the upper bound of the input range
     * @param min       the lower bound of the input range
     * @param outputMin the output value when {@code value >= max}
     * @param outputMax the output value when {@code value <= min}
     * @return the mapped output value, clamped to [{@code outputMin}, {@code outputMax}]
     */
    public static float getChanceAtValue(float value, float max, float min, float outputMin, float outputMax) {
        if (max == min)
            return outputMin;
        float clampedValue = Mth.clamp((max - value) / (max - min), 0f, 1f);
        return outputMin + clampedValue * (outputMax - outputMin);
    }
}
