package insane96mcp.progressivebosses.network;

import insane96mcp.progressivebosses.module.dragon.data.DragonAnger;
import net.minecraft.client.Minecraft;
import net.minecraft.server.TickTask;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.common.util.LogicalSidedProvider;
import net.minecraftforge.fml.LogicalSide;

public class ClientNetworkHandler {
    public static void applyDragonAnger(int entityId, boolean isAngry) {
        BlockableEventLoop<? super TickTask> executor = LogicalSidedProvider.WORKQUEUE.get(LogicalSide.CLIENT);
        executor.tell(new TickTask(1, () -> {
            if (Minecraft.getInstance().level == null)
                return;

            Entity entity = Minecraft.getInstance().level.getEntity(entityId);
            if (entity instanceof EnderDragon dragon)
                DragonAnger.setAngered(dragon, isAngry);
        }));
    }
}
