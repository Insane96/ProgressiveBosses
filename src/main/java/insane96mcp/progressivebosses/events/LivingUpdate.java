package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.Dragon;
import insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingUpdate {
    @SubscribeEvent
    public static void eventLivingUpdate(LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote)
            return;

        Wither.update(event);
        Dragon.update(event);
    }
}
