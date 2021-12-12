package insane96mcp.progressivebosses.events;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.capability.DifficultyImpl;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID)
public class CapabilityEvent {
	@SubscribeEvent
	public void registerCaps(RegisterCapabilitiesEvent event) {
		event.register(DifficultyImpl.class);
	}
}
