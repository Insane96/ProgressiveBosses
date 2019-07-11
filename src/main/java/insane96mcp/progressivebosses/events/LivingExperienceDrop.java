package net.insane96mcp.progressivebosses.events;

import net.insane96mcp.progressivebosses.ProgressiveBosses;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class LivingExperienceDrop {

	@SubscribeEvent
	public static void EventLivingExperienceDrop(LivingExperienceDropEvent event) {
		//Dragon.SetExperience(event);
	}
}
