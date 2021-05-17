package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.Dragon;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingDeath {

    @SubscribeEvent
    public static void eventLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().world.isRemote)
            return;

        Dragon.onDeath(event);
    }
}
