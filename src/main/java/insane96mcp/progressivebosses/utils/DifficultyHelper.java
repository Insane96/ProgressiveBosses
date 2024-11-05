package insane96mcp.progressivebosses.utils;

import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ElderGuardian;

public class DifficultyHelper {
    public static float getScalingDifficulty(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        float maxDifficulty = 0;
        if (entity instanceof ElderGuardian)
            maxDifficulty = 3;

        if (maxDifficulty == 0)
            return 0;

        float difficulty = persistentData.getFloat(Strings.Tags.DIFFICULTY);
        return difficulty / maxDifficulty;
    }
}
