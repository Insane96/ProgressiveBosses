package insane96mcp.progressivebosses.module.dragon.data;

import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class DragonAnger {

    public static float flySpeedMultiplier(EnderDragon dragon) {
        return /*isAngered(dragon) ? 1.3f :*/ 1f;
    }
}
