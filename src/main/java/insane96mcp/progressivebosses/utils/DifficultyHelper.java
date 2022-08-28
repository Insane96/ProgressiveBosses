package insane96mcp.progressivebosses.utils;

import insane96mcp.progressivebosses.module.Modules;
import insane96mcp.progressivebosses.setup.Strings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.ElderGuardian;

public class DifficultyHelper {
    public static float getScalingDifficulty(LivingEntity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        float maxDifficulty = 0;
        if (entity instanceof EnderDragon)
            maxDifficulty = Modules.dragon.difficulty.maxDifficulty;
        else if (entity instanceof WitherBoss)
            maxDifficulty = Modules.wither.difficulty.maxDifficulty;
        else if (entity instanceof ElderGuardian)
            maxDifficulty = 3;

        if (maxDifficulty == 0)
            return 0;
        //throw new UnsupportedOperationException("%s is not a valid entity".formatted(entity));

        float difficulty = persistentData.getFloat(Strings.Tags.DIFFICULTY);
        return difficulty / maxDifficulty;
    }

    public static float getScalingDifficulty(float difficulty, float maxDifficulty) {
        return difficulty / maxDifficulty;
    }
}
