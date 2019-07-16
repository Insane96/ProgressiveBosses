package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.Dragon;
import insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class EntityJoinWorld {
    @SubscribeEvent
    public static void entityJoinWorldEvent(EntityJoinWorldEvent event) {

        if (event.getWorld().isRemote)
            return;

        Wither.setStats(event);
        Dragon.setStats(event);
    }

}
