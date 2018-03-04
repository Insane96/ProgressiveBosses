package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.events.entities.Dragon;
import net.insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EntityJoinWorld {	
	@SubscribeEvent
	public static void EntityJoinWorldEvent(EntityJoinWorldEvent event) {
		if (event.getWorld().isRemote)
			return;
		
		Wither.SetStats(event);
		Dragon.SetStats(event);
	}

}
