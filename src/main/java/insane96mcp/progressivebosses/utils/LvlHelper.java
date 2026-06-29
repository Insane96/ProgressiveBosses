package insane96mcp.progressivebosses.utils;

import insane96mcp.insanelib.core.ModNBTData;
import insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LvlHelper {
    public static final ResourceLocation LEVEL_KEY = ProgressiveBosses.id("lvl");

    public static int getLvl(LivingEntity entity) {
        return ModNBTData.get(entity, LEVEL_KEY, Integer.class);
    }

    public static void setLvl(LivingEntity entity, int lvl) {
        ModNBTData.put(entity, LEVEL_KEY, lvl);
    }
}
