package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.insane96mcp.progressivebosses.events.entities.Dragon;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingHurt {
	
	@SubscribeEvent
	public static void EventLivingHurt(LivingHurtEvent event) {
		if (event.getEntity().world.isRemote)
			return;
		
		Dragon.OnPlayerDamage(event);
	}
}
