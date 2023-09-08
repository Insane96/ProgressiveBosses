package insane96mcp.progressivebosses.event;

import insane96mcp.progressivebosses.ProgressiveBosses;
import insane96mcp.progressivebosses.module.dragon.entity.Larva;
import insane96mcp.progressivebosses.module.wither.entity.PBWither;
import insane96mcp.progressivebosses.module.wither.entity.minion.WitherMinion;
import insane96mcp.progressivebosses.setup.PBEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ProgressiveBosses.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EntityRegisterAttributes {
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(PBEntities.WITHER.get(), PBWither.prepareAttributes().build());
		event.put(PBEntities.WITHER_MINION.get(), WitherMinion.prepareAttributes().build());
		event.put(PBEntities.LARVA.get(), Larva.prepareAttributes().build());
	}
}
