package insane96mcp.progressivebosses.utils;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.world.entity.LivingEntity;

public class LvlHelper {
    public static int getLvl(LivingEntity entity) {
        return ModNBTData.get(entity, ProgressiveBosses.LVL, Integer.class);
    }

    public static void setLvl(LivingEntity entity, int lvl) {
        ModNBTData.put(entity, ProgressiveBosses.LVL, lvl);
    }

    public static boolean hasLvl(LivingEntity entity) {
        return ModNBTData.contains(entity, ProgressiveBosses.LVL);
    }
}
