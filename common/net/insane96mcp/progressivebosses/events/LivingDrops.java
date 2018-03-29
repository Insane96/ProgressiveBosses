package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LivingDrops {

	@SubscribeEvent
	public static void EventLivingDrops(LivingDropsEvent event) {
		Wither.SetDrops(event);
	}
}
