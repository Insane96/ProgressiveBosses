package net.insane96mcp.progressivebosses.events;

import com.ibm.icu.text.UFormat;

import net.insane96mcp.progressivebosses.events.entities.Dragon;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import scala.inline;

public class LivingExperienceDrop {

	@SubscribeEvent
	public static void EventLivingExperienceDrop(LivingExperienceDropEvent event) {
		Dragon.SetExperience(event);
	}
}
