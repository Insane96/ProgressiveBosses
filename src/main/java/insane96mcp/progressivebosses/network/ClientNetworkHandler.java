package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public class ClientNetworkHandler {
    public static void applyDragonAnger(int entityId, boolean isAngry) {
        if (Minecraft.getInstance().level == null)
            return;

        Entity entity = Minecraft.getInstance().level.getEntity(entityId);
        if (entity instanceof EnderDragon dragon)
            DragonAnger.setAngered(dragon, isAngry);
    }
}
