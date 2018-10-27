package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.insane96mcp.progressivebosses.events.entities.Dragon;
import net.insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingUpdate {
	@SubscribeEvent
	public static void EventLivingUpdate(LivingUpdateEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		
		Wither.Update(event);
		Dragon.Update(event);
	}
}
