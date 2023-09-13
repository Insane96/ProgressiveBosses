package insane96mcp.progressivebosses.utils;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.ILvl;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.ElderGuardian;

public class LvlHelper {
    public static int getLvl(LivingEntity entity) {
        if (entity instanceof ILvl levellableMob) {
            return levellableMob.getLvl();
        }
        else if (entity instanceof EnderDragon || entity instanceof ElderGuardian) {
            return entity.getPersistentData().getInt(ProgressiveBosses.RESOURCE_PREFIX + "lvl");
        }
        return 0;
    }
    public static void setLvl(LivingEntity entity, int lvl) {
        if (entity instanceof ILvl levellableMob) {
            levellableMob.setLvl(lvl);
        }
        else if (entity instanceof EnderDragon || entity instanceof ElderGuardian) {
            entity.getPersistentData().putInt(ProgressiveBosses.RESOURCE_PREFIX + "lvl", lvl);
        }
    }
}