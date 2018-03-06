package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.events.entities.Dragon;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingDeath {

	@SubscribeEvent
	public static void EventLivingDeath(LivingDeathEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		
		Dragon.OnDeath(event);
	}
}
