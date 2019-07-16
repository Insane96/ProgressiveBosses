package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.events.entities.Dragon;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingHurt {

    @SubscribeEvent
    public static void eventLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().world.isRemote)
            return;

        Dragon.onPlayerDamage(event);
    }
}
