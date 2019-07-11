package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.insane96mcp.progressivebosses.events.entities.Wither;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingDrops {

	@SubscribeEvent
	public static void EventLivingDrops(LivingDropsEvent event) {
		Wither.SetDrops(event);
	}
}
